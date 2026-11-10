package com.example.loopatempo.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.loopatempo.data.HabitRepository
import com.example.loopatempo.service.isWebsiteServiceEnabled
import com.example.loopatempo.ui.components.SettingRow
import kotlinx.coroutines.launch

@Composable
fun WebsiteAutoCheckRow(repository: HabitRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val settings by repository.observeSettings().collectAsStateWithLifecycle(initialValue = null)
    val enabled = settings?.websiteAutoCheck ?: false
    var serviceOn by remember { mutableStateOf(isWebsiteServiceEnabled(context)) }
    var explaining by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                serviceOn = isWebsiteServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val attention = enabled && !serviceOn
    val errorColor = MaterialTheme.colorScheme.error
    val errorContainer = MaterialTheme.colorScheme.errorContainer

    val subtitle = when {
        !enabled -> "Off: check website tasks yourself"
        serviceOn -> "On: visits to your website tasks are checked automatically"
        else -> "Accessibility is not on in settings. Turn it on to check website tasks automatically"
    }

    SettingRow(
        icon = Icons.Filled.Language,
        title = "Website auto-check",
        subtitle = subtitle,
        trailing = {
            Switch(
                checked = enabled,
                onCheckedChange = { on ->
                    if (on) {
                        explaining = true
                    } else {
                        scope.launch { repository.updateSettings { it.copy(websiteAutoCheck = false) } }
                    }
                }
            )
        },
        containerColor = if (attention) errorContainer else null,
        borderColor = if (attention) errorColor else null,
        subtitleColor = if (attention) errorColor else null
    )

    if (explaining) {
        AlertDialog(
            onDismissRequest = { explaining = false },
            title = { Text("Website auto-check") },
            text = {
                Text(
                    "LoopATempo will read the address bar of Chrome, Edge, Brave, Firefox, Samsung Internet " +
                            "and Opera. It only compares the domain with your website tasks and only saves a " +
                            "check-in. URLs and browsing history are never stored or sent.\n\n" +
                            "Android will ask you to turn on LoopATempo in Accessibility settings. On some phones " +
                            "you may first need to open LoopATempo's app info, tap the three dots, and choose " +
                            "Allow restricted settings."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    explaining = false
                    scope.launch { repository.updateSettings { it.copy(websiteAutoCheck = true) } }
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { explaining = false }) { Text("Cancel") }
            }
        )
    }
}