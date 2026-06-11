package xyz.kiurchv.cull.ui.viewer

import android.net.Uri
import android.provider.MediaStore
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import xyz.kiurchv.cull.data.PhotoRepository
import xyz.kiurchv.cull.data.db.PhotoMetadataDao
import xyz.kiurchv.cull.data.model.Photo
import javax.inject.Inject

// ---- ViewModel ----

data class ViewerUiState(
    val photos: List<Photo> = emptyList(),
    val currentIndex: Int = 0,
    val showControls: Boolean = true,
    val showDeleteConfirm: Boolean = false,
)

@HiltViewModel
class PhotoViewerViewModel @Inject constructor(
    private val photoRepository: PhotoRepository,
    private val photoMetadataDao: PhotoMetadataDao,
) : ViewModel() {

    private val _state = MutableStateFlow(ViewerUiState())
    val state: StateFlow<ViewerUiState> = _state.asStateFlow()

    val currentPhoto: Photo?
        get() = _state.value.photos.getOrNull(_state.value.currentIndex)

    fun load(mediaIds: List<Long>, startId: Long) {
        viewModelScope.launch {
            val msData = photoRepository.loadMediaStoreData(mediaIds)
            val metaMap = photoMetadataDao.getByIds(mediaIds)
                .associateBy { it.mediaId }

            val photos = mediaIds.mapNotNull { id ->
                val ms = msData[id] ?: return@mapNotNull null
                val meta = metaMap[id]
                Photo(
                    id = id,
                    uri = Uri.withAppendedPath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString()
                    ),
                    path = ms.path,
                    displayName = ms.displayName,
                    dateTaken = meta?.dateTaken ?: 0L,
                    width = ms.width,
                    height = ms.height,
                    size = ms.size,
                    mimeType = ms.mimeType,
                    isFavorite = ms.isFavorite,
                    pendingDelete = meta?.pendingDelete ?: false,
                )
            }

            val startIndex = photos.indexOfFirst { it.id == startId }.coerceAtLeast(0)
            _state.update { it.copy(photos = photos, currentIndex = startIndex) }
        }
    }

    fun onPageChanged(index: Int) {
        _state.update { it.copy(currentIndex = index) }
    }

    fun toggleControls() {
        _state.update { it.copy(showControls = !it.showControls) }
    }

    fun toggleFavorite() {
        val photo = currentPhoto ?: return
        viewModelScope.launch {
            photoRepository.setFavorite(photo.id, !photo.isFavorite)
            // Refresh current photo
            val updated = photoRepository.loadMediaStoreData(listOf(photo.id))
            val ms = updated[photo.id] ?: return@launch
            _state.update { s ->
                val newPhotos = s.photos.toMutableList().also { list ->
                    val idx = list.indexOfFirst { it.id == photo.id }
                    if (idx >= 0) list[idx] = list[idx].copy(isFavorite = ms.isFavorite)
                }
                s.copy(photos = newPhotos)
            }
        }
    }

    fun togglePendingDelete() {
        val photo = currentPhoto ?: return
        viewModelScope.launch {
            val newState = !photo.pendingDelete
            photoRepository.setPendingDelete(photo.id, newState)
            _state.update { s ->
                val newPhotos = s.photos.toMutableList().also { list ->
                    val idx = list.indexOfFirst { it.id == photo.id }
                    if (idx >= 0) list[idx] = list[idx].copy(pendingDelete = newState)
                }
                s.copy(photos = newPhotos)
            }
        }
    }

    fun requestDelete() {
        _state.update { it.copy(showDeleteConfirm = true) }
    }

    fun confirmDelete() {
        val photo = currentPhoto ?: return
        _state.update { it.copy(showDeleteConfirm = false) }
        viewModelScope.launch {
            photoRepository.deletePhoto(photo.id)
            _state.update { s ->
                val newPhotos = s.photos.filter { it.id != photo.id }
                val newIndex = s.currentIndex.coerceAtMost((newPhotos.size - 1).coerceAtLeast(0))
                s.copy(photos = newPhotos, currentIndex = newIndex)
            }
        }
    }

    fun cancelDelete() {
        _state.update { it.copy(showDeleteConfirm = false) }
    }
}

// ---- Screen ----

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoViewerScreen(
    mediaIds: List<Long>,
    startMediaId: Long,
    onBack: () -> Unit,
    viewModel: PhotoViewerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(mediaIds) { viewModel.load(mediaIds, startMediaId) }

    if (state.photos.isEmpty()) {
        Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = state.currentIndex,
        pageCount = { state.photos.size },
    )

    LaunchedEffect(pagerState.currentPage) {
        viewModel.onPageChanged(pagerState.currentPage)
    }

    val currentPhoto = state.photos.getOrNull(pagerState.currentPage)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { viewModel.toggleControls() },
    ) {
        // Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            val photo = state.photos.getOrNull(page) ?: return@HorizontalPager
            AsyncImage(
                model = photo.uri,
                contentDescription = photo.displayName,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Top bar
        AnimatedVisibility(
            visible = state.showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Назад", tint = Color.White)
                }
                Text(
                    text = "${pagerState.currentPage + 1} / ${state.photos.size}",
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }

        // Bottom action bar
        AnimatedVisibility(
            visible = state.showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .navigationBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Favorite
                IconButton(onClick = { viewModel.toggleFavorite() }) {
                    Icon(
                        if (currentPhoto?.isFavorite == true) Icons.Default.Favorite
                        else Icons.Default.FavoriteBorder,
                        contentDescription = "Улюблене",
                        tint = if (currentPhoto?.isFavorite == true) Color.Red else Color.White,
                    )
                }

                // Pending delete toggle
                IconButton(onClick = { viewModel.togglePendingDelete() }) {
                    Icon(
                        if (currentPhoto?.pendingDelete == true) Icons.Default.RestoreFromTrash
                        else Icons.Default.DeleteOutline,
                        contentDescription = if (currentPhoto?.pendingDelete == true)
                            "Зняти позначку" else "Позначити до видалення",
                        tint = if (currentPhoto?.pendingDelete == true)
                            MaterialTheme.colorScheme.error else Color.White,
                    )
                }

                // Delete immediately
                IconButton(onClick = { viewModel.requestDelete() }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Видалити в корзину",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        // Pending delete overlay indicator
        if (currentPhoto?.pendingDelete == true) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 56.dp, end = 12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.small,
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    "До видалення",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
    }

    // Delete confirmation dialog
    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            title = { Text("Видалити фото?") },
            text = { Text("Фото буде переміщено в корзину Android. Це можна скасувати через стандартну галерею.") },
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
