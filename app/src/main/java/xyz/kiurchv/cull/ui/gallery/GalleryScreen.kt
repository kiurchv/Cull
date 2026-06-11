package xyz.kiurchv.cull.ui.gallery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import xyz.kiurchv.cull.data.db.PhotoMetadataDao
import xyz.kiurchv.cull.data.db.SeriesDao
import xyz.kiurchv.cull.data.db.SeriesEntity
import xyz.kiurchv.cull.domain.GroupingSettings
import xyz.kiurchv.cull.worker.GroupingWorker
import android.content.Context
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.*

// ---- ViewModel ----

data class GalleryUiState(
    val series: List<SeriesWithCount> = emptyList(),
    val isLoading: Boolean = true,
)

data class SeriesWithCount(
    val entity: SeriesEntity,
    val photoCount: Int,
    val pendingDeleteCount: Int,
    val thumbnailMediaId: Long?,
)

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val seriesDao: SeriesDao,
    private val photoMetadataDao: PhotoMetadataDao,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(GalleryUiState())
    val state: StateFlow<GalleryUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            seriesDao.observeAll().collect { seriesList ->
                val enriched = seriesList.map { series ->
                    val photos = photoMetadataDao.getBySeriesId(series.id)
                    val thumbnail = photos.firstOrNull { !it.pendingDelete }?.mediaId
                    SeriesWithCount(
                        entity = series,
                        photoCount = photos.count { !it.pendingDelete },
                        pendingDeleteCount = photos.count { it.pendingDelete },
                        thumbnailMediaId = thumbnail,
                    )
                }
                _state.update { it.copy(series = enriched, isLoading = false) }
            }
        }
    }

    fun triggerGrouping(settings: GroupingSettings) {
        WorkManager.getInstance(context)
            .enqueue(GroupingWorker.buildRequest(settings))
    }
}

// ---- Screen ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    onSeriesClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onAlbumsClick: () -> Unit,
    viewModel: GalleryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.triggerGrouping(GroupingSettings())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cull") },
                actions = {
                    IconButton(onClick = onAlbumsClick) {
                        Icon(Icons.Default.PhotoAlbum, "Альбоми")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, "Налаштування")
                    }
                },
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.series.isEmpty() -> EmptyState(Modifier.align(Alignment.Center))
                else -> SeriesList(state.series, onSeriesClick)
            }
        }
    }
}

@Composable
private fun SeriesList(series: List<SeriesWithCount>, onSeriesClick: (String) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
        items(series, key = { it.entity.id }) { item ->
            SeriesRow(item = item, onClick = { onSeriesClick(item.entity.id) })
        }
    }
}

@Composable
private fun SeriesRow(item: SeriesWithCount, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("d MMMM yyyy", Locale.getDefault()) }
    val thumbnailUri = item.thumbnailMediaId?.let {
        android.net.Uri.withAppendedPath(
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, it.toString()
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(modifier = Modifier.size(64.dp), shape = MaterialTheme.shapes.small) {
            thumbnailUri?.let {
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.entity.locationName
                    ?: dateFormat.format(Date(item.entity.date)),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (item.entity.locationName != null) {
                Text(
                    text = dateFormat.format(Date(item.entity.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = buildString {
                    append("${item.photoCount} фото")
                    if (item.pendingDeleteCount > 0)
                        append(" · ${item.pendingDeleteCount} до видалення")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (item.pendingDeleteCount > 0) {
            Badge(containerColor = MaterialTheme.colorScheme.error) {
                Text("${item.pendingDeleteCount}")
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Фото не знайдено", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "Дозвольте доступ до фото або почекайте індексування",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
