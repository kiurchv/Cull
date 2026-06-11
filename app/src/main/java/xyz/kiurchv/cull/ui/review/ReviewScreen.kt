package xyz.kiurchv.cull.ui.review

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
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
import xyz.kiurchv.cull.data.MediaStoreRepository
import xyz.kiurchv.cull.data.db.AlbumDao
import xyz.kiurchv.cull.data.model.*
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

// ---- ViewModel ----

data class ReviewUiState(
    val batches: List<Batch> = emptyList(),
    val currentBatchIndex: Int = 0,
    val currentGroupIndex: Int = 0,
    val currentPhotoIndex: Int = 0,
    val albums: List<Album> = emptyList(),
    val showAlbumPicker: Boolean = false,
    val done: Boolean = false,
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val mediaStore: MediaStoreRepository,
    private val hardLinkManager: HardLinkManager,
    private val albumDao: AlbumDao,
) : ViewModel() {

    private val _state = MutableStateFlow(ReviewUiState())
    val state: StateFlow<ReviewUiState> = _state.asStateFlow()

    fun loadSeries(series: Series) {
        viewModelScope.launch {
            val albums = albumDao.observeAll().first().map {
                Album(name = it.name, path = it.path, photoCount = 0)
            }
            _state.update {
                it.copy(
                    batches = series.batches,
                    albums = albums,
                    done = series.batches.isEmpty(),
                )
            }
        }
    }

    val currentPhoto: Photo?
        get() = with(_state.value) {
            batches.getOrNull(currentBatchIndex)
                ?.groups?.getOrNull(currentGroupIndex)
                ?.photos?.getOrNull(currentPhotoIndex)
        }

    fun trash() {
        val photo = currentPhoto ?: return
        viewModelScope.launch {
            mediaStore.trashPhoto(photo.id)
            advance()
        }
    }

    fun keep() = advance()

    fun toggleFavorite() {
        val photo = currentPhoto ?: return
        viewModelScope.launch {
            mediaStore.setFavorite(photo.id, !photo.isFavorite)
        }
    }

    fun addToAlbum(albumName: String) {
        val photo = currentPhoto ?: return
        viewModelScope.launch {
            hardLinkManager.addPhotoToAlbum(photo, albumName)
            _state.update { it.copy(showAlbumPicker = false) }
        }
    }

    fun showAlbumPicker() = _state.update { it.copy(showAlbumPicker = true) }
    fun hideAlbumPicker() = _state.update { it.copy(showAlbumPicker = false) }

    private fun advance() {
        _state.update { s ->
            val batch = s.batches.getOrNull(s.currentBatchIndex)
                ?: return@update s.copy(done = true)
            val group = batch.groups.getOrNull(s.currentGroupIndex)

            when {
                group != null && s.currentPhotoIndex + 1 < group.photos.size ->
                    s.copy(currentPhotoIndex = s.currentPhotoIndex + 1)
                s.currentGroupIndex + 1 < batch.groups.size ->
                    s.copy(currentGroupIndex = s.currentGroupIndex + 1, currentPhotoIndex = 0)
                s.currentBatchIndex + 1 < s.batches.size ->
                    s.copy(currentBatchIndex = s.currentBatchIndex + 1, currentGroupIndex = 0, currentPhotoIndex = 0)
                else -> s.copy(done = true)
            }
        }
    }
}

// ---- Screen ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    series: Series,
    onBack: () -> Unit,
    viewModel: ReviewViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(series.id) { viewModel.loadSeries(series) }

    if (state.done) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    val photo = viewModel.currentPhoto

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Серія ${state.currentBatchIndex + 1}/${state.batches.size}" +
                                " · Фото ${state.currentGroupIndex + 1}"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                },
            )
        },
        bottomBar = {
            BottomActionBar(
                isFavorite = photo?.isFavorite ?: false,
                onTrash = viewModel::trash,
                onKeep = viewModel::keep,
                onFavorite = viewModel::toggleFavorite,
                onAlbum = viewModel::showAlbumPicker,
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            photo?.let {
                SwipeablePhoto(
                    photo = it,
                    onSwipeLeft = viewModel::trash,
                    onSwipeRight = viewModel::keep,
                )
            }
        }
    }

    if (state.showAlbumPicker) {
        AlbumPickerSheet(
            albums = state.albums,
            onDismiss = viewModel::hideAlbumPicker,
            onSelect = viewModel::addToAlbum,
        )
    }
}

@Composable
private fun SwipeablePhoto(
    photo: Photo,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(offsetX, label = "swipe")
    val rotation = (animatedOffset / 20f).coerceIn(-15f, 15f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(photo.id) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        when {
                            offsetX < -200f -> { offsetX = 0f; onSwipeLeft() }
                            offsetX > 200f -> { offsetX = 0f; onSwipeRight() }
                            else -> offsetX = 0f
                        }
                    },
                    onHorizontalDrag = { _, delta -> offsetX += delta }
                )
            },
    ) {
        AsyncImage(
            model = photo.uri,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .rotate(rotation),
        )

        if (animatedOffset < -50f) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Видалити",
                    tint = Color.Red.copy(alpha = (abs(animatedOffset) / 300f).coerceIn(0f, 1f)),
                    modifier = Modifier.size(64.dp),
                )
            }
        }
        if (animatedOffset > 50f) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Залишити",
                    tint = Color.Green.copy(alpha = (animatedOffset / 300f).coerceIn(0f, 1f)),
                    modifier = Modifier.size(64.dp),
                )
            }
        }
    }
}

@Composable
private fun BottomActionBar(
    isFavorite: Boolean,
    onTrash: () -> Unit,
    onKeep: () -> Unit,
    onFavorite: () -> Unit,
    onAlbum: () -> Unit,
) {
    BottomAppBar {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onTrash) {
                Icon(Icons.Default.Delete, "Видалити", tint = MaterialTheme.colorScheme.error)
            }
            IconButton(onClick = onFavorite) {
                Icon(
                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    "Улюблене",
                    tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface,
                )
            }
            FilledTonalButton(onClick = onKeep) {
                Icon(Icons.Default.Check, null)
                Spacer(Modifier.width(8.dp))
                Text("Залишити")
            }
            IconButton(onClick = onAlbum) {
                Icon(Icons.Default.AddCircle, "До альбому")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlbumPickerSheet(
    albums: List<Album>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            "Додати до альбому",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        albums.forEach { album ->
            ListItem(
                headlineContent = { Text(album.name) },
                modifier = Modifier.clickable { onSelect(album.name) },
            )
        }
        Spacer(Modifier.height(32.dp))
    }
}
