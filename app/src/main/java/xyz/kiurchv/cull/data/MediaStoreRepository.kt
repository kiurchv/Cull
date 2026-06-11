package xyz.kiurchv.cull.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.kiurchv.cull.data.model.Photo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val resolver get() = context.contentResolver

    private val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DATA,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.DATE_TAKEN,
        MediaStore.Images.Media.DATE_ADDED,
        MediaStore.Images.Media.WIDTH,
        MediaStore.Images.Media.HEIGHT,
        MediaStore.Images.Media.SIZE,
        MediaStore.Images.Media.MIME_TYPE,
        MediaStore.Images.Media.IS_FAVORITE,
        MediaStore.Images.Media.IS_TRASHED,
    )

    suspend fun loadPhotos(includeTrashed: Boolean = false): List<Photo> =
        withContext(Dispatchers.IO) {
            val photos = mutableListOf<Photo>()
            val uri = if (includeTrashed) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                    .buildUpon()
                    .appendQueryParameter(MediaStore.QUERY_ARG_MATCH_TRASHED, "1")
                    .build()
            } else {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            }

            val selection = "${MediaStore.Images.Media.DATA} LIKE ?"
            val selectionArgs = arrayOf("%/DCIM/%")
            val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

            resolver.query(uri, projection, selection, selectionArgs, sortOrder)
                ?.use { cursor -> cursor.toPhotos(photos) }

            photos
        }

    private fun Cursor.toPhotos(into: MutableList<Photo>) {
        val idCol = getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val dataCol = getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        val nameCol = getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
        val dateTakenCol = getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
        val dateAddedCol = getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
        val widthCol = getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
        val heightCol = getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
        val sizeCol = getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
        val mimeCol = getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
        val favCol = getColumnIndexOrThrow(MediaStore.Images.Media.IS_FAVORITE)
        val trashedCol = getColumnIndexOrThrow(MediaStore.Images.Media.IS_TRASHED)

        while (moveToNext()) {
            val id = getLong(idCol)
            val path = getString(dataCol) ?: continue
            val (lat, lon) = getExifLocation(path)

            into += Photo(
                id = id,
                uri = Uri.withAppendedPath(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString()
                ),
                path = path,
                displayName = getString(nameCol) ?: "",
                dateTaken = getLong(dateTakenCol),
                dateAdded = getLong(dateAddedCol) * 1000L,
                latitude = lat,
                longitude = lon,
                width = getInt(widthCol),
                height = getInt(heightCol),
                size = getLong(sizeCol),
                mimeType = getString(mimeCol) ?: "image/jpeg",
                isFavorite = getInt(favCol) == 1,
                isTrashed = getInt(trashedCol) == 1,
            )
        }
    }

    private fun getExifLocation(path: String): Pair<Double?, Double?> = try {
        val exif = androidx.exifinterface.media.ExifInterface(path)
        val latLon = FloatArray(2)
        if (exif.getLatLong(latLon)) {
            latLon[0].toDouble() to latLon[1].toDouble()
        } else {
            null to null
        }
    } catch (_: Exception) {
        null to null
    }

    suspend fun trashPhoto(id: Long) = withContext(Dispatchers.IO) {
        val uri = Uri.withAppendedPath(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString()
        )
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.IS_TRASHED, 1)
        }
        resolver.update(uri, values, null, null)
    }

    suspend fun trashPhotos(ids: List<Long>) = withContext(Dispatchers.IO) {
        ids.forEach { trashPhoto(it) }
    }

    suspend fun setFavorite(id: Long, favorite: Boolean) = withContext(Dispatchers.IO) {
        val uri = Uri.withAppendedPath(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString()
        )
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.IS_FAVORITE, if (favorite) 1 else 0)
        }
        resolver.update(uri, values, null, null)
    }
}
