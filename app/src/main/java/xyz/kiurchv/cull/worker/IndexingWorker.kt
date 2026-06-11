package xyz.kiurchv.cull.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import xyz.kiurchv.cull.data.MediaStoreRepository
import xyz.kiurchv.cull.data.db.PhotoHashDao
import xyz.kiurchv.cull.data.db.PhotoHashEntity
import xyz.kiurchv.cull.domain.PHashEngine

@HiltWorker
class IndexingWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val mediaStore: MediaStoreRepository,
    private val hashDao: PhotoHashDao,
    private val pHashEngine: PHashEngine,
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "photo_indexing"
        const val PROGRESS_CURRENT = "current"
        const val PROGRESS_TOTAL = "total"

        fun buildRequest(): PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<IndexingWorker>(
                repeatInterval = 1,
                repeatIntervalTimeUnit = java.util.concurrent.TimeUnit.HOURS,
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(false)
                        .build()
                )
                .build()

        fun buildOneTimeRequest(): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<IndexingWorker>().build()
    }

    override suspend fun doWork(): Result {
        val photos = mediaStore.loadPhotos()
        if (photos.isEmpty()) return Result.success()

        val existingIds = hashDao.getAllIds().toSet()
        val toIndex = photos.filter { it.id !in existingIds }

        if (toIndex.isEmpty()) return Result.success()

        setProgress(workDataOf(PROGRESS_CURRENT to 0, PROGRESS_TOTAL to toIndex.size))

        val batch = mutableListOf<PhotoHashEntity>()

        toIndex.forEachIndexed { idx, photo ->
            val hash = pHashEngine.computeHash(photo.path)
            val sharpness = pHashEngine.computeSharpness(photo.path)

            if (hash != null) {
                batch += PhotoHashEntity(
                    mediaId = photo.id,
                    path = photo.path,
                    pHash = hash,
                    sharpness = sharpness,
                )
            }

            if (batch.size >= 50) {
                hashDao.upsertAll(batch.toList())
                batch.clear()
            }

            if (idx % 10 == 0) {
                setProgress(
                    workDataOf(
                        PROGRESS_CURRENT to idx + 1,
                        PROGRESS_TOTAL to toIndex.size,
                    )
                )
            }
        }

        if (batch.isNotEmpty()) hashDao.upsertAll(batch)

        // Clean up hashes for deleted photos
        val currentIds = photos.map { it.id }.toSet()
        val staleIds = existingIds - currentIds
        if (staleIds.isNotEmpty()) hashDao.deleteByIds(staleIds.toList())

        return Result.success()
    }
}
