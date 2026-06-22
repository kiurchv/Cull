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
import kotlin.Float
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
public class PhotoHashDao_Impl(
  __db: RoomDatabase,
) : PhotoHashDao {
  private val __db: RoomDatabase

  private val __upsertAdapterOfPhotoHashEntity: EntityUpsertAdapter<PhotoHashEntity>
  init {
    this.__db = __db
    this.__upsertAdapterOfPhotoHashEntity = EntityUpsertAdapter<PhotoHashEntity>(object :
        EntityInsertAdapter<PhotoHashEntity>() {
      protected override fun createQuery(): String =
          "INSERT INTO `photo_hashes` (`mediaId`,`path`,`pHash`,`sharpness`,`indexedAt`) VALUES (?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PhotoHashEntity) {
        statement.bindLong(1, entity.mediaId)
        statement.bindText(2, entity.path)
        statement.bindLong(3, entity.pHash)
        statement.bindDouble(4, entity.sharpness.toDouble())
        statement.bindLong(5, entity.indexedAt)
      }
    }, object : EntityDeleteOrUpdateAdapter<PhotoHashEntity>() {
      protected override fun createQuery(): String =
          "UPDATE `photo_hashes` SET `mediaId` = ?,`path` = ?,`pHash` = ?,`sharpness` = ?,`indexedAt` = ? WHERE `mediaId` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: PhotoHashEntity) {
        statement.bindLong(1, entity.mediaId)
        statement.bindText(2, entity.path)
        statement.bindLong(3, entity.pHash)
        statement.bindDouble(4, entity.sharpness.toDouble())
        statement.bindLong(5, entity.indexedAt)
        statement.bindLong(6, entity.mediaId)
      }
    })
  }

  public override suspend fun upsert(entity: PhotoHashEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __upsertAdapterOfPhotoHashEntity.upsert(_connection, entity)
  }

  public override suspend fun upsertAll(entities: List<PhotoHashEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfPhotoHashEntity.upsert(_connection, entities)
  }

  public override fun observeAll(): Flow<List<PhotoHashEntity>> {
    val _sql: String = "SELECT * FROM photo_hashes"
    return createFlow(__db, false, arrayOf("photo_hashes")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfMediaId: Int = getColumnIndexOrThrow(_stmt, "mediaId")
        val _columnIndexOfPath: Int = getColumnIndexOrThrow(_stmt, "path")
        val _columnIndexOfPHash: Int = getColumnIndexOrThrow(_stmt, "pHash")
        val _columnIndexOfSharpness: Int = getColumnIndexOrThrow(_stmt, "sharpness")
        val _columnIndexOfIndexedAt: Int = getColumnIndexOrThrow(_stmt, "indexedAt")
        val _result: MutableList<PhotoHashEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PhotoHashEntity
          val _tmpMediaId: Long
          _tmpMediaId = _stmt.getLong(_columnIndexOfMediaId)
          val _tmpPath: String
          _tmpPath = _stmt.getText(_columnIndexOfPath)
          val _tmpPHash: Long
          _tmpPHash = _stmt.getLong(_columnIndexOfPHash)
          val _tmpSharpness: Float
          _tmpSharpness = _stmt.getDouble(_columnIndexOfSharpness).toFloat()
          val _tmpIndexedAt: Long
          _tmpIndexedAt = _stmt.getLong(_columnIndexOfIndexedAt)
          _item = PhotoHashEntity(_tmpMediaId,_tmpPath,_tmpPHash,_tmpSharpness,_tmpIndexedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllIds(): List<Long> {
    val _sql: String = "SELECT mediaId FROM photo_hashes"
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

  public override suspend fun getByIds(ids: List<Long>): List<PhotoHashEntity> {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT * FROM photo_hashes WHERE mediaId IN (")
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
        val _columnIndexOfPath: Int = getColumnIndexOrThrow(_stmt, "path")
        val _columnIndexOfPHash: Int = getColumnIndexOrThrow(_stmt, "pHash")
        val _columnIndexOfSharpness: Int = getColumnIndexOrThrow(_stmt, "sharpness")
        val _columnIndexOfIndexedAt: Int = getColumnIndexOrThrow(_stmt, "indexedAt")
        val _result: MutableList<PhotoHashEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item_1: PhotoHashEntity
          val _tmpMediaId: Long
          _tmpMediaId = _stmt.getLong(_columnIndexOfMediaId)
          val _tmpPath: String
          _tmpPath = _stmt.getText(_columnIndexOfPath)
          val _tmpPHash: Long
          _tmpPHash = _stmt.getLong(_columnIndexOfPHash)
          val _tmpSharpness: Float
          _tmpSharpness = _stmt.getDouble(_columnIndexOfSharpness).toFloat()
          val _tmpIndexedAt: Long
          _tmpIndexedAt = _stmt.getLong(_columnIndexOfIndexedAt)
          _item_1 = PhotoHashEntity(_tmpMediaId,_tmpPath,_tmpPHash,_tmpSharpness,_tmpIndexedAt)
          _result.add(_item_1)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getIndexedIds(ids: List<Long>): List<Long> {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT mediaId FROM photo_hashes WHERE mediaId IN (")
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
        val _result: MutableList<Long> = mutableListOf()
        while (_stmt.step()) {
          val _item_1: Long
          _item_1 = _stmt.getLong(0)
          _result.add(_item_1)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteById(id: Long) {
    val _sql: String = "DELETE FROM photo_hashes WHERE mediaId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteByIds(ids: List<Long>) {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("DELETE FROM photo_hashes WHERE mediaId IN (")
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
