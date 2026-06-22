package xyz.kiurchv.cull.`data`.db

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlin.text.StringBuilder
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class PhotoMetadataDao_Impl(
  __db: RoomDatabase,
) : PhotoMetadataDao {
  private val __db: RoomDatabase

  private val __upsertAdapterOfPhotoMetadataEntity: EntityUpsertAdapter<PhotoMetadataEntity>
  init {
    this.__db = __db
    this.__upsertAdapterOfPhotoMetadataEntity = EntityUpsertAdapter<PhotoMetadataEntity>(object :
        EntityInsertAdapter<PhotoMetadataEntity>() {
      protected override fun createQuery(): String =
          "INSERT INTO `photo_metadata` (`mediaId`,`dateTaken`,`groupId`,`pendingDelete`) VALUES (?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PhotoMetadataEntity) {
        statement.bindLong(1, entity.mediaId)
        statement.bindLong(2, entity.dateTaken)
        val _tmpGroupId: String? = entity.groupId
        if (_tmpGroupId == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmpGroupId)
        }
        val _tmp: Int = if (entity.pendingDelete) 1 else 0
        statement.bindLong(4, _tmp.toLong())
      }
    }, object : EntityDeleteOrUpdateAdapter<PhotoMetadataEntity>() {
      protected override fun createQuery(): String =
          "UPDATE `photo_metadata` SET `mediaId` = ?,`dateTaken` = ?,`groupId` = ?,`pendingDelete` = ? WHERE `mediaId` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: PhotoMetadataEntity) {
        statement.bindLong(1, entity.mediaId)
        statement.bindLong(2, entity.dateTaken)
        val _tmpGroupId: String? = entity.groupId
        if (_tmpGroupId == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmpGroupId)
        }
        val _tmp: Int = if (entity.pendingDelete) 1 else 0
        statement.bindLong(4, _tmp.toLong())
        statement.bindLong(5, entity.mediaId)
      }
    })
  }

  public override suspend fun upsert(entity: PhotoMetadataEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __upsertAdapterOfPhotoMetadataEntity.upsert(_connection, entity)
  }

  public override suspend fun upsertAll(entities: List<PhotoMetadataEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfPhotoMetadataEntity.upsert(_connection, entities)
  }

  public override suspend fun getDistinctDays(): List<Long> {
    val _sql: String =
        "SELECT DISTINCT dateTaken / 86400000 as day FROM photo_metadata ORDER BY day DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: MutableList<Long> = mutableListOf()
        while (_stmt.step()) {
          val _item: Long
          val _tmp: Long
          _tmp = _stmt.getLong(0)
          _item = _tmp
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByDay(dayEpoch: Long): List<PhotoMetadataEntity> {
    val _sql: String =
        "SELECT * FROM photo_metadata WHERE dateTaken / 86400000 = ? ORDER BY dateTaken ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, dayEpoch)
        val _columnIndexOfMediaId: Int = getColumnIndexOrThrow(_stmt, "mediaId")
        val _columnIndexOfDateTaken: Int = getColumnIndexOrThrow(_stmt, "dateTaken")
        val _columnIndexOfGroupId: Int = getColumnIndexOrThrow(_stmt, "groupId")
        val _columnIndexOfPendingDelete: Int = getColumnIndexOrThrow(_stmt, "pendingDelete")
        val _result: MutableList<PhotoMetadataEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PhotoMetadataEntity
          val _tmpMediaId: Long
          _tmpMediaId = _stmt.getLong(_columnIndexOfMediaId)
          val _tmpDateTaken: Long
          _tmpDateTaken = _stmt.getLong(_columnIndexOfDateTaken)
          val _tmpGroupId: String?
          if (_stmt.isNull(_columnIndexOfGroupId)) {
            _tmpGroupId = null
          } else {
            _tmpGroupId = _stmt.getText(_columnIndexOfGroupId)
          }
          val _tmpPendingDelete: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfPendingDelete).toInt()
          _tmpPendingDelete = _tmp != 0
          _item = PhotoMetadataEntity(_tmpMediaId,_tmpDateTaken,_tmpGroupId,_tmpPendingDelete)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByDays(dayEpochs: List<Long>): List<PhotoMetadataEntity> {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT * FROM photo_metadata WHERE dateTaken / 86400000 IN (")
    val _inputSize: Int = dayEpochs.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(") ORDER BY dateTaken ASC")
    val _sql: String = _stringBuilder.toString()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        for (_item: Long in dayEpochs) {
          _stmt.bindLong(_argIndex, _item)
          _argIndex++
        }
        val _columnIndexOfMediaId: Int = getColumnIndexOrThrow(_stmt, "mediaId")
        val _columnIndexOfDateTaken: Int = getColumnIndexOrThrow(_stmt, "dateTaken")
        val _columnIndexOfGroupId: Int = getColumnIndexOrThrow(_stmt, "groupId")
        val _columnIndexOfPendingDelete: Int = getColumnIndexOrThrow(_stmt, "pendingDelete")
        val _result: MutableList<PhotoMetadataEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item_1: PhotoMetadataEntity
          val _tmpMediaId: Long
          _tmpMediaId = _stmt.getLong(_columnIndexOfMediaId)
          val _tmpDateTaken: Long
          _tmpDateTaken = _stmt.getLong(_columnIndexOfDateTaken)
          val _tmpGroupId: String?
          if (_stmt.isNull(_columnIndexOfGroupId)) {
            _tmpGroupId = null
          } else {
            _tmpGroupId = _stmt.getText(_columnIndexOfGroupId)
          }
          val _tmpPendingDelete: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfPendingDelete).toInt()
          _tmpPendingDelete = _tmp != 0
          _item_1 = PhotoMetadataEntity(_tmpMediaId,_tmpDateTaken,_tmpGroupId,_tmpPendingDelete)
          _result.add(_item_1)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getById(mediaId: Long): PhotoMetadataEntity? {
    val _sql: String = "SELECT * FROM photo_metadata WHERE mediaId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, mediaId)
        val _columnIndexOfMediaId: Int = getColumnIndexOrThrow(_stmt, "mediaId")
        val _columnIndexOfDateTaken: Int = getColumnIndexOrThrow(_stmt, "dateTaken")
        val _columnIndexOfGroupId: Int = getColumnIndexOrThrow(_stmt, "groupId")
        val _columnIndexOfPendingDelete: Int = getColumnIndexOrThrow(_stmt, "pendingDelete")
        val _result: PhotoMetadataEntity?
        if (_stmt.step()) {
          val _tmpMediaId: Long
          _tmpMediaId = _stmt.getLong(_columnIndexOfMediaId)
          val _tmpDateTaken: Long
          _tmpDateTaken = _stmt.getLong(_columnIndexOfDateTaken)
          val _tmpGroupId: String?
          if (_stmt.isNull(_columnIndexOfGroupId)) {
            _tmpGroupId = null
          } else {
            _tmpGroupId = _stmt.getText(_columnIndexOfGroupId)
          }
          val _tmpPendingDelete: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfPendingDelete).toInt()
          _tmpPendingDelete = _tmp != 0
          _result = PhotoMetadataEntity(_tmpMediaId,_tmpDateTaken,_tmpGroupId,_tmpPendingDelete)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByIds(ids: List<Long>): List<PhotoMetadataEntity> {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT * FROM photo_metadata WHERE mediaId IN (")
    val _inputSize: Int = ids.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        for (_item: Long in ids) {
          _stmt.bindLong(_argIndex, _item)
          _argIndex++
        }
        val _columnIndexOfMediaId: Int = getColumnIndexOrThrow(_stmt, "mediaId")
        val _columnIndexOfDateTaken: Int = getColumnIndexOrThrow(_stmt, "dateTaken")
        val _columnIndexOfGroupId: Int = getColumnIndexOrThrow(_stmt, "groupId")
        val _columnIndexOfPendingDelete: Int = getColumnIndexOrThrow(_stmt, "pendingDelete")
        val _result: MutableList<PhotoMetadataEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item_1: PhotoMetadataEntity
          val _tmpMediaId: Long
          _tmpMediaId = _stmt.getLong(_columnIndexOfMediaId)
          val _tmpDateTaken: Long
          _tmpDateTaken = _stmt.getLong(_columnIndexOfDateTaken)
          val _tmpGroupId: String?
          if (_stmt.isNull(_columnIndexOfGroupId)) {
            _tmpGroupId = null
          } else {
            _tmpGroupId = _stmt.getText(_columnIndexOfGroupId)
          }
          val _tmpPendingDelete: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfPendingDelete).toInt()
          _tmpPendingDelete = _tmp != 0
          _item_1 = PhotoMetadataEntity(_tmpMediaId,_tmpDateTaken,_tmpGroupId,_tmpPendingDelete)
          _result.add(_item_1)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllIds(): List<Long> {
    val _sql: String = "SELECT mediaId FROM photo_metadata"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
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

  public override fun observeByGroupId(groupId: String): Flow<List<PhotoMetadataEntity>> {
    val _sql: String = "SELECT * FROM photo_metadata WHERE groupId = ? ORDER BY dateTaken ASC"
    return createFlow(__db, false, arrayOf("photo_metadata")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, groupId)
        val _columnIndexOfMediaId: Int = getColumnIndexOrThrow(_stmt, "mediaId")
        val _columnIndexOfDateTaken: Int = getColumnIndexOrThrow(_stmt, "dateTaken")
        val _columnIndexOfGroupId: Int = getColumnIndexOrThrow(_stmt, "groupId")
        val _columnIndexOfPendingDelete: Int = getColumnIndexOrThrow(_stmt, "pendingDelete")
        val _result: MutableList<PhotoMetadataEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PhotoMetadataEntity
          val _tmpMediaId: Long
          _tmpMediaId = _stmt.getLong(_columnIndexOfMediaId)
          val _tmpDateTaken: Long
          _tmpDateTaken = _stmt.getLong(_columnIndexOfDateTaken)
          val _tmpGroupId: String?
          if (_stmt.isNull(_columnIndexOfGroupId)) {
            _tmpGroupId = null
          } else {
            _tmpGroupId = _stmt.getText(_columnIndexOfGroupId)
          }
          val _tmpPendingDelete: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfPendingDelete).toInt()
          _tmpPendingDelete = _tmp != 0
          _item = PhotoMetadataEntity(_tmpMediaId,_tmpDateTaken,_tmpGroupId,_tmpPendingDelete)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observePendingDelete(): Flow<List<PhotoMetadataEntity>> {
    val _sql: String =
        "SELECT * FROM photo_metadata WHERE pendingDelete = 1 ORDER BY dateTaken DESC"
    return createFlow(__db, false, arrayOf("photo_metadata")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfMediaId: Int = getColumnIndexOrThrow(_stmt, "mediaId")
        val _columnIndexOfDateTaken: Int = getColumnIndexOrThrow(_stmt, "dateTaken")
        val _columnIndexOfGroupId: Int = getColumnIndexOrThrow(_stmt, "groupId")
        val _columnIndexOfPendingDelete: Int = getColumnIndexOrThrow(_stmt, "pendingDelete")
        val _result: MutableList<PhotoMetadataEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PhotoMetadataEntity
          val _tmpMediaId: Long
          _tmpMediaId = _stmt.getLong(_columnIndexOfMediaId)
          val _tmpDateTaken: Long
          _tmpDateTaken = _stmt.getLong(_columnIndexOfDateTaken)
          val _tmpGroupId: String?
          if (_stmt.isNull(_columnIndexOfGroupId)) {
            _tmpGroupId = null
          } else {
            _tmpGroupId = _stmt.getText(_columnIndexOfGroupId)
          }
          val _tmpPendingDelete: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfPendingDelete).toInt()
          _tmpPendingDelete = _tmp != 0
          _item = PhotoMetadataEntity(_tmpMediaId,_tmpDateTaken,_tmpGroupId,_tmpPendingDelete)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLatestDateTaken(): Long? {
    val _sql: String = "SELECT MAX(dateTaken) FROM photo_metadata"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Long?
        if (_stmt.step()) {
          val _tmp: Long?
          if (_stmt.isNull(0)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(0)
          }
          _result = _tmp
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun setPendingDelete(mediaId: Long, pending: Boolean) {
    val _sql: String = "UPDATE photo_metadata SET pendingDelete = ? WHERE mediaId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: Int = if (pending) 1 else 0
        _stmt.bindLong(_argIndex, _tmp.toLong())
        _argIndex = 2
        _stmt.bindLong(_argIndex, mediaId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun setPendingDeleteBatch(ids: List<Long>, pending: Boolean) {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("UPDATE photo_metadata SET pendingDelete = ")
    _stringBuilder.append("?")
    _stringBuilder.append(" WHERE mediaId IN (")
    val _inputSize: Int = ids.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: Int = if (pending) 1 else 0
        _stmt.bindLong(_argIndex, _tmp.toLong())
        _argIndex = 2
        for (_item: Long in ids) {
          _stmt.bindLong(_argIndex, _item)
          _argIndex++
        }
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteById(mediaId: Long) {
    val _sql: String = "DELETE FROM photo_metadata WHERE mediaId = ?"
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

  public override suspend fun deleteByIds(ids: List<Long>) {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("DELETE FROM photo_metadata WHERE mediaId IN (")
    val _inputSize: Int = ids.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        for (_item: Long in ids) {
          _stmt.bindLong(_argIndex, _item)
          _argIndex++
        }
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
