package xyz.kiurchv.cull.`data`.db

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class CullDatabase_Impl : CullDatabase() {
  private val _photoMetadataDao: Lazy<PhotoMetadataDao> = lazy {
    PhotoMetadataDao_Impl(this)
  }

  private val _photoHashDao: Lazy<PhotoHashDao> = lazy {
    PhotoHashDao_Impl(this)
  }

  private val _albumDao: Lazy<AlbumDao> = lazy {
    AlbumDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(3,
        "fb864a347888bcc012b64bb7fb4495d5", "3ccbcd5dbac177fb9686d21f49ee7b73") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `photo_metadata` (`mediaId` INTEGER NOT NULL, `dateTaken` INTEGER NOT NULL, `groupId` TEXT, `pendingDelete` INTEGER NOT NULL, PRIMARY KEY(`mediaId`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_photo_metadata_dateTaken` ON `photo_metadata` (`dateTaken`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_photo_metadata_groupId` ON `photo_metadata` (`groupId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_photo_metadata_pendingDelete` ON `photo_metadata` (`pendingDelete`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `photo_hashes` (`mediaId` INTEGER NOT NULL, `path` TEXT NOT NULL, `pHash` INTEGER NOT NULL, `sharpness` REAL NOT NULL, `indexedAt` INTEGER NOT NULL, PRIMARY KEY(`mediaId`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `albums` (`name` TEXT NOT NULL, `path` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`name`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `album_links` (`albumName` TEXT NOT NULL, `mediaId` INTEGER NOT NULL, `hardlinkPath` TEXT NOT NULL, PRIMARY KEY(`albumName`, `mediaId`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'fb864a347888bcc012b64bb7fb4495d5')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `photo_metadata`")
        connection.execSQL("DROP TABLE IF EXISTS `photo_hashes`")
        connection.execSQL("DROP TABLE IF EXISTS `albums`")
        connection.execSQL("DROP TABLE IF EXISTS `album_links`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsPhotoMetadata: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsPhotoMetadata.put("mediaId", TableInfo.Column("mediaId", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPhotoMetadata.put("dateTaken", TableInfo.Column("dateTaken", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPhotoMetadata.put("groupId", TableInfo.Column("groupId", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPhotoMetadata.put("pendingDelete", TableInfo.Column("pendingDelete", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysPhotoMetadata: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesPhotoMetadata: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesPhotoMetadata.add(TableInfo.Index("index_photo_metadata_dateTaken", false,
            listOf("dateTaken"), listOf("ASC")))
        _indicesPhotoMetadata.add(TableInfo.Index("index_photo_metadata_groupId", false,
            listOf("groupId"), listOf("ASC")))
        _indicesPhotoMetadata.add(TableInfo.Index("index_photo_metadata_pendingDelete", false,
            listOf("pendingDelete"), listOf("ASC")))
        val _infoPhotoMetadata: TableInfo = TableInfo("photo_metadata", _columnsPhotoMetadata,
            _foreignKeysPhotoMetadata, _indicesPhotoMetadata)
        val _existingPhotoMetadata: TableInfo = read(connection, "photo_metadata")
        if (!_infoPhotoMetadata.equals(_existingPhotoMetadata)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |photo_metadata(xyz.kiurchv.cull.data.db.PhotoMetadataEntity).
              | Expected:
              |""".trimMargin() + _infoPhotoMetadata + """
              |
              | Found:
              |""".trimMargin() + _existingPhotoMetadata)
        }
        val _columnsPhotoHashes: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsPhotoHashes.put("mediaId", TableInfo.Column("mediaId", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPhotoHashes.put("path", TableInfo.Column("path", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPhotoHashes.put("pHash", TableInfo.Column("pHash", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPhotoHashes.put("sharpness", TableInfo.Column("sharpness", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPhotoHashes.put("indexedAt", TableInfo.Column("indexedAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysPhotoHashes: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesPhotoHashes: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoPhotoHashes: TableInfo = TableInfo("photo_hashes", _columnsPhotoHashes,
            _foreignKeysPhotoHashes, _indicesPhotoHashes)
        val _existingPhotoHashes: TableInfo = read(connection, "photo_hashes")
        if (!_infoPhotoHashes.equals(_existingPhotoHashes)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |photo_hashes(xyz.kiurchv.cull.data.db.PhotoHashEntity).
              | Expected:
              |""".trimMargin() + _infoPhotoHashes + """
              |
              | Found:
              |""".trimMargin() + _existingPhotoHashes)
        }
        val _columnsAlbums: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsAlbums.put("name", TableInfo.Column("name", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsAlbums.put("path", TableInfo.Column("path", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsAlbums.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysAlbums: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesAlbums: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoAlbums: TableInfo = TableInfo("albums", _columnsAlbums, _foreignKeysAlbums,
            _indicesAlbums)
        val _existingAlbums: TableInfo = read(connection, "albums")
        if (!_infoAlbums.equals(_existingAlbums)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |albums(xyz.kiurchv.cull.data.db.AlbumEntity).
              | Expected:
              |""".trimMargin() + _infoAlbums + """
              |
              | Found:
              |""".trimMargin() + _existingAlbums)
        }
        val _columnsAlbumLinks: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsAlbumLinks.put("albumName", TableInfo.Column("albumName", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsAlbumLinks.put("mediaId", TableInfo.Column("mediaId", "INTEGER", true, 2, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsAlbumLinks.put("hardlinkPath", TableInfo.Column("hardlinkPath", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysAlbumLinks: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesAlbumLinks: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoAlbumLinks: TableInfo = TableInfo("album_links", _columnsAlbumLinks,
            _foreignKeysAlbumLinks, _indicesAlbumLinks)
        val _existingAlbumLinks: TableInfo = read(connection, "album_links")
        if (!_infoAlbumLinks.equals(_existingAlbumLinks)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |album_links(xyz.kiurchv.cull.data.db.AlbumLinkEntity).
              | Expected:
              |""".trimMargin() + _infoAlbumLinks + """
              |
              | Found:
              |""".trimMargin() + _existingAlbumLinks)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "photo_metadata",
        "photo_hashes", "albums", "album_links")
  }

  public override fun clearAllTables() {
    super.performClear(false, "photo_metadata", "photo_hashes", "albums", "album_links")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(PhotoMetadataDao::class, PhotoMetadataDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(PhotoHashDao::class, PhotoHashDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(AlbumDao::class, AlbumDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun photoMetadataDao(): PhotoMetadataDao = _photoMetadataDao.value

  public override fun photoHashDao(): PhotoHashDao = _photoHashDao.value

  public override fun albumDao(): AlbumDao = _albumDao.value
}
