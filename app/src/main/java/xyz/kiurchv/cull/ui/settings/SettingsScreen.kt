package xyz.kiurchv.cull.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.kiurchv.cull.domain.GroupingSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    current: GroupingSettings,
    onSave: (GroupingSettings) -> Unit,
    onBack: () -> Unit,
) {
    var radiusMeters by remember { mutableFloatStateOf(current.seriesRadiusMeters.toFloat()) }
    var batchSeconds by remember { mutableFloatStateOf(current.batchIntervalSeconds.toFloat()) }
    var hashThreshold by remember { mutableFloatStateOf(current.duplicateHashThreshold.toFloat()) }

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
                        onSave(
                            GroupingSettings(
                                seriesRadiusMeters = radiusMeters.toDouble(),
                                batchIntervalSeconds = batchSeconds.toLong(),
                                duplicateHashThreshold = hashThreshold.toInt(),
                            )
                        )
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

            // Series radius
            SliderSetting(
                label = "Радіус серії",
                value = radiusMeters,
                onValueChange = { radiusMeters = it },
                valueRange = 100f..5000f,
                steps = 49,
                display = "%.0f м".format(radiusMeters),
                description = "Фото зняті в межах цього радіусу в один день об'єднуються в серію",
            )

            // Batch interval
            SliderSetting(
                label = "Інтервал батчу",
                value = batchSeconds,
                onValueChange = { batchSeconds = it },
                valueRange = 2f..120f,
                steps = 58,
                display = "%.0f с".format(batchSeconds),
                description = "Фото зняті з інтервалом менше цього часу — один батч",
            )

            // pHash threshold
            SliderSetting(
                label = "Чутливість дублікатів",
                value = hashThreshold,
                onValueChange = { hashThreshold = it },
                valueRange = 1f..20f,
                steps = 19,
                display = "%.0f біт".format(hashThreshold),
                description = "Менше = тільки майже ідентичні. Більше = схожі за змістом",
            )
        }
    }
}

@Composable
private fun SliderSetting(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    display: String,
    description: String,
) {
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
            value = value,
            onValueChange = onValueChange,
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
