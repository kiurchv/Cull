package xyz.kiurchv.cull.ui.gallery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import xyz.kiurchv.cull.data.model.Series
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    onSeriesClick: (seriesId: String) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: GalleryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cull") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.series.isEmpty() -> EmptyState(Modifier.align(Alignment.Center))
                else -> SeriesList(
                    series = state.series,
                    onSeriesClick = onSeriesClick,
                )
            }
        }
    }
}

@Composable
private fun SeriesList(
    series: List<Series>,
    onSeriesClick: (String) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items(series, key = { it.id }) { s ->
            SeriesRow(series = s, onClick = { onSeriesClick(s.id) })
        }
    }
}

@Composable
private fun SeriesRow(series: Series, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("d MMMM yyyy", Locale.getDefault()) }
    val firstPhoto = series.batches.firstOrNull()?.groups?.firstOrNull()?.best

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Thumbnail
        Card(
            modifier = Modifier.size(64.dp),
            shape = MaterialTheme.shapes.small,
        ) {
            firstPhoto?.let {
                AsyncImage(
                    model = it.uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dateFormat.format(Date(series.date)),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = buildString {
                    append("${series.photoCount} фото")
                    append(" · ${series.batches.size} серій")
                    val dupes = series.batches.sumOf { b ->
                        b.groups.count { it.isDuplicate }
                    }
                    if (dupes > 0) append(" · $dupes груп дублікатів")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            series.centerLat?.let {
                Text(
                    text = "%.4f, %.4f".format(series.centerLat, series.centerLon),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }

        // Duplicate badge
        val dupeCount = series.batches.sumOf { b -> b.groups.count { it.isDuplicate } }
        if (dupeCount > 0) {
            Badge { Text("$dupeCount") }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Фото не знайдено", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "Переконайтесь що Cull має доступ до фото",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
