package xyz.kiurchv.cull.`data`.db

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AlbumDao_Impl(
  __db: RoomDatabase,
) : AlbumDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfAlbumEntity: EntityInsertAdapter<AlbumEntity>

  private val __insertAdapterOfAlbumLinkEntity: EntityInsertAdapter<AlbumLinkEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfAlbumEntity = object : EntityInsertAdapter<AlbumEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR IGNORE INTO `albums` (`name`,`path`,`createdAt`) VALUES (?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: AlbumEntity) {
        statement.bindText(1, entity.name)
        statement.bindText(2, entity.path)
        statement.bindLong(3, entity.createdAt)
      }
    }
    this.__insertAdapterOfAlbumLinkEntity = object : EntityInsertAdapter<AlbumLinkEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR IGNORE INTO `album_links` (`albumName`,`mediaId`,`hardlinkPath`) VALUES (?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: AlbumLinkEntity) {
        statement.bindText(1, entity.albumName)
        statement.bindLong(2, entity.mediaId)
        statement.bindText(3, entity.hardlinkPath)
      }
    }
  }

  public override suspend fun insert(album: AlbumEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __insertAdapterOfAlbumEntity.insert(_connection, album)
  }

  public override suspend fun insertLink(link: AlbumLinkEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfAlbumLinkEntity.insert(_connection, link)
  }

  public override fun observeAll(): Flow<List<AlbumEntity>> {
    val _sql: String = "SELECT * FROM albums ORDER BY name ASC"
    return createFlow(__db, false, arrayOf("albums")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfPath: Int = getColumnIndexOrThrow(_stmt, "path")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<AlbumEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: AlbumEntity
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpPath: String
          _tmpPath = _stmt.getText(_columnIndexOfPath)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item = AlbumEntity(_tmpName,_tmpPath,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByName(name: String): AlbumEntity? {
    val _sql: String = "SELECT * FROM albums WHERE name = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, name)
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfPath: Int = getColumnIndexOrThrow(_stmt, "path")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: AlbumEntity?
        if (_stmt.step()) {
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpPath: String
          _tmpPath = _stmt.getText(_columnIndexOfPath)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result = AlbumEntity(_tmpName,_tmpPath,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeLinks(albumName: String): Flow<List<AlbumLinkEntity>> {
    val _sql: String = "SELECT * FROM album_links WHERE albumName = ?"
    return createFlow(__db, false, arrayOf("album_links")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, albumName)
        val _columnIndexOfAlbumName: Int = getColumnIndexOrThrow(_stmt, "albumName")
        val _columnIndexOfMediaId: Int = getColumnIndexOrThrow(_stmt, "mediaId")
        val _columnIndexOfHardlinkPath: Int = getColumnIndexOrThrow(_stmt, "hardlinkPath")
        val _result: MutableList<AlbumLinkEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: AlbumLinkEntity
          val _tmpAlbumName: String
          _tmpAlbumName = _stmt.getText(_columnIndexOfAlbumName)
          val _tmpMediaId: Long
          _tmpMediaId = _stmt.getLong(_columnIndexOfMediaId)
          val _tmpHardlinkPath: String
          _tmpHardlinkPath = _stmt.getText(_columnIndexOfHardlinkPath)
          _item = AlbumLinkEntity(_tmpAlbumName,_tmpMediaId,_tmpHardlinkPath)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLinksForPhoto(mediaId: Long): List<AlbumLinkEntity> {
    val _sql: String = "SELECT * FROM album_links WHERE mediaId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, mediaId)
        val _columnIndexOfAlbumName: Int = getColumnIndexOrThrow(_stmt, "albumName")
        val _columnIndexOfMediaId: Int = getColumnIndexOrThrow(_stmt, "mediaId")
        val _columnIndexOfHardlinkPath: Int = getColumnIndexOrThrow(_stmt, "hardlinkPath")
        val _result: MutableList<AlbumLinkEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: AlbumLinkEntity
          val _tmpAlbumName: String
          _tmpAlbumName = _stmt.getText(_columnIndexOfAlbumName)
          val _tmpMediaId: Long
          _tmpMediaId = _stmt.getLong(_columnIndexOfMediaId)
          val _tmpHardlinkPath: String
          _tmpHardlinkPath = _stmt.getText(_columnIndexOfHardlinkPath)
          _item = AlbumLinkEntity(_tmpAlbumName,_tmpMediaId,_tmpHardlinkPath)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun countPhotosInAlbum(albumName: String): Int {
    val _sql: String = "SELECT COUNT(*) FROM album_links WHERE albumName = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, albumName)
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeMediaIdsInAlbum(albumName: String): Flow<List<Long>> {
    val _sql: String = "SELECT mediaId FROM album_links WHERE albumName = ? ORDER BY mediaId DESC"
    return createFlow(__db, false, arrayOf("album_links")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, albumName)
        val _result: MutableList<Long> = mutableListOf()
        while (_stmt.step()) {
          val _item: Long
          _item = _stmt.getLong(0)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLinksForAlbum(albumName: String): List<AlbumLinkEntity> {
    val _sql: String = "SELECT * FROM album_links WHERE albumName = ? ORDER BY mediaId DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, albumName)
        val _columnIndexOfAlbumName: Int = getColumnIndexOrThrow(_stmt, "albumName")
        val _columnIndexOfMediaId: Int = getColumnIndexOrThrow(_stmt, "mediaId")
        val _columnIndexOfHardlinkPath: Int = getColumnIndexOrThrow(_stmt, "hardlinkPath")
        val _result: MutableList<AlbumLinkEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: AlbumLinkEntity
          val _tmpAlbumName: String
          _tmpAlbumName = _stmt.getText(_columnIndexOfAlbumName)
          val _tmpMediaId: Long
          _tmpMediaId = _stmt.getLong(_columnIndexOfMediaId)
          val _tmpHardlinkPath: String
          _tmpHardlinkPath = _stmt.getText(_columnIndexOfHardlinkPath)
          _item = AlbumLinkEntity(_tmpAlbumName,_tmpMediaId,_tmpHardlinkPath)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(name: String) {
    val _sql: String = "DELETE FROM albums WHERE name = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, name)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteLink(albumName: String, mediaId: Long) {
    val _sql: String = "DELETE FROM album_links WHERE albumName = ? AND mediaId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, albumName)
        _argIndex = 2
        _stmt.bindLong(_argIndex, mediaId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAllLinks(albumName: String) {
    val _sql: String = "DELETE FROM album_links WHERE albumName = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, albumName)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAllLinksForPhoto(mediaId: Long) {
    val _sql: String = "DELETE FROM album_links WHERE mediaId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, mediaId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
