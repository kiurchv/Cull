package xyz.kiurchv.cull.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ---------- Entities ----------

@Entity(
    tableName = "photo_metadata",
    indices = [
        Index("dateTaken"),
        Index("groupId"),
        Index("pendingDelete"),
    ]
)
data class PhotoMetadataEntity(
    @PrimaryKey val mediaId: Long,
    val dateTaken: Long,
    val groupId: String? = null,
    val pendingDelete: Boolean = false,
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

    @Query("SELECT DISTINCT dateTaken / 86400000 as day FROM photo_metadata ORDER BY day DESC")
    suspend fun getDistinctDays(): List<Long>

    @Query("SELECT * FROM photo_metadata WHERE dateTaken / 86400000 = :dayEpoch ORDER BY dateTaken ASC")
    suspend fun getByDay(dayEpoch: Long): List<PhotoMetadataEntity>

    @Query("SELECT * FROM photo_metadata WHERE dateTaken / 86400000 IN (:dayEpochs) ORDER BY dateTaken ASC")
    suspend fun getByDays(dayEpochs: List<Long>): List<PhotoMetadataEntity>

    @Query("SELECT * FROM photo_metadata WHERE mediaId = :mediaId")
    suspend fun getById(mediaId: Long): PhotoMetadataEntity?

    @Query("SELECT * FROM photo_metadata WHERE mediaId IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<PhotoMetadataEntity>

    @Query("SELECT mediaId FROM photo_metadata")
    suspend fun getAllIds(): List<Long>

    @Query("SELECT * FROM photo_metadata WHERE groupId = :groupId ORDER BY dateTaken ASC")
    fun observeByGroupId(groupId: String): Flow<List<PhotoMetadataEntity>>

    @Query("SELECT * FROM photo_metadata WHERE pendingDelete = 1 ORDER BY dateTaken DESC")
    fun observePendingDelete(): Flow<List<PhotoMetadataEntity>>

    @Query("SELECT MAX(dateTaken) FROM photo_metadata")
    suspend fun getLatestDateTaken(): Long?

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
}

@Dao
interface PhotoHashDao {

    @Query("SELECT * FROM photo_hashes")
    fun observeAll(): Flow<List<PhotoHashEntity>>

    @Query("SELECT mediaId FROM photo_hashes")
    suspend fun getAllIds(): List<Long>

    @Query("SELECT * FROM photo_hashes WHERE mediaId IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<PhotoHashEntity>

    @Query("SELECT mediaId FROM photo_hashes WHERE mediaId IN (:ids)")
    suspend fun getIndexedIds(ids: List<Long>): List<Long>

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

    @Query("SELECT COUNT(*) FROM album_links WHERE albumName = :albumName")
    suspend fun countPhotosInAlbum(albumName: String): Int

    @Query("SELECT mediaId FROM album_links WHERE albumName = :albumName ORDER BY mediaId DESC")
    fun observeMediaIdsInAlbum(albumName: String): Flow<List<Long>>

    @Query("SELECT * FROM album_links WHERE albumName = :albumName ORDER BY mediaId DESC")
    suspend fun getLinksForAlbum(albumName: String): List<AlbumLinkEntity>

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
        PhotoHashEntity::class,
        AlbumEntity::class,
        AlbumLinkEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class CullDatabase : RoomDatabase() {
    abstract fun photoMetadataDao(): PhotoMetadataDao
    abstract fun photoHashDao(): PhotoHashDao
    abstract fun albumDao(): AlbumDao
}
