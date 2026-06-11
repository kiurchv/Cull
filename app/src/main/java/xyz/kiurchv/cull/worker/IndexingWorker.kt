package xyz.kiurchv.cull.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.kiurchv.cull.data.PhotoRepository
import xyz.kiurchv.cull.data.db.PhotoHashDao
import xyz.kiurchv.cull.data.db.PhotoHashEntity
import xyz.kiurchv.cull.data.db.PhotoMetadataDao
import xyz.kiurchv.cull.data.db.PhotoMetadataEntity
import xyz.kiurchv.cull.domain.PHashEngine
import java.util.concurrent.TimeUnit

@HiltWorker
class IndexingWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val photoRepository: PhotoRepository,
    private val photoMetadataDao: PhotoMetadataDao,
    private val hashDao: PhotoHashDao,
    private val pHashEngine: PHashEngine,
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "photo_indexing"
        const val PROGRESS_CURRENT = "current"
        const val PROGRESS_TOTAL = "total"

        fun buildRequest(): PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<IndexingWorker>(1, TimeUnit.HOURS)
                .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(false).build())
                .build()

        fun buildOneTimeRequest(): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<IndexingWorker>().build()
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val photos = photoRepository.loadPhotosFromMediaStore()
        if (photos.isEmpty()) return@withContext Result.success()

        val currentIds = photos.map { it.id }.toSet()

        // Sync photo_metadata — add new, remove deleted
        val existingMetaIds = photoMetadataDao.getAllIds().toSet()
        val newPhotos = photos.filter { it.id !in existingMetaIds }

        if (newPhotos.isNotEmpty()) {
            photoMetadataDao.upsertAll(newPhotos.map { photo ->
                PhotoMetadataEntity(
                    mediaId = photo.id,
                    dateTaken = photo.dateTaken,
                )
            })
        }

        val deletedIds = existingMetaIds - currentIds
        if (deletedIds.isNotEmpty()) {
            photoMetadataDao.deleteByIds(deletedIds.toList())
        }

        // Index pHash for new photos
        val existingHashIds = hashDao.getAllIds().toSet()
        val toHash = photos.filter { it.id !in existingHashIds }

        if (toHash.isEmpty()) return@withContext Result.success()

        setProgress(workDataOf(PROGRESS_CURRENT to 0, PROGRESS_TOTAL to toHash.size))

        val batch = mutableListOf<PhotoHashEntity>()
        toHash.forEachIndexed { idx, photo ->
            val hash = pHashEngine.computeHash(photo.path)
            val sharpness = pHashEngine.computeSharpness(photo.path)
            if (hash != null) {
                batch.add(PhotoHashEntity(
                    mediaId = photo.id,
                    path = photo.path,
                    pHash = hash,
                    sharpness = sharpness,
                ))
            }
            if (batch.size >= 50) {
                hashDao.upsertAll(batch.toList())
                batch.clear()
            }
            if (idx % 10 == 0) {
                setProgress(workDataOf(PROGRESS_CURRENT to idx + 1, PROGRESS_TOTAL to toHash.size))
            }
        }
        if (batch.isNotEmpty()) hashDao.upsertAll(batch)

        // Clean stale hashes
        val staleHashIds = existingHashIds - currentIds
        if (staleHashIds.isNotEmpty()) hashDao.deleteByIds(staleHashIds.toList())

        Result.success()
    }
}
