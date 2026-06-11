package xyz.kiurchv.cull.data

import android.content.Context
import android.os.Environment
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.kiurchv.cull.data.db.AlbumDao
import xyz.kiurchv.cull.data.db.AlbumEntity
import xyz.kiurchv.cull.data.db.AlbumLinkEntity
import xyz.kiurchv.cull.data.model.Photo
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HardLinkManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val albumDao: AlbumDao,
) {
    private val albumsRoot: File
        get() = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            "Albums"
        )

    suspend fun createAlbum(name: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val dir = File(albumsRoot, name)
            dir.mkdirs()
            albumDao.insert(AlbumEntity(name = name, path = dir.absolutePath))
        }
    }

    suspend fun deleteAlbum(name: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val dir = File(albumsRoot, name)
            // Remove hardlinks (files in album dir) but keep originals
            dir.listFiles()?.forEach { it.delete() }
            dir.delete()
            albumDao.deleteAllLinks(name)
            albumDao.delete(name)
        }
    }

    suspend fun addPhotoToAlbum(photo: Photo, albumName: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val albumDir = File(albumsRoot, albumName)
                albumDir.mkdirs()

                val source = File(photo.path)
                val target = File(albumDir, source.name)

                // Use Os.link() for hard link
                android.system.Os.link(source.absolutePath, target.absolutePath)

                albumDao.insertLink(
                    AlbumLinkEntity(
                        albumName = albumName,
                        mediaId = photo.id,
                        hardlinkPath = target.absolutePath,
                    )
                )
            }
        }

    suspend fun removePhotoFromAlbum(photo: Photo, albumName: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val links = albumDao.getLinksForPhoto(photo.id)
                val link = links.firstOrNull { it.albumName == albumName } ?: return@runCatching
                File(link.hardlinkPath).delete()
                albumDao.deleteLink(albumName, photo.id)
            }
        }

    suspend fun getAlbumsForPhoto(photoId: Long): List<String> =
        withContext(Dispatchers.IO) {
            albumDao.getLinksForPhoto(photoId).map { it.albumName }
        }
}
