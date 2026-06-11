package xyz.kiurchv.cull.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ---------- Entities ----------

@Entity(tableName = "photo_hashes")
data class PhotoHashEntity(
    @PrimaryKey val mediaId: Long,
    val path: String,
    val pHash: Long,
    val sharpness: Float,
    val indexedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey val name: String,
    val path: String,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "album_links",
    primaryKeys = ["albumName", "mediaId"],
)
data class AlbumLinkEntity(
    val albumName: String,
    val mediaId: Long,
    val hardlinkPath: String,
)

// ---------- DAOs ----------

@Dao
interface PhotoHashDao {

    @Query("SELECT * FROM photo_hashes")
    fun observeAll(): Flow<List<PhotoHashEntity>>

    @Query("SELECT * FROM photo_hashes WHERE mediaId = :id")
    suspend fun getById(id: Long): PhotoHashEntity?

    @Query("SELECT mediaId FROM photo_hashes")
    suspend fun getAllIds(): List<Long>

    @Upsert
    suspend fun upsert(entity: PhotoHashEntity)

    @Upsert
    suspend fun upsertAll(entities: List<PhotoHashEntity>)

    @Query("DELETE FROM photo_hashes WHERE mediaId = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM photo_hashes WHERE mediaId IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}

@Dao
interface AlbumDao {

    @Query("SELECT * FROM albums ORDER BY name ASC")
    fun observeAll(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE name = :name")
    suspend fun getByName(name: String): AlbumEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(album: AlbumEntity)

    @Query("DELETE FROM albums WHERE name = :name")
    suspend fun delete(name: String)

    @Query("SELECT * FROM album_links WHERE albumName = :albumName")
    fun observeLinks(albumName: String): Flow<List<AlbumLinkEntity>>

    @Query("SELECT * FROM album_links WHERE mediaId = :mediaId")
    suspend fun getLinksForPhoto(mediaId: Long): List<AlbumLinkEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLink(link: AlbumLinkEntity)

    @Query("DELETE FROM album_links WHERE albumName = :albumName AND mediaId = :mediaId")
    suspend fun deleteLink(albumName: String, mediaId: Long)

    @Query("DELETE FROM album_links WHERE albumName = :albumName")
    suspend fun deleteAllLinks(albumName: String)
}

// ---------- Database ----------

@Database(
    entities = [PhotoHashEntity::class, AlbumEntity::class, AlbumLinkEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class CullDatabase : RoomDatabase() {
    abstract fun photoHashDao(): PhotoHashDao
    abstract fun albumDao(): AlbumDao
}
