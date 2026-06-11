package xyz.kiurchv.cull.ui.duplicates

import android.net.Uri
import android.provider.MediaStore
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import xyz.kiurchv.cull.data.PhotoRepository
import xyz.kiurchv.cull.data.db.PhotoMetadataDao
import xyz.kiurchv.cull.data.model.Photo
import xyz.kiurchv.cull.ui.series.PhotoCell
import xyz.kiurchv.cull.ui.series.PhotoCellBadge
import javax.inject.Inject

// ---- ViewModel ----

data class DuplicateGroupUiState(
    val photos: List<Photo> = emptyList(),
    val bestIndex: Int = 0,
    val selectedIds: Set<Long> = emptySet(),
    val isSelecting: Boolean = false,
    val isLoading: Boolean = true,
    val showDeleteConfirm: Boolean = false,
    val deleteTargetIds: List<Long> = emptyList(),
)

@HiltViewModel
class DuplicateGroupViewModel @Inject constructor(
    private val photoRepository: PhotoRepository,
    private val photoMetadataDao: PhotoMetadataDao,
) : ViewModel() {

    private val _state = MutableStateFlow(DuplicateGroupUiState())
    val state: StateFlow<DuplicateGroupUiState> = _state.asStateFlow()

    fun load(groupId: String) {
        viewModelScope.launch {
            photoMetadataDao.observeByGroupId(groupId).collect { metaList ->
                val ids = metaList.map { it.mediaId }
                val msData = photoRepository.loadMediaStoreData(ids)

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
                    )
                }

                // Best = highest sharpness (loaded via hashDao in a real impl,
                // for now use size as proxy — larger usually = better quality)
                val bestIndex = photos.indexOfMaxByOrZero { it.size }

                _state.update {
                    it.copy(photos = photos, bestIndex = bestIndex, isLoading = false)
                }
            }
        }
    }

    fun toggleSelect(mediaId: Long) {
        _state.update { s ->
            val newSelected = if (mediaId in s.selectedIds) s.selectedIds - mediaId
            else s.selectedIds + mediaId
            s.copy(selectedIds = newSelected, isSelecting = newSelected.isNotEmpty())
        }
    }

    fun selectAll() {
        _state.update { s ->
            s.copy(selectedIds = s.photos.map { it.id }.toSet(), isSelecting = true)
        }
    }

    fun selectAllExceptBest() {
        _state.update { s ->
            val bestId = s.photos.getOrNull(s.bestIndex)?.id
            s.copy(
                selectedIds = s.photos.map { it.id }.filter { it != bestId }.toSet(),
                isSelecting = true,
            )
        }
    }

    fun clearSelection() {
        _state.update { it.copy(selectedIds = emptySet(), isSelecting = false) }
    }

    fun markSelectedPendingDelete() {
        val ids = _state.value.selectedIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            photoRepository.setPendingDeleteBatch(ids, true)
            _state.update { it.copy(selectedIds = emptySet(), isSelecting = false) }
        }
    }

    fun markSelectedFavorite() {
        val ids = _state.value.selectedIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            ids.forEach { photoRepository.setFavorite(it, true) }
            // Refresh
            val msData = photoRepository.loadMediaStoreData(ids)
            _state.update { s ->
                val newPhotos = s.photos.map { p ->
                    msData[p.id]?.let { ms -> p.copy(isFavorite = ms.isFavorite) } ?: p
                }
                newPhotos.let { s.copy(photos = it, selectedIds = emptySet(), isSelecting = false) }
            }
        }
    }

    fun requestDeleteSelected() {
        val ids = _state.value.selectedIds.toList()
        if (ids.isEmpty()) return
        _state.update { it.copy(showDeleteConfirm = true, deleteTargetIds = ids) }
    }

    fun confirmDelete() {
        val ids = _state.value.deleteTargetIds
        _state.update { it.copy(showDeleteConfirm = false, deleteTargetIds = emptyList()) }
        viewModelScope.launch {
            photoRepository.deletePhotos(ids)
            _state.update { it.copy(selectedIds = emptySet(), isSelecting = false) }
        }
    }

    fun cancelDelete() {
        _state.update { it.copy(showDeleteConfirm = false, deleteTargetIds = emptyList()) }
    }

    private fun <T> List<T>.indexOfMaxByOrZero(selector: (T) -> Long): Int {
        var bestIdx = 0
        var bestVal = Long.MIN_VALUE
        forEachIndexed { i, item ->
            val v = selector(item)
            if (v > bestVal) { bestVal = v; bestIdx = i }
        }
        return bestIdx
    }
}

// ---- Screen ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuplicateGroupScreen(
    groupId: String,
    onBack: () -> Unit,
    onPhotoClick: (mediaId: Long, allIds: List<Long>) -> Unit = { _, _ -> },
    viewModel: DuplicateGroupViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(groupId) { viewModel.load(groupId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (state.isSelecting)
                        Text("${state.selectedIds.size} вибрано")
                    else
                        Text("Схожі фото · ${state.photos.size}")
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
                    if (!state.isSelecting) {
                        // Quick action: select all except best
                        TextButton(onClick = viewModel::selectAllExceptBest) {
                            Text("Крім найкращого")
                        }
                    } else {
                        IconButton(onClick = viewModel::markSelectedFavorite) {
                            Icon(Icons.Default.FavoriteBorder, "Улюблене")
                        }
                        IconButton(onClick = viewModel::markSelectedPendingDelete) {
                            Icon(Icons.Default.DeleteOutline, "До видалення",
                                tint = MaterialTheme.colorScheme.error)
                        }
                        IconButton(onClick = viewModel::requestDeleteSelected) {
                            Icon(Icons.Default.Delete, "Видалити",
                                tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (!state.isSelecting && state.photos.isNotEmpty()) {
                BottomAppBar {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        OutlinedButton(onClick = viewModel::selectAllExceptBest) {
                            Icon(Icons.Default.AutoAwesome, null,
                                modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Видалити дублікати")
                        }
                        OutlinedButton(onClick = viewModel::selectAll) {
                            Icon(Icons.Default.SelectAll, null,
                                modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Вибрати всі")
                        }
                    }
                }
            }
        },
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            itemsIndexed(state.photos, key = { _, p -> p.id }) { index, photo ->
                Box {
                    PhotoCell(
                        uri = photo.uri,
                        isSelected = photo.id in state.selectedIds,
                        isPendingDelete = photo.pendingDelete,
                        isFavorite = photo.isFavorite,
                        badge = if (index == state.bestIndex)
                            PhotoCellBadge.Best else null,
                        onClick = {
                            if (state.isSelecting) viewModel.toggleSelect(photo.id)
                            else onPhotoClick(photo.id, state.photos.map { it.id })
                        },
                        onLongClick = { viewModel.toggleSelect(photo.id) },
                    )
                }
            }
        }
    }

    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            title = { Text("Видалити ${state.deleteTargetIds.size} фото?") },
            text = { Text("Фото буде переміщено в корзину Android.") },
            confirmButton = {
                TextButton(
                    onClick = viewModel::confirmDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Видалити") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelDelete) { Text("Скасувати") }
            }
        )
    }
}
