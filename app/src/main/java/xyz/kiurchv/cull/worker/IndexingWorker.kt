package xyz.kiurchv.cull.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.kiurchv.cull.data.IndexingStore
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
    private val indexingStore: IndexingStore,
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "photo_indexing"
        const val WORK_NAME_PERIODIC = "photo_indexing_periodic"

        fun buildOneTimeRequest(): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<IndexingWorker>().build()

        fun buildPeriodicRequest(): PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<IndexingWorker>(6, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder().setRequiresBatteryNotLow(true).build()
                )
                .build()
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            indexingStore.setRunning("Сканування фотогалереї…")

            // 1. Load all photos from MediaStore (fast — metadata only)
            val photos = photoRepository.loadPhotosFromMediaStore()
            if (photos.isEmpty()) {
                indexingStore.setSuccess(0)
                return@withContext Result.success()
            }

            indexingStore.setStage("Знайдено ${photos.size} фото. Синхронізація…")
            val currentIds = photos.map { it.id }.toSet()

            // 2. Sync photo_metadata
            val existingMetaIds = photoMetadataDao.getAllIds().toSet()
            val newPhotos = photos.filter { it.id !in existingMetaIds }
            if (newPhotos.isNotEmpty()) {
                photoMetadataDao.upsertAll(newPhotos.map { photo ->
                    PhotoMetadataEntity(mediaId = photo.id, dateTaken = photo.dateTaken)
                })
            }
            val deletedIds = existingMetaIds - currentIds
            if (deletedIds.isNotEmpty()) {
                photoMetadataDao.deleteByIds(deletedIds.toList())
                hashDao.deleteByIds(deletedIds.toList())
            }

            // 3. Index pHash — latest day first, then older days
            val existingHashIds = hashDao.getAllIds().toSet()
            val toHash = photos.filter { it.id !in existingHashIds }

            if (toHash.isEmpty()) {
                indexingStore.setSuccess(photoMetadataDao.getDistinctDays().size)
                return@withContext Result.success()
            }

            val dayMs = 24 * 60 * 60 * 1000L
            val byDay = toHash
                .groupBy { it.dateTaken / dayMs }
                .toSortedMap(reverseOrder()) // latest day first

            val totalDays = byDay.size
            indexingStore.setTotalDays(totalDays)
            var daysIndexed = 0

            val dateFormat = java.text.SimpleDateFormat("d MMMM", java.util.Locale("uk"))

            byDay.forEach { (dayEpoch, dayPhotos) ->
                val dayLabel = dateFormat.format(java.util.Date(dayEpoch * dayMs))
                indexingStore.setStage(
                    "Аналіз фото: $dayLabel (${daysIndexed + 1} з $totalDays, ${dayPhotos.size} фото)"
                )

                val batch = mutableListOf<PhotoHashEntity>()
                dayPhotos.forEach { photo ->
                    val hash = pHashEngine.computeHash(photo.path) ?: return@forEach
                    val sharpness = pHashEngine.computeSharpness(photo.path)
                    batch.add(PhotoHashEntity(
                        mediaId = photo.id,
                        path = photo.path,
                        pHash = hash,
                        sharpness = sharpness,
                    ))
                    if (batch.size >= 50) {
                        hashDao.upsertAll(batch.toList())
                        batch.clear()
                    }
                }
                if (batch.isNotEmpty()) hashDao.upsertAll(batch)

                daysIndexed++
                indexingStore.incrementIndexedDays()

                setProgress(workDataOf(
                    "days_done" to daysIndexed,
                    "days_total" to totalDays,
                ))
            }

            indexingStore.setSuccess(photoMetadataDao.getDistinctDays().size)
            Result.success()
        } catch (e: Exception) {
            indexingStore.setError(e.message ?: "Невідома помилка")
            Result.failure()
        }
    }
}
