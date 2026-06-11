package xyz.kiurchv.cull.domain

import xyz.kiurchv.cull.data.model.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

data class GroupingSettings(
    val seriesRadiusMeters: Double = 500.0,
    val batchIntervalSeconds: Long = 10L,
    val duplicateHashThreshold: Int = 10,
) {
    fun hash(): String = "${seriesRadiusMeters}_${batchIntervalSeconds}_${duplicateHashThreshold}"
}

@Singleton
class GroupingEngine @Inject constructor() {

    /**
     * Build batches from a pre-sorted (by dateTaken ASC) list of photos belonging to one series.
     * Called in-memory, no DB access.
     */
    fun buildBatches(
        photos: List<Photo>,
        settings: GroupingSettings,
    ): List<Batch> {
        if (photos.isEmpty()) return emptyList()

        val sorted = photos.sortedBy { it.dateTaken }
        val intervalMs = settings.batchIntervalSeconds * 1000L

        // Split into time-based groups
        val rawBatches = mutableListOf<MutableList<Photo>>()
        for (photo in sorted) {
            val last = rawBatches.lastOrNull()
            if (last != null && photo.dateTaken - last.last().dateTaken <= intervalMs) {
                last.add(photo)
            } else {
                rawBatches.add(mutableListOf(photo))
            }
        }

        return rawBatches.map { batchPhotos ->
            val (standalone, grouped) = batchPhotos.partition { it.groupId == null }
            val duplicateGroups = grouped
                .groupBy { it.groupId!! }
                .map { (groupId, groupPhotos) ->
                    val bestIndex = groupPhotos.indexOfMaxBy { it.sharpness }
                    DuplicateGroup(
                        id = groupId,
                        photos = groupPhotos,
                        bestIndex = bestIndex.coerceAtLeast(0),
                    )
                }
            Batch(
                startTime = batchPhotos.first().dateTaken,
                endTime = batchPhotos.last().dateTaken,
                photos = standalone,
                duplicateGroups = duplicateGroups,
            )
        }
    }

    /**
     * Build Series objects for a single day from a list of photos belonging to that day.
     * Called lazily per page in GalleryViewModel.
     */
    fun buildSeriesForDay(
        photos: List<Photo>,
        dayEpoch: Long,
        settings: GroupingSettings,
        hashMap: Map<Long, Pair<Long, Float>>,
    ): List<Series> {
        if (photos.isEmpty()) return emptyList()

        val clusters = clusterByGeo(photos, settings.seriesRadiusMeters)
        return clusters.mapIndexed { idx, cluster ->
            val center = computeCenter(cluster)
            // Assign groupIds in-memory (don't persist here)
            val withGroups = assignGroupsInMemory(cluster, settings, hashMap)
            val batches = buildBatches(withGroups, settings)
            Series(
                id = "${dayEpoch}_$idx",
                date = dayEpoch * DAY_MS,
                centerLat = center?.first,
                centerLon = center?.second,
                locationName = null,
                batches = batches,
            )
        }
    }

    private fun assignGroupsInMemory(
        photos: List<Photo>,
        settings: GroupingSettings,
        hashMap: Map<Long, Pair<Long, Float>>,
    ): List<Photo> {
        val withHash = photos.filter { hashMap.containsKey(it.id) }
        val withoutHash = photos.filter { !hashMap.containsKey(it.id) }
        val groups = mutableListOf<MutableList<Photo>>()

        for (photo in withHash) {
            val photoHash = hashMap[photo.id]!!.first
            val added = groups.any { group ->
                val groupHash = hashMap[group.first().id]?.first ?: return@any false
                val dist = java.lang.Long.bitCount(photoHash xor photoHash.xor(groupHash))
                if (dist <= settings.duplicateHashThreshold) { group.add(photo); true } else false
            }
            if (!added) groups.add(mutableListOf(photo))
        }

        val result = mutableListOf<Photo>()
        groups.forEachIndexed { idx, group ->
            val groupId = if (group.size > 1) "g_${photos.firstOrNull()?.dateTaken}_$idx" else null
            group.forEach { result.add(it.copy(groupId = groupId)) }
        }
        result.addAll(withoutHash)
        return result
    }

    /**
     * Full grouping from scratch — used by GroupingWorker.
     * Returns: map of mediaId → (seriesId, groupId?)
     */
    fun computeAssignments(
        photos: List<Photo>,
        settings: GroupingSettings,
        hashMap: Map<Long, Pair<Long, Float>>, // mediaId → (pHash, sharpness)
    ): GroupingResult {
        if (photos.isEmpty()) return GroupingResult(emptyList(), emptyMap())

        val sorted = photos.sortedBy { it.dateTaken }
        val byDay = sorted.groupBy { it.dateTaken / DAY_MS }
        val series = mutableListOf<SeriesData>()
        val assignments = mutableMapOf<Long, Assignment>()

        byDay.forEach { (dayEpoch, dayPhotos) ->
            val clusters = clusterByGeo(dayPhotos, settings.seriesRadiusMeters)
            clusters.forEachIndexed { idx, cluster ->
                val seriesId = "${dayEpoch}_$idx"
                val center = computeCenter(cluster)
                val groupAssignments = assignDuplicateGroups(cluster, settings, hashMap, seriesId)

                series.add(SeriesData(
                    id = seriesId,
                    date = dayEpoch * DAY_MS,
                    centerLat = center?.first,
                    centerLon = center?.second,
                ))

                cluster.forEach { photo ->
                    assignments[photo.id] = Assignment(
                        seriesId = seriesId,
                        groupId = groupAssignments[photo.id],
                    )
                }
            }
        }

        return GroupingResult(series, assignments)
    }

    // ---- Geo clustering ----

    private fun clusterByGeo(photos: List<Photo>, radiusMeters: Double): List<List<Photo>> {
        val withGeo = photos.filter { it.latitude != null }
        val withoutGeo = photos.filter { it.latitude == null }
        val clusters = mutableListOf<MutableList<Photo>>()

        for (photo in withGeo) {
            val added = clusters.any { cluster ->
                val center = computeCenter(cluster) ?: return@any false
                val dist = haversineMeters(center.first, center.second, photo.latitude!!, photo.longitude!!)
                if (dist <= radiusMeters) { cluster.add(photo); true } else false
            }
            if (!added) clusters.add(mutableListOf(photo))
        }

        if (withoutGeo.isNotEmpty()) {
            if (clusters.isEmpty()) clusters.add(mutableListOf())
            clusters[0].addAll(withoutGeo)
        }

        return clusters
    }

    // ---- Duplicate detection ----

    private fun assignDuplicateGroups(
        photos: List<Photo>,
        settings: GroupingSettings,
        hashMap: Map<Long, Pair<Long, Float>>,
        seriesId: String,
    ): Map<Long, String> {
        val result = mutableMapOf<Long, String>()
        val withHash = photos.filter { hashMap.containsKey(it.id) }
        val groups = mutableListOf<MutableList<Photo>>()

        for (photo in withHash) {
            val photoHash = hashMap[photo.id]!!.first
            val added = groups.any { group ->
                val groupHash = hashMap[group.first().id]?.first ?: return@any false
                val dist = java.lang.Long.bitCount(photoHash xor groupHash)
                if (dist <= settings.duplicateHashThreshold) { group.add(photo); true } else false
            }
            if (!added) groups.add(mutableListOf(photo))
        }

        groups.filter { it.size > 1 }.forEachIndexed { idx, group ->
            val groupId = "${seriesId}_g$idx"
            group.forEach { result[it.id] = groupId }
        }

        return result
    }

    // ---- Helpers ----

    private fun computeCenter(photos: List<Photo>): Pair<Double, Double>? {
        val withGeo = photos.filter { it.latitude != null }
        if (withGeo.isEmpty()) return null
        return withGeo.map { it.latitude!! }.average() to withGeo.map { it.longitude!! }.average()
    }

    private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    private fun <T> List<T>.indexOfMaxBy(selector: (T) -> Float): Int {
        var bestIdx = 0
        var bestVal = Float.MIN_VALUE
        forEachIndexed { i, item ->
            val v = selector(item)
            if (v > bestVal) { bestVal = v; bestIdx = i }
        }
        return bestIdx
    }

    companion object {
        private const val DAY_MS = 24 * 60 * 60 * 1000L
    }
}

data class SeriesData(
    val id: String,
    val date: Long,
    val centerLat: Double?,
    val centerLon: Double?,
)

data class Assignment(
    val seriesId: String,
    val groupId: String?,
)

data class GroupingResult(
    val series: List<SeriesData>,
    val assignments: Map<Long, Assignment>, // mediaId → assignment
)


