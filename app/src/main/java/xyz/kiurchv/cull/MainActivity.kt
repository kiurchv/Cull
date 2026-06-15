package xyz.kiurchv.cull

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import androidx.room.Room
import xyz.kiurchv.cull.data.db.AlbumDao
import xyz.kiurchv.cull.data.db.CullDatabase
import xyz.kiurchv.cull.data.db.PhotoHashDao
import xyz.kiurchv.cull.data.db.PhotoMetadataDao
import xyz.kiurchv.cull.ui.PermissionGate
import xyz.kiurchv.cull.ui.albums.AlbumContentScreen
import xyz.kiurchv.cull.ui.albums.AlbumsScreen
import xyz.kiurchv.cull.ui.gallery.GalleryScreen
import xyz.kiurchv.cull.ui.series.SeriesScreen
import xyz.kiurchv.cull.ui.duplicates.DuplicateGroupScreen
import xyz.kiurchv.cull.ui.trash.TrashReviewScreen
import xyz.kiurchv.cull.ui.viewer.PhotoViewerScreen
import xyz.kiurchv.cull.ui.settings.SettingsScreen
import xyz.kiurchv.cull.worker.IndexingWorker
import javax.inject.Singleton

// ---- Application ----

@HiltAndroidApp
class CullApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        WorkManager.getInstance(this).enqueueUniqueWork(
            IndexingWorker.WORK_NAME,
            androidx.work.ExistingWorkPolicy.REPLACE,
            IndexingWorker.buildOneTimeRequest(),
        )
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            IndexingWorker.WORK_NAME_PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP,
            IndexingWorker.buildPeriodicRequest(),
        )
    }
}

// ---- DI ----

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CullDatabase =
        Room.databaseBuilder(context, CullDatabase::class.java, "cull.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun providePhotoMetadataDao(db: CullDatabase): PhotoMetadataDao = db.photoMetadataDao()
    @Provides fun providePhotoHashDao(db: CullDatabase): PhotoHashDao = db.photoHashDao()
    @Provides fun provideAlbumDao(db: CullDatabase): AlbumDao = db.albumDao()
}

// ---- Navigation ----

private object Routes {
    const val GALLERY = "gallery"
    const val SERIES = "series/{seriesId}"
    const val VIEWER = "viewer/{mediaIds}/{startId}"
    const val DUPLICATE_GROUP = "duplicates/{groupId}"
    const val TRASH_REVIEW = "trash/{seriesId}"
    const val TRASH_REVIEW_ALL = "trash"
    fun trashReview(seriesId: String) = "trash/$seriesId"
    const val ALBUMS = "albums"
    const val ALBUM_CONTENT = "album/{albumName}"
    fun albumContent(name: String) = "album/$name"
    const val SETTINGS = "settings"
    fun series(id: String) = "series/$id"
    fun viewer(mediaIds: List<Long>, startId: Long) =
        "viewer/${mediaIds.joinToString(",")}/\${startId}"
    fun duplicateGroup(groupId: String) = "duplicates/$groupId"
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
    val seriesCache = remember { mutableStateMapOf<String, xyz.kiurchv.cull.data.model.Series>() }
    MaterialTheme {
        PermissionGate {
            NavHost(navController = navController, startDestination = Routes.GALLERY) {
                composable(Routes.GALLERY) {
                    GalleryScreen(
                        onSeriesClick = { series ->
                            seriesCache[series.id] = series
                            navController.navigate(Routes.series(series.id))
                        },
                        onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                        onAlbumsClick = { navController.navigate(Routes.ALBUMS) },
                    )
                }
                composable(Routes.SERIES) { backStack ->
                    val seriesId = backStack.arguments?.getString("seriesId") ?: return@composable
                    val series = seriesCache[seriesId] ?: return@composable
                    SeriesScreen(
                        series = series,
                        onBack = { navController.popBackStack() },
                        onPhotoClick = { mediaId, allIds ->
                            navController.navigate(Routes.viewer(allIds, mediaId))
                        },
                        onDuplicateGroupClick = { groupId ->
                            navController.navigate(Routes.duplicateGroup(groupId))
                        },
                        onTrashReviewClick = {
                            navController.navigate(Routes.trashReview(seriesId))
                        },
                    )
                }
                composable(Routes.TRASH_REVIEW_ALL) {
                    TrashReviewScreen(
                        seriesId = null,
                        onBack = { navController.popBackStack() },
                        onPhotoClick = { mediaId, allIds ->
                            navController.navigate(Routes.viewer(allIds, mediaId))
                        },
                    )
                }
                composable(Routes.TRASH_REVIEW) { backStack ->
                    val seriesId = backStack.arguments?.getString("seriesId")
                    TrashReviewScreen(
                        seriesId = seriesId,
                        onBack = { navController.popBackStack() },
                        onPhotoClick = { mediaId, allIds ->
                            navController.navigate(Routes.viewer(allIds, mediaId))
                        },
                    )
                }
                composable(Routes.DUPLICATE_GROUP) { backStack ->
                    val groupId = backStack.arguments?.getString("groupId") ?: return@composable
                    DuplicateGroupScreen(
                        groupId = groupId,
                        onBack = { navController.popBackStack() },
                        onPhotoClick = { mediaId, allIds ->
                            navController.navigate(Routes.viewer(allIds, mediaId))
                        },
                    )
                }
                composable(Routes.VIEWER) { backStack ->
                    val mediaIdsStr = backStack.arguments?.getString("mediaIds") ?: return@composable
                    val startId = backStack.arguments?.getString("startId")?.toLongOrNull() ?: return@composable
                    val mediaIds = mediaIdsStr.split(",").mapNotNull { it.toLongOrNull() }
                    PhotoViewerScreen(
                        mediaIds = mediaIds,
                        startMediaId = startId,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(Routes.ALBUMS) {
                    AlbumsScreen(
                        onBack = { navController.popBackStack() },
                        onAlbumClick = { name ->
                            navController.navigate(Routes.albumContent(name))
                        },
                    )
                }
                composable(Routes.ALBUM_CONTENT) { backStack ->
                    val albumName = backStack.arguments?.getString("albumName") ?: return@composable
                    AlbumContentScreen(
                        albumName = albumName,
                        onBack = { navController.popBackStack() },
                        onPhotoClick = { mediaId, allIds ->
                            navController.navigate(Routes.viewer(allIds, mediaId))
                        },
                    )
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}
