package xyz.kiurchv.cull.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.kiurchv.cull.data.PhotoRepository
import xyz.kiurchv.cull.data.db.*
import xyz.kiurchv.cull.domain.GroupingEngine
import xyz.kiurchv.cull.domain.GroupingSettings
import java.util.concurrent.TimeUnit

@HiltWorker
class GroupingWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val photoRepository: PhotoRepository,
    private val photoMetadataDao: PhotoMetadataDao,
    private val seriesDao: SeriesDao,
    private val photoHashDao: PhotoHashDao,
    private val groupingEngine: GroupingEngine,
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "photo_grouping"
        const val KEY_SETTINGS_HASH = "settings_hash"
        const val KEY_RADIUS = "radius"
        const val KEY_BATCH_INTERVAL = "batch_interval"
        const val KEY_HASH_THRESHOLD = "hash_threshold"

        fun buildRequest(settings: GroupingSettings): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<GroupingWorker>()
                .setInputData(
                    workDataOf(
                        KEY_SETTINGS_HASH to settings.hash(),
                        KEY_RADIUS to settings.seriesRadiusMeters,
                        KEY_BATCH_INTERVAL to settings.batchIntervalSeconds,
                        KEY_HASH_THRESHOLD to settings.duplicateHashThreshold,
                    )
                )
                .build()
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val settingsHash = inputData.getString(KEY_SETTINGS_HASH) ?: return@withContext Result.failure()
        val settings = GroupingSettings(
            seriesRadiusMeters = inputData.getDouble(KEY_RADIUS, 500.0),
            batchIntervalSeconds = inputData.getLong(KEY_BATCH_INTERVAL, 10L),
            duplicateHashThreshold = inputData.getInt(KEY_HASH_THRESHOLD, 10),
        )

        // Load photos + hashes
        val photos = photoRepository.loadPhotosFromMediaStore()
        if (photos.isEmpty()) return@withContext Result.success()

        val hashEntities = photoHashDao.getByIds(photos.map { it.id })
        val hashMap = hashEntities.associate { it.mediaId to (it.pHash to it.sharpness) }

        // Compute grouping
        val result = groupingEngine.computeAssignments(photos, settings, hashMap)

        // Persist to DB
        // 1. Delete series with different settings
        seriesDao.deleteWithDifferentSettings(settingsHash)

        // 2. Upsert new series
        seriesDao.upsertAll(result.series.map { s ->
            SeriesEntity(
                id = s.id,
                date = s.date,
                centerLat = s.centerLat,
                centerLon = s.centerLon,
                locationName = null, // geocoding done separately
                settingsHash = settingsHash,
            )
        })

        // 3. Update photo_metadata assignments
        val photoDateMap = photos.associate { it.id to it.dateTaken }
        result.assignments.entries
            .chunked(500)
            .forEach { chunk ->
                photoMetadataDao.upsertAll(
                    chunk.map { (mediaId, assignment) ->
                        PhotoMetadataEntity(
                            mediaId = mediaId,
                            dateTaken = photoDateMap[mediaId] ?: 0L,
                            seriesId = assignment.seriesId,
                            groupId = assignment.groupId,
                        )
                    }
                )
            }

        Result.success()
    }
}
