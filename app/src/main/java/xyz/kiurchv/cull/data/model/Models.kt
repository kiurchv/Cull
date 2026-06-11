package xyz.kiurchv.cull.data.model

import android.net.Uri

// ---- MediaStore photo (loaded fresh from MediaStore) ----
data class Photo(
    val id: Long,
    val uri: Uri,
    val path: String,
    val displayName: String,
    val dateTaken: Long,
    val width: Int,
    val height: Int,
    val size: Long,
    val mimeType: String,
    val isFavorite: Boolean,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val pHash: Long? = null,
    val sharpness: Float = 0f,
    val pendingDelete: Boolean = false,
    val seriesId: String? = null,
    val groupId: String? = null,
)

// ---- Series (from Room cache) ----
data class Series(
    val id: String,
    val date: Long,
    val centerLat: Double?,
    val centerLon: Double?,
    val locationName: String?,
    val batches: List<Batch> = emptyList(),
) {
    val photoCount: Int get() = batches.sumOf { it.photos.size + it.duplicateGroups.sumOf { g -> g.photos.size } }
    val pendingDeleteCount: Int get() = batches.sumOf { b ->
        b.photos.count { it.pendingDelete } +
        b.duplicateGroups.sumOf { g -> g.photos.count { it.pendingDelete } }
    }
}

// ---- Batch (computed in-memory from sorted photos) ----
data class Batch(
    val startTime: Long,
    val endTime: Long,
    val photos: List<Photo>,                    // standalone photos
    val duplicateGroups: List<DuplicateGroup>,  // grouped duplicates
) {
    val totalCount: Int get() = photos.size + duplicateGroups.sumOf { it.photos.size }
}

// ---- Duplicate group ----
data class DuplicateGroup(
    val id: String,
    val photos: List<Photo>,
    val bestIndex: Int = 0,
) {
    val best: Photo get() = photos[bestIndex]
}

// ---- Album ----
data class Album(
    val name: String,
    val path: String,
    val photoCount: Int = 0,
)
