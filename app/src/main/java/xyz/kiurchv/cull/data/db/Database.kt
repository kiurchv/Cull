package xyz.kiurchv.cull.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ---------- Entities ----------

@Entity(
    tableName = "photo_metadata",
    indices = [
        Index("seriesId"),
        Index("groupId"),
        Index("pendingDelete"),
    ]
)
data class PhotoMetadataEntity(
    @PrimaryKey val mediaId: Long,
    val dateTaken: Long,
    val seriesId: String? = null,
    val groupId: String? = null,
    val pendingDelete: Boolean = false,
)

@Entity(tableName = "series")
data class SeriesEntity(
    @PrimaryKey val id: String,
    val date: Long,
    val centerLat: Double?,
    val centerLon: Double?,
    val locationName: String?,
    val settingsHash: String,
    val updatedAt: Long = System.currentTimeMillis(),
)

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
interface PhotoMetadataDao {

    @Query("SELECT * FROM photo_metadata ORDER BY dateTaken DESC")
    fun observeAll(): Flow<List<PhotoMetadataEntity>>

    @Query("SELECT * FROM photo_metadata WHERE seriesId = :seriesId ORDER BY dateTaken ASC")
    suspend fun getBySeriesId(seriesId: String): List<PhotoMetadataEntity>

    @Query("SELECT * FROM photo_metadata WHERE seriesId = :seriesId ORDER BY dateTaken ASC")
    fun observeBySeriesId(seriesId: String): Flow<List<PhotoMetadataEntity>>

    @Query("SELECT * FROM photo_metadata WHERE groupId = :groupId ORDER BY dateTaken ASC")
    suspend fun getByGroupId(groupId: String): List<PhotoMetadataEntity>

    @Query("SELECT * FROM photo_metadata WHERE groupId = :groupId ORDER BY dateTaken ASC")
    fun observeByGroupId(groupId: String): Flow<List<PhotoMetadataEntity>>

    @Query("SELECT * FROM photo_metadata WHERE mediaId = :mediaId")
    suspend fun getById(mediaId: Long): PhotoMetadataEntity?

    @Query("SELECT * FROM photo_metadata WHERE mediaId IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<PhotoMetadataEntity>

    @Query("SELECT mediaId FROM photo_metadata")
    suspend fun getAllIds(): List<Long>

    @Query("SELECT * FROM photo_metadata WHERE pendingDelete = 1 ORDER BY dateTaken DESC")
    fun observePendingDelete(): Flow<List<PhotoMetadataEntity>>

    @Query("SELECT COUNT(*) FROM photo_metadata WHERE seriesId = :seriesId AND pendingDelete = 0")
    suspend fun countActiveInSeries(seriesId: String): Int

    @Upsert
    suspend fun upsert(entity: PhotoMetadataEntity)

    @Upsert
    suspend fun upsertAll(entities: List<PhotoMetadataEntity>)

    @Query("UPDATE photo_metadata SET pendingDelete = :pending WHERE mediaId = :mediaId")
    suspend fun setPendingDelete(mediaId: Long, pending: Boolean)

    @Query("UPDATE photo_metadata SET pendingDelete = :pending WHERE mediaId IN (:ids)")
    suspend fun setPendingDeleteBatch(ids: List<Long>, pending: Boolean)

    @Query("DELETE FROM photo_metadata WHERE mediaId = :mediaId")
    suspend fun deleteById(mediaId: Long)

    @Query("DELETE FROM photo_metadata WHERE mediaId IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM photo_metadata WHERE seriesId IS NULL")
    suspend fun deleteUnassigned()
}

@Dao
interface SeriesDao {

    @Query("SELECT * FROM series ORDER BY date DESC")
    fun observeAll(): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series ORDER BY date DESC")
    suspend fun getAll(): List<SeriesEntity>

    @Query("SELECT * FROM series WHERE id = :id")
    suspend fun getById(id: String): SeriesEntity?

    @Query("SELECT * FROM series WHERE settingsHash != :hash")
    suspend fun getWithDifferentSettings(hash: String): List<SeriesEntity>

    @Upsert
    suspend fun upsert(entity: SeriesEntity)

    @Upsert
    suspend fun upsertAll(entities: List<SeriesEntity>)

    @Query("DELETE FROM series WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM series WHERE settingsHash != :hash")
    suspend fun deleteWithDifferentSettings(hash: String)
}

@Dao
interface PhotoHashDao {

    @Query("SELECT * FROM photo_hashes")
    fun observeAll(): Flow<List<PhotoHashEntity>>

    @Query("SELECT mediaId FROM photo_hashes")
    suspend fun getAllIds(): List<Long>

    @Query("SELECT * FROM photo_hashes WHERE mediaId IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<PhotoHashEntity>

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

    @Query("DELETE FROM album_links WHERE mediaId = :mediaId")
    suspend fun deleteAllLinksForPhoto(mediaId: Long)
}

// ---------- Database ----------

@Database(
    entities = [
        PhotoMetadataEntity::class,
        SeriesEntity::class,
        PhotoHashEntity::class,
        AlbumEntity::class,
        AlbumLinkEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class CullDatabase : RoomDatabase() {
    abstract fun photoMetadataDao(): PhotoMetadataDao
    abstract fun seriesDao(): SeriesDao
    abstract fun photoHashDao(): PhotoHashDao
    abstract fun albumDao(): AlbumDao
}
