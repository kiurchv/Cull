package xyz.kiurchv.cull.ui.gallery

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import coil.compose.AsyncImage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import xyz.kiurchv.cull.data.IndexingState
import xyz.kiurchv.cull.data.IndexingStatus
import xyz.kiurchv.cull.data.IndexingStore
import xyz.kiurchv.cull.data.PhotoRepository
import xyz.kiurchv.cull.data.db.PhotoHashDao
import xyz.kiurchv.cull.data.db.PhotoMetadataDao
import xyz.kiurchv.cull.data.model.*
import xyz.kiurchv.cull.domain.GroupingEngine
import xyz.kiurchv.cull.domain.GroupingSettings
import xyz.kiurchv.cull.worker.IndexingWorker
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

// ---- ViewModel ----

data class GalleryUiState(
    val series: List<Series> = emptyList(),
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val indexingState: IndexingState = IndexingState(),
    val settings: GroupingSettings = GroupingSettings(),
)

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val photoMetadataDao: PhotoMetadataDao,
    private val photoHashDao: PhotoHashDao,
    private val photoRepository: PhotoRepository,
    private val groupingEngine: GroupingEngine,
    private val indexingStore: IndexingStore,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(GalleryUiState())
    val state: StateFlow<GalleryUiState> = _state.asStateFlow()

    private var allDays: List<Long> = emptyList()
    private var loadedDayIndex = 0

    init {
        viewModelScope.launch {
            indexingStore.state.collect { indexingState ->
                _state.update { it.copy(indexingState = indexingState) }
                // Reload days when indexing completes or updates
                if (indexingState.status == IndexingStatus.SUCCESS ||
                    indexingState.indexedDayCount > loadedDayIndex) {
                    refreshDays()
                }
            }
        }
        viewModelScope.launch { refreshDays() }
    }

    private suspend fun refreshDays() {
        allDays = photoMetadataDao.getDistinctDays()
        if (allDays.isNotEmpty() && loadedDayIndex == 0) {
            loadNextPage()
        }
    }

    fun loadNextPage() {
        if (_state.value.isLoadingMore) return
        if (loadedDayIndex >= allDays.size) {
            _state.update { it.copy(hasMore = false) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoadingMore = true) }

            // Load one day at a time
            val day = allDays[loadedDayIndex]
            val metaList = photoMetadataDao.getByDay(day)

            if (metaList.isNotEmpty()) {
                val ids = metaList.map { it.mediaId }
                val msData = photoRepository.loadMediaStoreData(ids)
                val hashMap = photoHashDao.getByIds(ids)
                    .associate { it.mediaId to (it.pHash to it.sharpness) }

                val photos = metaList.mapNotNull { meta ->
                    val ms = msData[meta.mediaId] ?: return@mapNotNull null
                    Photo(
                        id = meta.mediaId,
                        uri = Uri.withAppendedPath(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            meta.mediaId.toString()
                        ),
                        path = ms.path,
                        displayName = ms.displayName,
                        dateTaken = meta.dateTaken,
                        width = ms.width,
                        height = ms.height,
                        size = ms.size,
                        mimeType = ms.mimeType,
                        isFavorite = ms.isFavorite,
                        pendingDelete = meta.pendingDelete,
                        groupId = meta.groupId,
                    )
                }

                val newSeries = groupingEngine.buildSeriesForDay(photos, day, _state.value.settings, hashMap)
                _state.update { s ->
                    s.copy(series = s.series + newSeries)
                }
            }

            loadedDayIndex++
            _state.update { it.copy(
                isLoadingMore = false,
                hasMore = loadedDayIndex < allDays.size,
            ) }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            // Reset loaded state
            loadedDayIndex = 0
            _state.update { it.copy(series = emptyList(), hasMore = true) }
            // Trigger indexing
            WorkManager.getInstance(context).enqueueUniqueWork(
                IndexingWorker.WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                IndexingWorker.buildOneTimeRequest(),
            )
        }
    }

    fun updateSettings(settings: GroupingSettings) {
        loadedDayIndex = 0
        _state.update { it.copy(settings = settings, series = emptyList(), hasMore = true) }
        viewModelScope.launch { loadNextPage() }
    }
}

// ---- Screen ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    onSeriesClick: (series: Series) -> Unit,
    onSettingsClick: () -> Unit,
    onAlbumsClick: () -> Unit,
    viewModel: GalleryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error snackbar
    LaunchedEffect(state.indexingState.error) {
        state.indexingState.error?.let { error ->
            val result = snackbarHostState.showSnackbar(
                message = "Помилка індексування: $error",
                actionLabel = "Повторити",
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.refresh()
        }
    }

    // Trigger load next page when approaching end
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            lastVisible >= total - 3 && state.hasMore && !state.isLoadingMore
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadNextPage()
    }

    var isRefreshing by remember { mutableStateOf(false) }
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            viewModel.refresh()
            isRefreshing = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Cull") },
                actions = {
                    // Indexing status indicator
                    when (state.indexingState.status) {
                        IndexingStatus.RUNNING -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp).padding(end = 4.dp),
                                strokeWidth = 2.dp,
                            )
                        }
                        IndexingStatus.ERROR -> {
                            IconButton(onClick = viewModel::refresh) {
                                Icon(Icons.Default.ErrorOutline, "Помилка індексування",
                                    tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        else -> {}
                    }
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
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { isRefreshing = true },
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            when {
                state.series.isEmpty() && state.indexingState.status == IndexingStatus.RUNNING -> {
                    FirstRunLoader()
                }
                state.series.isEmpty() && !state.isLoadingMore -> {
                    EmptyState()
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(vertical = 8.dp),
                    ) {
                        items(state.series, key = { it.id }) { series ->
                            SeriesRow(
                                series = series,
                                onClick = { onSeriesClick(series) },
                            )
                        }
                        if (state.isLoadingMore) {
                            item {
                                Box(
                                    Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FirstRunLoader() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CircularProgressIndicator()
            Text(
                "Індексуємо фото…\nЦе займе кілька хвилин при першому запуску",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            "Фото не знайдено\nДозвольте доступ до фото або потягніть вниз для оновлення",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(32.dp),
        )
    }
}

@Composable
private fun SeriesRow(series: Series, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("d MMMM yyyy", Locale.getDefault()) }
    val firstPhoto = series.batches.firstOrNull()?.photos?.firstOrNull()
        ?: series.batches.firstOrNull()?.duplicateGroups?.firstOrNull()?.best

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(modifier = Modifier.size(64.dp), shape = MaterialTheme.shapes.small) {
            firstPhoto?.let {
                AsyncImage(
                    model = it.uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                series.locationName ?: dateFormat.format(Date(series.date)),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (series.locationName != null) {
                Text(
                    dateFormat.format(Date(series.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                buildString {
                    append("${series.photoCount} фото")
                    if (series.pendingDeleteCount > 0)
                        append(" · ${series.pendingDeleteCount} до видалення")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (series.pendingDeleteCount > 0) {
            Badge(containerColor = MaterialTheme.colorScheme.error) {
                Text("${series.pendingDeleteCount}")
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}
