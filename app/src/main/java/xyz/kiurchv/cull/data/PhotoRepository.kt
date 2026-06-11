package xyz.kiurchv.cull.data

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.room.withTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.kiurchv.cull.data.db.*
import xyz.kiurchv.cull.data.model.Photo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: CullDatabase,
    private val hardLinkManager: HardLinkManager,
) {
    private val resolver get() = context.contentResolver

    // ---- MediaStore reading ----

    suspend fun loadPhotosFromMediaStore(): List<Photo> = withContext(Dispatchers.IO) {
        val photos = mutableListOf<Photo>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.IS_FAVORITE,
        )
        val uri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val selection = "${MediaStore.Images.Media.DATA} LIKE ? AND ${MediaStore.Images.Media.IS_TRASHED} = 0"
        val selectionArgs = arrayOf("%/DCIM/%")
        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        resolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateTakenCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
            val favCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.IS_FAVORITE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val path = cursor.getString(dataCol) ?: continue
                val (lat, lon) = readExifLatLon(path)
                photos += Photo(
                    id = id,
                    uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString()),
                    path = path,
                    displayName = cursor.getString(nameCol) ?: "",
                    dateTaken = cursor.getLong(dateTakenCol),
                    width = cursor.getInt(widthCol),
                    height = cursor.getInt(heightCol),
                    size = cursor.getLong(sizeCol),
                    mimeType = cursor.getString(mimeCol) ?: "image/jpeg",
                    isFavorite = cursor.getInt(favCol) == 1,
                    latitude = lat,
                    longitude = lon,
                )
            }
        }
        photos
    }

    // ---- Favorites ----

    /**
     * Batch-load all display fields for a list of mediaIds — one MediaStore query.
     * Returns map of mediaId -> MediaStoreData.
     */
    data class MediaStoreData(
        val path: String,
        val displayName: String,
        val width: Int,
        val height: Int,
        val size: Long,
        val mimeType: String,
        val isFavorite: Boolean,
    )

    suspend fun loadMediaStoreData(mediaIds: List<Long>): Map<Long, MediaStoreData> =
        withContext(Dispatchers.IO) {
            if (mediaIds.isEmpty()) return@withContext emptyMap()
            val result = mutableMapOf<Long, MediaStoreData>()
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.IS_FAVORITE,
            )
            val placeholders = mediaIds.joinToString(",") { "?" }
            val selection = "${MediaStore.Images.Media._ID} IN ($placeholders)"
            val selectionArgs = mediaIds.map { it.toString() }.toTypedArray()

            resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, selection, selectionArgs, null
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
                val favCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.IS_FAVORITE)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    result[id] = MediaStoreData(
                        path = cursor.getString(dataCol) ?: "",
                        displayName = cursor.getString(nameCol) ?: "",
                        width = cursor.getInt(widthCol),
                        height = cursor.getInt(heightCol),
                        size = cursor.getLong(sizeCol),
                        mimeType = cursor.getString(mimeCol) ?: "image/jpeg",
                        isFavorite = cursor.getInt(favCol) == 1,
                    )
                }
            }
            result
        }

    private fun readExifLatLon(path: String): Pair<Double?, Double?> = try {
        val exif = androidx.exifinterface.media.ExifInterface(path)
        val latLon = FloatArray(2)
        if (exif.getLatLong(latLon)) latLon[0].toDouble() to latLon[1].toDouble()
        else null to null
    } catch (_: Exception) { null to null }

    suspend fun setFavorite(mediaId: Long, favorite: Boolean) = withContext(Dispatchers.IO) {
        val uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mediaId.toString())
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.IS_FAVORITE, if (favorite) 1 else 0)
        }
        resolver.update(uri, values, null, null)
    }

    // ---- Pending delete ----

    suspend fun setPendingDelete(mediaId: Long, pending: Boolean) {
        db.photoMetadataDao().setPendingDelete(mediaId, pending)
    }

    suspend fun setPendingDeleteBatch(ids: List<Long>, pending: Boolean) {
        db.photoMetadataDao().setPendingDeleteBatch(ids, pending)
    }

    fun observePendingDelete() = db.photoMetadataDao().observePendingDelete()

    // ---- Permanent delete (transactional) ----

    suspend fun deletePhoto(mediaId: Long) = withContext(Dispatchers.IO) {
        db.withTransaction {
            // 1. Remove hardlinks from albums
            val links = db.albumDao().getLinksForPhoto(mediaId)
            links.forEach { java.io.File(it.hardlinkPath).delete() }
            db.albumDao().deleteAllLinksForPhoto(mediaId)

            // 2. Remove from DB
            db.photoMetadataDao().deleteById(mediaId)
            db.photoHashDao().deleteById(mediaId)

            // 3. Check if series became empty
            val meta = db.photoMetadataDao()
            // seriesId was already deleted, check via a separate query isn't needed —
            // series cleanup handled by GroupingWorker on next run
        }

        // 4. Move to MediaStore trash
        val uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mediaId.toString())
        val values = ContentValues().apply { put(MediaStore.Images.Media.IS_TRASHED, 1) }
        resolver.update(uri, values, null, null)
    }

    suspend fun deletePhotos(ids: List<Long>) {
        ids.forEach { deletePhoto(it) }
    }
}
