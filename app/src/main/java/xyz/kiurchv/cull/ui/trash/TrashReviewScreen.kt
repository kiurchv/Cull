package xyz.kiurchv.cull.ui.trash

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
import androidx.compose.ui.text.style.TextAlign
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

data class TrashReviewUiState(
    val photos: List<Photo> = emptyList(),
    val selectedIds: Set<Long> = emptySet(),
    val isSelecting: Boolean = false,
    val isLoading: Boolean = true,
    val showConfirmDelete: Boolean = false,
    val showConfirmRestore: Boolean = false,
    val deleteTargetIds: List<Long> = emptyList(),
)

@HiltViewModel
class TrashReviewViewModel @Inject constructor(
    private val photoRepository: PhotoRepository,
    private val photoMetadataDao: PhotoMetadataDao,
) : ViewModel() {

    private val _state = MutableStateFlow(TrashReviewUiState())
    val state: StateFlow<TrashReviewUiState> = _state.asStateFlow()

    // Optional: filter by seriesId to show only pending in current series
    fun load(seriesId: String? = null) {
        viewModelScope.launch {
            photoRepository.observePendingDelete().collect { metaList ->
                val filtered = if (seriesId != null)
                    metaList.filter { it.seriesId == seriesId }
                else metaList

                val ids = filtered.map { it.mediaId }
                val msData = photoRepository.loadMediaStoreData(ids)

                val photos = filtered.mapNotNull { meta ->
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
                        pendingDelete = true,
                        seriesId = meta.seriesId,
                        groupId = meta.groupId,
                    )
                }

                _state.update { it.copy(photos = photos, isLoading = false) }
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

    fun clearSelection() {
        _state.update { it.copy(selectedIds = emptySet(), isSelecting = false) }
    }

    // Restore — remove pendingDelete flag
    fun restoreSelected() {
        val ids = if (_state.value.isSelecting)
            _state.value.selectedIds.toList()
        else
            _state.value.photos.map { it.id }

        viewModelScope.launch {
            photoRepository.setPendingDeleteBatch(ids, false)
            _state.update { it.copy(selectedIds = emptySet(), isSelecting = false) }
        }
    }

    fun requestDeleteSelected() {
        val ids = if (_state.value.isSelecting)
            _state.value.selectedIds.toList()
        else
            _state.value.photos.map { it.id }
        _state.update { it.copy(showConfirmDelete = true, deleteTargetIds = ids) }
    }

    fun confirmDelete() {
        val ids = _state.value.deleteTargetIds
        _state.update { it.copy(showConfirmDelete = false, deleteTargetIds = emptyList()) }
        viewModelScope.launch {
            photoRepository.deletePhotos(ids)
            _state.update { it.copy(selectedIds = emptySet(), isSelecting = false) }
        }
    }

    fun cancelDelete() {
        _state.update { it.copy(showConfirmDelete = false, deleteTargetIds = emptyList()) }
    }
}

// ---- Screen ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashReviewScreen(
    seriesId: String? = null,
    onBack: () -> Unit,
    onPhotoClick: (mediaId: Long, allIds: List<Long>) -> Unit = { _, _ -> },
    viewModel: TrashReviewViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(seriesId) { viewModel.load(seriesId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (state.isSelecting)
                        Text("${state.selectedIds.size} вибрано")
                    else
                        Text("До видалення · ${state.photos.size}")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.isSelecting) viewModel.clearSelection() else onBack()
                    }) {
                        Icon(
                            if (state.isSelecting) Icons.Default.Close
                            else Icons.Default.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                actions = {
                    if (!state.isSelecting) {
                        IconButton(onClick = viewModel::selectAll) {
                            Icon(Icons.Default.SelectAll, "Вибрати всі")
                        }
                    } else {
                        // Restore selected
                        IconButton(onClick = viewModel::restoreSelected) {
                            Icon(Icons.Default.RestoreFromTrash, "Відновити")
                        }
                        // Delete selected
                        IconButton(onClick = viewModel::requestDeleteSelected) {
                            Icon(Icons.Default.Delete, "Видалити",
                                tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (state.photos.isNotEmpty()) {
                BottomAppBar {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Restore all / restore selected
                        OutlinedButton(
                            onClick = viewModel::restoreSelected,
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Default.RestoreFromTrash, null,
                                modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (state.isSelecting) "Відновити (${state.selectedIds.size})"
                                else "Відновити всі"
                            )
                        }

                        // Delete all / delete selected
                        Button(
                            onClick = viewModel::requestDeleteSelected,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                            ),
                        ) {
                            Icon(Icons.Default.Delete, null,
                                modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (state.isSelecting) "Видалити (${state.selectedIds.size})"
                                else "Видалити всі"
                            )
                        }
                    }
                }
            }
        },
    ) { padding ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            state.photos.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            "Нічого до видалення",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            "Позначені фото з'являться тут",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 120.dp),
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    items(state.photos, key = { it.id }) { photo ->
                        PhotoCell(
                            uri = photo.uri,
                            isSelected = photo.id in state.selectedIds,
                            isPendingDelete = true,
                            isFavorite = photo.isFavorite,
                            badge = null,
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
    }

    // Confirm delete dialog
    if (state.showConfirmDelete) {
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Видалити ${state.deleteTargetIds.size} фото?") },
            text = {
                Text("Фото буде переміщено в корзину Android. Їх можна відновити через стандартну галерею протягом 30 днів.")
            },
            confirmButton = {
                Button(
                    onClick = viewModel::confirmDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Видалити") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelDelete) { Text("Скасувати") }
            }
        )
    }
}
