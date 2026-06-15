package xyz.kiurchv.cull.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import xyz.kiurchv.cull.domain.GroupingSettings
import android.content.Context
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val indexingStore: xyz.kiurchv.cull.data.IndexingStore,
) : ViewModel() {

    private val _settings = MutableStateFlow(GroupingSettings())
    val settings: StateFlow<GroupingSettings> = _settings.asStateFlow()

    val indexingState = indexingStore.state.stateIn(
        viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
        xyz.kiurchv.cull.data.IndexingState(),
    )

    fun save(settings: GroupingSettings) {
        _settings.value = settings
    }

    fun refreshIndex() {
        androidx.work.WorkManager.getInstance(context).enqueueUniqueWork(
            xyz.kiurchv.cull.worker.IndexingWorker.WORK_NAME,
            androidx.work.ExistingWorkPolicy.REPLACE,
            xyz.kiurchv.cull.worker.IndexingWorker.buildOneTimeRequest(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val current by viewModel.settings.collectAsStateWithLifecycle()
    val indexingState by viewModel.indexingState.collectAsStateWithLifecycle()

    // Local state for sliders
    var radiusMeters by remember(current) { mutableFloatStateOf(current.seriesRadiusMeters.toFloat()) }
    var batchSeconds by remember(current) { mutableFloatStateOf(current.batchIntervalSeconds.toFloat()) }
    var hashThreshold by remember(current) { mutableFloatStateOf(current.duplicateHashThreshold.toFloat()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Налаштування") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        viewModel.save(GroupingSettings(
                            seriesRadiusMeters = radiusMeters.toDouble(),
                            batchIntervalSeconds = batchSeconds.toLong(),
                            duplicateHashThreshold = hashThreshold.toInt(),
                        ))
                        onBack()
                    }) { Text("Зберегти") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            // Indexing status + refresh
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Default.Refresh, null)
                        Text("Індексування фото", style = MaterialTheme.typography.titleSmall)
                    }
                    Text(
                        when (indexingState.status) {
                            xyz.kiurchv.cull.data.IndexingStatus.RUNNING ->
                                indexingState.stageMessage.ifBlank { "Виконується…" }
                            xyz.kiurchv.cull.data.IndexingStatus.ERROR ->
                                "Помилка: ${indexingState.error ?: "невідома"}"
                            xyz.kiurchv.cull.data.IndexingStatus.SUCCESS ->
                                "Готово · ${indexingState.indexedDayCount} днів проіндексовано"
                            else -> "Очікування"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedButton(
                        onClick = viewModel::refreshIndex,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (indexingState.status == xyz.kiurchv.cull.data.IndexingStatus.RUNNING) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                        }
                        Text("Оновити індекс")
                    }
                }
            }

            // Series radius — steps of 100m from 100m to 5000m
            SteppedSlider(
                label = "Радіус серії",
                value = radiusMeters,
                onValueChange = { radiusMeters = it },
                valueRange = 100f..5000f,
                stepSize = 100f,
                display = "${radiusMeters.roundToInt()} м",
                description = "Фото в межах цієї відстані за один день — одна серія",
            )

            // Batch interval — steps of 5s from 5s to 120s
            SteppedSlider(
                label = "Інтервал батчу",
                value = batchSeconds,
                onValueChange = { batchSeconds = it },
                valueRange = 5f..120f,
                stepSize = 5f,
                display = "${batchSeconds.roundToInt()} с",
                description = "Фото зняті з меншим інтервалом — один батч",
            )

            // Duplicate threshold — steps of 1 from 1 to 20
            SteppedSlider(
                label = "Чутливість дублікатів",
                value = hashThreshold,
                onValueChange = { hashThreshold = it },
                valueRange = 1f..20f,
                stepSize = 1f,
                display = "${hashThreshold.roundToInt()} біт",
                description = "Менше = тільки майже ідентичні · Більше = схожі за змістом",
            )
        }
    }
}

@Composable
private fun SteppedSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    stepSize: Float,
    display: String,
    description: String,
) {
    val steps = ((valueRange.endInclusive - valueRange.start) / stepSize).toInt() - 1

    // Snap to nearest step
    val snappedValue = (((value - valueRange.start) / stepSize).roundToInt() * stepSize + valueRange.start)
        .coerceIn(valueRange.start, valueRange.endInclusive)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = MaterialTheme.typography.titleSmall)
            Text(display, style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = snappedValue,
            onValueChange = { raw ->
                val snapped = (((raw - valueRange.start) / stepSize).roundToInt() * stepSize + valueRange.start)
                    .coerceIn(valueRange.start, valueRange.endInclusive)
                onValueChange(snapped)
            },
            valueRange = valueRange,
            steps = steps,
        )
        Text(
            description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
