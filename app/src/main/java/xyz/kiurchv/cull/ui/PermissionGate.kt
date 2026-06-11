package xyz.kiurchv.cull.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun PermissionGate(content: @Composable () -> Unit) {
    val context = LocalContext.current

    var hasMediaPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) ==
                        android.content.pm.PackageManager.PERMISSION_GRANTED
            } else {
                context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        android.content.pm.PackageManager.PERMISSION_GRANTED
            }
        )
    }

    var hasManageStorage by remember {
        mutableStateOf(Environment.isExternalStorageManager())
    }

    val mediaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasMediaPermission = results.values.all { it }
    }

    if (!hasMediaPermission) {
        PermissionScreen(
            title = "Доступ до фото",
            message = "Cull потребує доступу до ваших фото для відображення та сортування.",
            buttonText = "Надати доступ",
            onRequest = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    mediaLauncher.launch(
                        arrayOf(
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.ACCESS_MEDIA_LOCATION,
                        )
                    )
                } else {
                    mediaLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
                }
            }
        )
        return
    }

    if (!hasManageStorage) {
        PermissionScreen(
            title = "Доступ до файлової системи",
            message = "Cull потребує повного доступу до файлів для створення хардлінків в альбомах.\n\nЦе використовується виключно для DCIM/Albums.",
            buttonText = "Відкрити налаштування",
            onRequest = {
                val intent = Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
                context.startActivity(intent)
                hasManageStorage = Environment.isExternalStorageManager()
            }
        )
        return
    }

    content()
}

@Composable
private fun PermissionScreen(
    title: String,
    message: String,
    buttonText: String,
    onRequest: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onRequest) { Text(buttonText) }
    }
}
