package xyz.kiurchv.cull.worker

import android.content.Context
import androidx.work.*
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.kiurchv.cull.data.db.PhotoHashEntity
import xyz.kiurchv.cull.data.db.PhotoMetadataEntity
import java.util.concurrent.TimeUnit

/**
 * Plain CoroutineWorker (not @HiltWorker) — dependencies are fetched via
 * EntryPointAccessors to avoid relying on HiltWorkerFactory registration,
 * which was causing this worker to fail with RunAttemptCount=0 (never started).
 */
class IndexingWorker(
    appContext: Context,
    workerParams: WorkerParameters,
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
        // Wrap everything including EntryPoint access in try/catch
        // so ANY failure (including DI issues) gets recorded to DataStore
        val indexingStore = try {
            EntryPointAccessors
                .fromApplication(applicationContext, WorkerEntryPoint::class.java)
                .indexingStore()
        } catch (e: Throwable) {
            // Can't even get indexingStore - nothing we can do except fail
            android.util.Log.e("IndexingWorker", "EntryPoint access failed", e)
            return@withContext Result.failure(
                workDataOf("error" to "${e::class.simpleName}: ${e.message}")
            )
        }

        try {
            indexingStore.setRunning("Старт worker'а…")
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext, WorkerEntryPoint::class.java
            )
            val photoRepository = entryPoint.photoRepository()
            val photoMetadataDao = entryPoint.photoMetadataDao()
            val hashDao = entryPoint.photoHashDao()
            val pHashEngine = entryPoint.pHashEngine()

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
        } catch (e: Throwable) {
            val trace = e.stackTraceToString().take(2000)
            runCatching {
                indexingStore.setError("${e::class.simpleName}: ${e.message}\n$trace")
            }
            Result.failure()
        }
    }
}
