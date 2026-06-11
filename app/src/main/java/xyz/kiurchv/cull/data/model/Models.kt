package xyz.kiurchv.cull.data.model

import android.net.Uri

data class Photo(
    val id: Long,
    val uri: Uri,
    val path: String,
    val displayName: String,
    val dateTaken: Long,       // epoch millis
    val dateAdded: Long,       // epoch millis
    val latitude: Double?,
    val longitude: Double?,
    val width: Int,
    val height: Int,
    val size: Long,
    val mimeType: String,
    val isFavorite: Boolean,
    val isTrashed: Boolean,
    val pHash: Long? = null,   // populated after indexing
)

data class Series(
    val id: String,            // composed key: "lat_lon_date" or "date_only"
    val centerLat: Double?,
    val centerLon: Double?,
    val date: Long,            // day epoch millis
    val radiusMeters: Double,
    val batches: List<Batch>,
) {
    val photoCount: Int get() = batches.sumOf { it.groups.sumOf { g -> g.photos.size } }
}

data class Batch(
    val id: String,
    val startTime: Long,
    val endTime: Long,
    val groups: List<DuplicateGroup>,
) {
    val photoCount: Int get() = groups.sumOf { it.photos.size }
}

data class DuplicateGroup(
    val id: String,
    val photos: List<Photo>,
    val bestIndex: Int = 0,    // index of best photo in group (sharpness heuristic)
) {
    val best: Photo get() = photos[bestIndex]
    val isDuplicate: Boolean get() = photos.size > 1
}

data class Album(
    val name: String,
    val path: String,          // absolute path to DCIM/Albums/<name>
    val photoCount: Int,
)
