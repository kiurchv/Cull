package xyz.kiurchv.cull

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.hilt.work.HiltWorkerFactory
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
import dagger.hilt.components.SingletonComponent
import xyz.kiurchv.cull.data.db.CullDatabase
import xyz.kiurchv.cull.domain.GroupingSettings
import xyz.kiurchv.cull.ui.albums.AlbumsScreen
import xyz.kiurchv.cull.ui.gallery.GalleryScreen
import xyz.kiurchv.cull.ui.review.ReviewScreen
import xyz.kiurchv.cull.ui.settings.SettingsScreen
import xyz.kiurchv.cull.worker.IndexingWorker
import javax.inject.Inject
import androidx.room.Room
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton
import xyz.kiurchv.cull.data.db.AlbumDao
import xyz.kiurchv.cull.data.db.PhotoHashDao

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
        // Kick off initial indexing
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            IndexingWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            IndexingWorker.buildRequest(),
        )
        // Also run once immediately on first launch
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

// ---- Navigation ----

private sealed class Screen(val route: String) {
    object Gallery : Screen("gallery")
    object Review : Screen("review/{seriesId}") {
        fun go(id: String) = "review/$id"
    }
    object Albums : Screen("albums")
    object Settings : Screen("settings")
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
private fun CullApp() {
    val navController = rememberNavController()
    // Shared grouping settings state — passed down to GalleryScreen
    var settings by remember { mutableStateOf(GroupingSettings()) }

    // NOTE: Series objects are passed via a simple in-memory cache for the Review screen
    // In production this could be a shared ViewModel at nav graph level
    val seriesCache = remember { mutableStateMapOf<String, xyz.kiurchv.cull.data.model.Series>() }

    MaterialTheme {
        NavHost(navController = navController, startDestination = Screen.Gallery.route) {

            composable(Screen.Gallery.route) {
                GalleryScreen(
                    onSeriesClick = { seriesId ->
                        navController.navigate(Screen.Review.go(seriesId))
                    },
                    onSettingsClick = { navController.navigate(Screen.Settings.route) },
                )
            }

            composable(Screen.Review.route) { backStack ->
                val seriesId = backStack.arguments?.getString("seriesId") ?: return@composable
                val series = seriesCache[seriesId] ?: return@composable
                ReviewScreen(
                    series = series,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Screen.Albums.route) {
                AlbumsScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    current = settings,
                    onSave = { settings = it },
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
