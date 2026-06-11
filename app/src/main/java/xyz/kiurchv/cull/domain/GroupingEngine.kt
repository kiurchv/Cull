package xyz.kiurchv.cull.domain

import xyz.kiurchv.cull.data.db.PhotoHashDao
import xyz.kiurchv.cull.data.model.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

data class GroupingSettings(
    val seriesRadiusMeters: Double = 500.0,
    val batchIntervalSeconds: Long = 10L,
    val duplicateHashThreshold: Int = 10,   // hamming distance
)

@Singleton
class GroupingEngine @Inject constructor(
    private val hashDao: PhotoHashDao,
    private val pHashEngine: PHashEngine,
) {
    suspend fun group(photos: List<Photo>, settings: GroupingSettings): List<Series> {
        if (photos.isEmpty()) return emptyList()

        // Load all hashes from DB
        val hashMap = hashDao.observeAll().let { flow ->
            // Collect once synchronously — called from coroutine context
            buildMap {
                hashDao.getAllIds().forEach { id -> }
            }
        }
        val dbHashes = buildMap<Long, Pair<Long, Float>> {
            // We iterate photos and match from DB in worker; here just prepare
        }

        return groupIntoSeries(photos, settings, dbHashes)
    }

    /**
     * Public entry point used by ViewModel — takes pre-loaded hash map.
     */
    fun groupSync(
        photos: List<Photo>,
        settings: GroupingSettings,
        hashMap: Map<Long, Pair<Long, Float>>, // mediaId -> (pHash, sharpness)
    ): List<Series> = groupIntoSeries(photos, settings, hashMap)

    // ---- Level 1: Series by geo + date ----

    private fun groupIntoSeries(
        photos: List<Photo>,
        settings: GroupingSettings,
        hashMap: Map<Long, Pair<Long, Float>>,
    ): List<Series> {
        // Sort by date taken
        val sorted = photos.sortedBy { it.dateTaken }

        // Group by day first, then by geo proximity
        val byDay = sorted.groupBy { photo ->
            // Truncate to day (UTC)
            photo.dateTaken / (24 * 60 * 60 * 1000L)
        }

        val series = mutableListOf<Series>()

        byDay.forEach { (dayEpoch, dayPhotos) ->
            val geoSeries = clusterByGeo(dayPhotos, settings.seriesRadiusMeters)
            geoSeries.forEachIndexed { idx, cluster ->
                val center = computeCenter(cluster)
                val batches = groupIntoBatches(cluster, settings, hashMap)
                series += Series(
                    id = "${dayEpoch}_${idx}",
                    centerLat = center?.first,
                    centerLon = center?.second,
                    date = dayEpoch * 24 * 60 * 60 * 1000L,
                    radiusMeters = settings.seriesRadiusMeters,
                    batches = batches,
                )
            }
        }

        return series.sortedByDescending { it.date }
    }

    private fun clusterByGeo(photos: List<Photo>, radiusMeters: Double): List<List<Photo>> {
        val withGeo = photos.filter { it.latitude != null && it.longitude != null }
        val withoutGeo = photos.filter { it.latitude == null || it.longitude == null }

        val clusters = mutableListOf<MutableList<Photo>>()

        for (photo in withGeo) {
            val added = clusters.any { cluster ->
                val center = computeCenter(cluster) ?: return@any false
                val dist = haversineMeters(
                    center.first, center.second,
                    photo.latitude!!, photo.longitude!!
                )
                if (dist <= radiusMeters) {
                    cluster += photo
                    true
                } else false
            }
            if (!added) clusters += mutableListOf(photo)
        }

        // Photos without geo go into their own cluster (or merge into single-cluster if only one)
        if (withoutGeo.isNotEmpty()) {
            if (clusters.isEmpty()) clusters += mutableListOf()
            clusters.first() += withoutGeo
        }

        return clusters
    }

    // ---- Level 2: Batches by time ----

    private fun groupIntoBatches(
        photos: List<Photo>,
        settings: GroupingSettings,
        hashMap: Map<Long, Pair<Long, Float>>,
    ): List<Batch> {
        val sorted = photos.sortedBy { it.dateTaken }
        val batches = mutableListOf<MutableList<Photo>>()
        val intervalMs = settings.batchIntervalSeconds * 1000L

        for (photo in sorted) {
            val added = batches.lastOrNull()?.let { batch ->
                val lastTime = batch.last().dateTaken
                if (photo.dateTaken - lastTime <= intervalMs) {
                    batch += photo
                    true
                } else false
            } ?: false
            if (!added) batches += mutableListOf(photo)
        }

        return batches.mapIndexed { idx, batchPhotos ->
            Batch(
                id = "batch_${idx}",
                startTime = batchPhotos.first().dateTaken,
                endTime = batchPhotos.last().dateTaken,
                groups = groupDuplicates(batchPhotos, settings, hashMap),
            )
        }
    }

    // ---- Level 3: Duplicate groups by pHash ----

    private fun groupDuplicates(
        photos: List<Photo>,
        settings: GroupingSettings,
        hashMap: Map<Long, Pair<Long, Float>>,
    ): List<DuplicateGroup> {
        if (photos.size == 1) {
            return listOf(DuplicateGroup(id = photos[0].id.toString(), photos = photos))
        }

        // Only cluster if we have hashes
        val withHash = photos.filter { hashMap.containsKey(it.id) }
        val withoutHash = photos.filter { !hashMap.containsKey(it.id) }

        val groups = mutableListOf<MutableList<Photo>>()

        for (photo in withHash) {
            val photoHash = hashMap[photo.id]!!.first
            val added = groups.any { group ->
                val groupHash = hashMap[group.first().id]?.first ?: return@any false
                val dist = java.lang.Long.bitCount(photoHash xor groupHash)
                if (dist <= settings.duplicateHashThreshold) {
                    group += photo
                    true
                } else false
            }
            if (!added) groups += mutableListOf(photo)
        }

        // Photos without hash each get their own group
        withoutHash.forEach { groups += mutableListOf(it) }

        return groups.mapIndexed { idx, groupPhotos ->
            val bestIndex = findBestPhoto(groupPhotos, hashMap)
            DuplicateGroup(
                id = "group_${idx}_${groupPhotos.first().id}",
                photos = groupPhotos,
                bestIndex = bestIndex,
            )
        }
    }

    private fun findBestPhoto(
        photos: List<Photo>,
        hashMap: Map<Long, Pair<Long, Float>>,
    ): Int {
        if (photos.size == 1) return 0
        // Best = highest sharpness score
        var bestIdx = 0
        var bestSharpness = -1f
        photos.forEachIndexed { idx, photo ->
            val sharpness = hashMap[photo.id]?.second ?: 0f
            if (sharpness > bestSharpness) {
                bestSharpness = sharpness
                bestIdx = idx
            }
        }
        return bestIdx
    }

    // ---- Geo helpers ----

    private fun computeCenter(photos: List<Photo>): Pair<Double, Double>? {
        val withGeo = photos.filter { it.latitude != null && it.longitude != null }
        if (withGeo.isEmpty()) return null
        return withGeo.map { it.latitude!! }.average() to
                withGeo.map { it.longitude!! }.average()
    }

    private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}
