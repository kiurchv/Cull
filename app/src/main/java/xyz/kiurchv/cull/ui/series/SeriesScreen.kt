package xyz.kiurchv.cull.ui.series

import android.net.Uri
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import xyz.kiurchv.cull.data.db.*
import xyz.kiurchv.cull.data.model.*
import xyz.kiurchv.cull.domain.GroupingEngine
import xyz.kiurchv.cull.domain.GroupingSettings
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.*

// ---- ViewModel ----

data class SeriesUiState(
    val locationName: String? = null,
    val date: Long = 0L,
    val batches: List<Batch> = emptyList(),
    val isLoading: Boolean = true,
    val selectedIds: Set<Long> = emptySet(),
    val isSelecting: Boolean = false,
)

@HiltViewModel
class SeriesViewModel @Inject constructor(
    private val photoMetadataDao: PhotoMetadataDao,
    private val seriesDao: SeriesDao,
    private val photoHashDao: PhotoHashDao,
    private val groupingEngine: GroupingEngine,
    private val photoRepository: xyz.kiurchv.cull.data.PhotoRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SeriesUiState())
    val state: StateFlow<SeriesUiState> = _state.asStateFlow()

    private var settings = GroupingSettings()

    fun load(seriesId: String) {
        viewModelScope.launch {
            val series = seriesDao.getById(seriesId) ?: return@launch
            photoMetadataDao.observeBySeriesId(seriesId).collect { metaList ->
                val ids = metaList.map { it.mediaId }
                val hashMap = photoHashDao.getByIds(ids)
                    .associate { it.mediaId to (it.pHash to it.sharpness) }
                val msData = photoRepository.loadMediaStoreData(ids)

                val photos = metaList.mapNotNull { meta ->
                    val ms = msData[meta.mediaId] ?: return@mapNotNull null
                    val (_, sharpness) = hashMap[meta.mediaId] ?: (0L to 0f)
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
                        sharpness = sharpness,
                        pendingDelete = meta.pendingDelete,
                        seriesId = meta.seriesId,
                        groupId = meta.groupId,
                    )
                }

                val batches = groupingEngine.buildBatches(photos, settings)
                _state.update {
                    it.copy(
                        locationName = series.locationName,
                        date = series.date,
                        batches = batches,
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun toggleSelect(mediaId: Long) {
        _state.update { s ->
            val newSelected = if (mediaId in s.selectedIds)
                s.selectedIds - mediaId
            else
                s.selectedIds + mediaId
            s.copy(
                selectedIds = newSelected,
                isSelecting = newSelected.isNotEmpty(),
            )
        }
    }

    fun clearSelection() {
        _state.update { it.copy(selectedIds = emptySet(), isSelecting = false) }
    }
}

// ---- Screen ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesScreen(
    seriesId: String,
    onBack: () -> Unit,
    onPhotoClick: (mediaId: Long, allIds: List<Long>) -> Unit = { _, _ -> },
    onDuplicateGroupClick: (groupId: String) -> Unit = {},
    onTrashReviewClick: () -> Unit = {},
    viewModel: SeriesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("d MMMM yyyy", Locale.getDefault()) }

    LaunchedEffect(seriesId) { viewModel.load(seriesId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            state.locationName ?: dateFormat.format(Date(state.date)),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (state.locationName != null) {
                            Text(
                                dateFormat.format(Date(state.date)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.isSelecting) viewModel.clearSelection() else onBack()
                    }) {
                        Icon(
                            if (state.isSelecting) Icons.Default.Close else Icons.Default.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                actions = {
                    val pendingCount = state.batches.sumOf { b ->
                        b.photos.count { it.pendingDelete } +
                        b.duplicateGroups.sumOf { g -> g.photos.count { it.pendingDelete } }
                    }
                    if (pendingCount > 0) {
                        BadgedBox(
                            badge = { Badge { Text("$pendingCount") } },
                            modifier = Modifier.padding(end = 4.dp),
                        ) {
                            IconButton(onClick = onTrashReviewClick) {
                                Icon(Icons.Default.DeleteSweep, "Перегляд до видалення")
                            }
                        }
                    }
                    if (state.isSelecting) {
                        IconButton(onClick = { /* TODO: favorite selected */ }) {
                            Icon(Icons.Default.FavoriteBorder, "Улюблене")
                        }
                        IconButton(onClick = { /* TODO: pending delete selected */ }) {
                            Icon(Icons.Default.Delete, "До видалення",
                                tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 8.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(state.batches, key = { it.startTime }) { batch ->
                BatchRow(
                    batch = batch,
                    timeFormat = timeFormat,
                    selectedIds = state.selectedIds,
                    isSelecting = state.isSelecting,
                    onPhotoClick = { mediaId ->
                        if (state.isSelecting) viewModel.toggleSelect(mediaId)
                        else {
                            val allIds = batch.photos.map { it.id } +
                                    batch.duplicateGroups.map { it.best.id }
                            onPhotoClick(mediaId, allIds)
                        }
                    },
                    onPhotoLongClick = { viewModel.toggleSelect(it) },
                    onDuplicateGroupClick = { groupId ->
                        if (state.isSelecting) {
                            val group = batch.duplicateGroups.first { it.id == groupId }
                            group.photos.forEach { viewModel.toggleSelect(it.id) }
                        } else {
                            onDuplicateGroupClick(groupId)
                        }
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BatchRow(
    batch: Batch,
    timeFormat: SimpleDateFormat,
    selectedIds: Set<Long>,
    isSelecting: Boolean,
    onPhotoClick: (Long) -> Unit,
    onPhotoLongClick: (Long) -> Unit,
    onDuplicateGroupClick: (String) -> Unit,
) {
    val timeLabel = buildString {
        append(timeFormat.format(Date(batch.startTime)))
        if (batch.endTime - batch.startTime > 5_000) {
            append(" — ${timeFormat.format(Date(batch.endTime))}")
        }
        append(" · ${batch.totalCount} фото")
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = timeLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            contentPadding = PaddingValues(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            userScrollEnabled = false,
        ) {
            // Standalone photos
            items(batch.photos, key = { it.id }) { photo ->
                PhotoCell(
                    uri = photo.uri,
                    isSelected = photo.id in selectedIds,
                    isPendingDelete = photo.pendingDelete,
                    isFavorite = photo.isFavorite,
                    badge = null,
                    onClick = { onPhotoClick(photo.id) },
                    onLongClick = { onPhotoLongClick(photo.id) },
                )
            }
            // Duplicate groups — one cell per group showing best photo
            items(batch.duplicateGroups, key = { "grp_\${it.id}" }) { group ->
                PhotoCell(
                    uri = group.best.uri,
                    isSelected = group.photos.any { it.id in selectedIds },
                    isPendingDelete = group.photos.any { it.pendingDelete },
                    isFavorite = group.best.isFavorite,
                    badge = PhotoCellBadge.Duplicate(group.photos.size),
                    onClick = { onDuplicateGroupClick(group.id) },
                    onLongClick = { onPhotoLongClick(group.best.id) },
                )
            }
        }
    }
}

enum class PhotoCellBadgeType { DUPLICATE, BATCH }
sealed class PhotoCellBadge {
    data class Duplicate(val count: Int) : PhotoCellBadge()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoCell(
    uri: android.net.Uri,
    isSelected: Boolean,
    isPendingDelete: Boolean,
    isFavorite: Boolean = false,
    badge: PhotoCellBadge?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .then(
                if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary)
                else Modifier
            ),
    ) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // Pending delete overlay
        if (isPendingDelete) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Red.copy(alpha = 0.3f))
            )
            Icon(
                Icons.Default.Delete,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.align(Alignment.Center).size(24.dp),
            )
        }

        // Favorite icon
        if (isFavorite && !isPendingDelete) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp)
                    .size(16.dp),
            )
        }

        // Selection overlay
        if (isSelected) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.TopStart).padding(4.dp).size(20.dp),
            )
        }

        // Badge (duplicate count)
        if (badge is PhotoCellBadge.Duplicate) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small,
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(10.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        "${badge.count}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }
    }
}
