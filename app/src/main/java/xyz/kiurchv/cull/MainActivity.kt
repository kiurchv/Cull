package xyz.kiurchv.cull

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.room.Room
import xyz.kiurchv.cull.data.MediaStoreRepository
import xyz.kiurchv.cull.data.db.AlbumDao
import xyz.kiurchv.cull.data.db.CullDatabase
import xyz.kiurchv.cull.data.db.PhotoHashDao
import xyz.kiurchv.cull.data.model.Series
import xyz.kiurchv.cull.domain.GroupingEngine
import xyz.kiurchv.cull.domain.GroupingSettings
import xyz.kiurchv.cull.ui.PermissionGate
import xyz.kiurchv.cull.ui.albums.AlbumsScreen
import xyz.kiurchv.cull.ui.gallery.GalleryScreen
import xyz.kiurchv.cull.ui.review.ReviewScreen
import xyz.kiurchv.cull.ui.settings.SettingsScreen
import xyz.kiurchv.cull.worker.IndexingWorker
import javax.inject.Inject
import javax.inject.Singleton

// ---- Application ----

@HiltAndroidApp
class CullApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            IndexingWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            IndexingWorker.buildRequest(),
        )
        WorkManager.getInstance(this).enqueue(IndexingWorker.buildOneTimeRequest())
    }
}

// ---- DI Module ----

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CullDatabase =
        Room.databaseBuilder(context, CullDatabase::class.java, "cull.db").build()

    @Provides fun providePhotoHashDao(db: CullDatabase): PhotoHashDao = db.photoHashDao()
    @Provides fun provideAlbumDao(db: CullDatabase): AlbumDao = db.albumDao()
}

// ---- Shared ViewModel (holds Series list for navigation) ----

data class AppUiState(
    val series: List<Series> = emptyList(),
    val isLoading: Boolean = true,
    val settings: GroupingSettings = GroupingSettings(),
)

@HiltViewModel
class AppViewModel @Inject constructor(
    private val mediaStore: MediaStoreRepository,
    private val groupingEngine: GroupingEngine,
    private val hashDao: PhotoHashDao,
) : ViewModel() {

    private val _state = MutableStateFlow(AppUiState())
    val state: StateFlow<AppUiState> = _state.asStateFlow()

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
            val hashMap = hashEntities.associate { e -> e.mediaId to (e.pHash to e.sharpness) }
            val series = groupingEngine.groupSync(photos, _state.value.settings, hashMap)
            _state.update { it.copy(series = series, isLoading = false) }
        }
    }

    fun getSeriesById(id: String): Series? = _state.value.series.find { it.id == id }

    fun updateSettings(settings: GroupingSettings) {
        _state.update { it.copy(settings = settings) }
        loadPhotos()
    }
}

// ---- Navigation ----

private object Routes {
    const val GALLERY = "gallery"
    const val REVIEW = "review/{seriesId}"
    const val ALBUMS = "albums"
    const val SETTINGS = "settings"
    fun review(id: String) = "review/$id"
}

// ---- MainActivity ----

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { CullApp() }
    }
}

@Composable
private fun CullApp(appViewModel: AppViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val state by appViewModel.state.collectAsState()

    PermissionGate {
        MaterialThemeWrapper {
            NavHost(navController = navController, startDestination = Routes.GALLERY) {

                composable(Routes.GALLERY) {
                    GalleryScreen(
                        series = state.series,
                        isLoading = state.isLoading,
                        onSeriesClick = { seriesId ->
                            navController.navigate(Routes.review(seriesId))
                        },
                        onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                        onAlbumsClick = { navController.navigate(Routes.ALBUMS) },
                    )
                }

                composable(Routes.REVIEW) { backStack ->
                    val seriesId = backStack.arguments?.getString("seriesId") ?: return@composable
                    val series = appViewModel.getSeriesById(seriesId) ?: return@composable
                    ReviewScreen(
                        series = series,
                        onBack = { navController.popBackStack() },
                    )
                }

                composable(Routes.ALBUMS) {
                    AlbumsScreen(onBack = { navController.popBackStack() })
                }

                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        current = state.settings,
                        onSave = { appViewModel.updateSettings(it) },
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}

@Composable
private fun MaterialThemeWrapper(content: @Composable () -> Unit) {
    androidx.compose.material3.MaterialTheme(content = content)
}
