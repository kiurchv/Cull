package xyz.kiurchv.cull.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class IndexingStatus { IDLE, RUNNING, SUCCESS, ERROR }

data class IndexingState(
    val status: IndexingStatus = IndexingStatus.IDLE,
    val error: String? = null,
    val lastIndexedAt: Long = 0L,
    val indexedDayCount: Int = 0,   // how many days of pHash are done
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "indexing")

@Singleton
class IndexingStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val STATUS = stringPreferencesKey("status")
        val ERROR = stringPreferencesKey("error")
        val LAST_INDEXED_AT = longPreferencesKey("last_indexed_at")
        val INDEXED_DAY_COUNT = intPreferencesKey("indexed_day_count")
    }

    val state: Flow<IndexingState> = context.dataStore.data.map { prefs ->
        IndexingState(
            status = prefs[Keys.STATUS]?.let {
                runCatching { IndexingStatus.valueOf(it) }.getOrDefault(IndexingStatus.IDLE)
            } ?: IndexingStatus.IDLE,
            error = prefs[Keys.ERROR],
            lastIndexedAt = prefs[Keys.LAST_INDEXED_AT] ?: 0L,
            indexedDayCount = prefs[Keys.INDEXED_DAY_COUNT] ?: 0,
        )
    }

    suspend fun setRunning() = update {
        it[Keys.STATUS] = IndexingStatus.RUNNING.name
        it.remove(Keys.ERROR)
    }

    suspend fun setSuccess(indexedDayCount: Int) = update {
        it[Keys.STATUS] = IndexingStatus.SUCCESS.name
        it[Keys.LAST_INDEXED_AT] = System.currentTimeMillis()
        it[Keys.INDEXED_DAY_COUNT] = indexedDayCount
        it.remove(Keys.ERROR)
    }

    suspend fun setError(message: String) = update {
        it[Keys.STATUS] = IndexingStatus.ERROR.name
        it[Keys.ERROR] = message
    }

    suspend fun incrementIndexedDays() = update {
        val current = it[Keys.INDEXED_DAY_COUNT] ?: 0
        it[Keys.INDEXED_DAY_COUNT] = current + 1
    }

    private suspend fun update(block: (MutablePreferences) -> Unit) {
        context.dataStore.updateData { prefs ->
            prefs.toMutablePreferences().also(block)
        }
    }
}
