package com.example.loopatempo.ui.update

import android.content.Intent
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.loopatempo.data.HabitRepository
import com.example.loopatempo.service.isWebsiteServiceEnabled
import kotlinx.coroutines.flow.first

private const val LAST_VERSION_KEY = "app.lastVersionName"

@Composable
fun ServiceUpdateNotice(repository: HabitRepository) {
    val context = LocalContext.current
    var show by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val current = context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
        val stored = repository.readPref(LAST_VERSION_KEY)
        if (stored != null && stored != current) {
            val settings = repository.observeSettings().first()
            if (settings?.websiteAutoCheck == true && !isWebsiteServiceEnabled(context)) {
                show = true
            }
        }
        repository.savePref(LAST_VERSION_KEY, current)
    }

    if (show) {
        AlertDialog(
            onDismissRequest = { show = false },
            title = { Text("Accessibility was turned off") },
            text = {
                Text(
                    "LoopATempo was updated, and Android turns off Accessibility services after an app update. " +
                            "Website auto-check needs them. Turn LoopATempo back on to keep auto-checking websites."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    show = false
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }) {
                    Text("Open Accessibility")
                }
            },
            dismissButton = {
                TextButton(onClick = { show = false }) { Text("Later") }
            }
        )
    }
}