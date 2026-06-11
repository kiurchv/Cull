package xyz.kiurchv.cull.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import xyz.kiurchv.cull.data.MediaStoreRepository
import xyz.kiurchv.cull.data.db.PhotoHashDao
import xyz.kiurchv.cull.domain.GroupingEngine
import xyz.kiurchv.cull.domain.GroupingSettings
import xyz.kiurchv.cull.data.model.Series
import javax.inject.Inject

data class GalleryUiState(
    val series: List<Series> = emptyList(),
    val isLoading: Boolean = true,
    val settings: GroupingSettings = GroupingSettings(),
)

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val mediaStore: MediaStoreRepository,
    private val groupingEngine: GroupingEngine,
    private val hashDao: PhotoHashDao,
) : ViewModel() {

    private val _state = MutableStateFlow(GalleryUiState())
    val state: StateFlow<GalleryUiState> = _state.asStateFlow()

    init {
        loadPhotos()
        viewModelScope.launch {
            hashDao.observeAll().collect { loadPhotos() }
        }
    }

    fun loadPhotos() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val photos = mediaStore.loadPhotos()
            val hashEntities = hashDao.observeAll().first()
            val hashMap = hashEntities.associate { e ->
                e.mediaId to (e.pHash to e.sharpness)
            }

            val series = groupingEngine.groupSync(photos, _state.value.settings, hashMap)
            _state.update { it.copy(series = series, isLoading = false) }
        }
    }

    fun updateSettings(settings: GroupingSettings) {
        _state.update { it.copy(settings = settings) }
        loadPhotos()
    }
}
