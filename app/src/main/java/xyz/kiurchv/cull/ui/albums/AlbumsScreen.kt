package xyz.kiurchv.cull.ui.albums

import android.net.Uri
import android.provider.MediaStore
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import xyz.kiurchv.cull.data.HardLinkManager
import xyz.kiurchv.cull.data.PhotoRepository
import xyz.kiurchv.cull.data.db.AlbumDao
import xyz.kiurchv.cull.data.model.Album
import xyz.kiurchv.cull.data.model.Photo
import xyz.kiurchv.cull.ui.series.PhotoCell
import xyz.kiurchv.cull.ui.series.PhotoCellBadge
import javax.inject.Inject

// ---- Albums list ViewModel ----

data class AlbumWithMeta(
    val album: Album,
    val photoCount: Int,
    val thumbnailMediaId: Long?,
)

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val albumDao: AlbumDao,
    private val hardLinkManager: HardLinkManager,
) : ViewModel() {

    val albums: StateFlow<List<AlbumWithMeta>> = albumDao.observeAll()
        .map { entities ->
            entities.map { entity ->
                val count = albumDao.countPhotosInAlbum(entity.name)
                val thumbnail = albumDao.getLinksForAlbum(entity.name).firstOrNull()?.mediaId
                AlbumWithMeta(
                    album = Album(entity.name, entity.path, count),
                    photoCount = count,
                    thumbnailMediaId = thumbnail,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun createAlbum(name: String) {
        viewModelScope.launch { hardLinkManager.createAlbum(name) }
    }

    fun deleteAlbum(name: String) {
        viewModelScope.launch { hardLinkManager.deleteAlbum(name) }
    }
}

// ---- Albums list Screen ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumsScreen(
    onBack: () -> Unit,
    onAlbumClick: (String) -> Unit = {},
    viewModel: AlbumsViewModel = hiltViewModel(),
) {
    val albums by viewModel.albums.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Альбоми") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, "Створити альбом")
            }
        }
    ) { padding ->
        if (albums.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    "Немає альбомів\nНатисніть + щоб створити",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 88.dp,
                )
            ) {
                items(albums, key = { it.album.name }) { item ->
                    AlbumRow(
                        item = item,
                        onClick = { onAlbumClick(item.album.name) },
                        onDelete = { viewModel.deleteAlbum(item.album.name) },
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateAlbumDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                viewModel.createAlbum(name)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun AlbumRow(
    item: AlbumWithMeta,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val thumbnailUri = item.thumbnailMediaId?.let {
        Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, it.toString())
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(modifier = Modifier.size(56.dp), shape = MaterialTheme.shapes.small) {
            if (thumbnailUri != null) {
                AsyncImage(
                    model = thumbnailUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.PhotoAlbum, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(item.album.name, style = MaterialTheme.typography.titleMedium)
            Text(
                "${item.photoCount} фото",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, "Видалити альбом",
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ---- Album content ViewModel ----

data class AlbumContentUiState(
    val albumName: String = "",
    val photos: List<Photo> = emptyList(),
    val selectedIds: Set<Long> = emptySet(),
    val isSelecting: Boolean = false,
    val isLoading: Boolean = true,
    val showDeleteConfirm: Boolean = false,
)

@HiltViewModel
class AlbumContentViewModel @Inject constructor(
    private val albumDao: AlbumDao,
    private val hardLinkManager: HardLinkManager,
    private val photoRepository: PhotoRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AlbumContentUiState())
    val state: StateFlow<AlbumContentUiState> = _state.asStateFlow()

    fun load(albumName: String) {
        _state.update { it.copy(albumName = albumName) }
        viewModelScope.launch {
            albumDao.observeMediaIdsInAlbum(albumName).collect { mediaIds ->
                if (mediaIds.isEmpty()) {
                    _state.update { it.copy(photos = emptyList(), isLoading = false) }
                    return@collect
                }
                val msData = photoRepository.loadMediaStoreData(mediaIds)
                val photos = mediaIds.mapNotNull { id ->
                    val ms = msData[id] ?: return@mapNotNull null
                    Photo(
                        id = id,
                        uri = Uri.withAppendedPath(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString()
                        ),
                        path = ms.path,
                        displayName = ms.displayName,
                        dateTaken = 0L,
                        width = ms.width,
                        height = ms.height,
                        size = ms.size,
                        mimeType = ms.mimeType,
                        isFavorite = ms.isFavorite,
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

    fun toggleFavoriteSelected() {
        val ids = _state.value.selectedIds.toList()
        val allFavorite = _state.value.photos
            .filter { it.id in _state.value.selectedIds }
            .all { it.isFavorite }
        viewModelScope.launch {
            ids.forEach { photoRepository.setFavorite(it, !allFavorite) }
            val msData = photoRepository.loadMediaStoreData(ids)
            _state.update { s ->
                val newPhotos = s.photos.map { p ->
                    msData[p.id]?.let { ms -> p.copy(isFavorite = ms.isFavorite) } ?: p
                }
                s.copy(photos = newPhotos, selectedIds = emptySet(), isSelecting = false)
            }
        }
    }

    fun removeSelectedFromAlbum() {
        val albumName = _state.value.albumName
        val ids = _state.value.selectedIds.toList()
        viewModelScope.launch {
            ids.forEach { mediaId ->
                val photo = _state.value.photos.firstOrNull { it.id == mediaId } ?: return@forEach
                hardLinkManager.removePhotoFromAlbum(photo, albumName)
            }
            _state.update { it.copy(selectedIds = emptySet(), isSelecting = false) }
        }
    }
}

// ---- Album content Screen ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumContentScreen(
    albumName: String,
    onBack: () -> Unit,
    onPhotoClick: (mediaId: Long, allIds: List<Long>) -> Unit = { _, _ -> },
    viewModel: AlbumContentViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(albumName) { viewModel.load(albumName) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (state.isSelecting) Text("${state.selectedIds.size} вибрано")
                    else Text(albumName)
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
                        IconButton(onClick = viewModel::selectAll) {
                            Icon(Icons.Default.SelectAll, "Вибрати всі")
                        }
                    } else {
                        IconButton(onClick = viewModel::toggleFavoriteSelected) {
                            val allFav = state.photos
                                .filter { it.id in state.selectedIds }
                                .all { it.isFavorite }
                            Icon(
                                if (allFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                "Улюблене",
                                tint = if (allFav) androidx.compose.ui.graphics.Color.Red
                                else LocalContentColor.current,
                            )
                        }
                        IconButton(onClick = viewModel::removeSelectedFromAlbum) {
                            Icon(Icons.Default.LinkOff, "Видалити з альбому",
                                tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.photos.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(
                        "Альбом порожній",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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
                            isPendingDelete = false,
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
}

// ---- Shared Album Picker Dialog ----

@Composable
fun AlbumPickerDialog(
    albums: List<Album>,
    onDismiss: () -> Unit,
    onSelectAlbum: (String) -> Unit,
    onCreateAlbum: (String) -> Unit,
) {
    var showCreateField by remember { mutableStateOf(false) }
    var newAlbumName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Додати до альбому") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (albums.isNotEmpty()) {
                    albums.forEach { album ->
                        ListItem(
                            headlineContent = { Text(album.name) },
                            supportingContent = { Text("${album.photoCount} фото") },
                            leadingContent = {
                                Icon(Icons.Default.PhotoAlbum, null)
                            },
                            modifier = Modifier.clickable {
                                onSelectAlbum(album.name)
                                onDismiss()
                            },
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }

                if (showCreateField) {
                    OutlinedTextField(
                        value = newAlbumName,
                        onValueChange = { newAlbumName = it },
                        label = { Text("Назва альбому") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    TextButton(
                        onClick = { showCreateField = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Створити новий альбом")
                    }
                }
            }
        },
        confirmButton = {
            if (showCreateField) {
                TextButton(
                    onClick = {
                        if (newAlbumName.isNotBlank()) {
                            onCreateAlbum(newAlbumName.trim())
                            onDismiss()
                        }
                    },
                    enabled = newAlbumName.isNotBlank(),
                ) { Text("Створити і додати") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Скасувати") }
        }
    )
}

@Composable
private fun CreateAlbumDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новий альбом") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Назва") },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onCreate(name.trim()) },
                enabled = name.isNotBlank(),
            ) { Text("Створити") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Скасувати") }
        }
    )
}
