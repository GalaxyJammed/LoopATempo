package com.example.loopatempo.ui.permissions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.loopatempo.LoopATempoApplication
import com.example.loopatempo.service.TrackingStatus
import com.example.loopatempo.ui.components.ShieldCard
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val RUNNING_WINDOW_MS = 2 * 60_000L

@Composable
fun TrackingStatusCard() {
    val context = LocalContext.current
    val repository = (context.applicationContext as LoopATempoApplication).repository
    val settings by repository.observeSettings().collectAsStateWithLifecycle(initialValue = null)
    val heartbeat by repository.observePref(TrackingStatus.HEARTBEAT_KEY)
        .collectAsStateWithLifecycle(initialValue = null)
    val boot by repository.observePref(TrackingStatus.BOOT_KEY)
        .collectAsStateWithLifecycle(initialValue = null)
    val now = System.currentTimeMillis()

    val keepAlive = settings?.keepAlive ?: false
    val ageMs = heartbeat?.toLongOrNull()?.let { now - it }
    val running = ageMs != null && ageMs < RUNNING_WINDOW_MS

    val statusText = when {
        !keepAlive -> "Background checks about every 15 minutes (Keep tracking alive is off)"
        running -> "Running. Last check ${ageText(ageMs ?: 0L)} ago"
        else -> "Not running. Open LoopATempo to start it again"
    }
    val bootText = boot?.split("|")?.takeIf { it.size == 2 }?.let { parts ->
        "After last restart (${formatTime(parts[0].toLongOrNull() ?: 0L)}): ${parts[1]}"
    }

    ShieldCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Background tracking", style = MaterialTheme.typography.titleMedium)
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = if (keepAlive && !running) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            bootText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun ageText(ms: Long): String {
    val seconds = ms / 1000
    return when {
        seconds < 60 -> "${seconds}s"
        seconds < 3600 -> "${seconds / 60}m"
        else -> "${seconds / 3600}h"
    }
}

private fun formatTime(millis: Long): String =
    DateTimeFormatter.ofPattern("MMM d, HH:mm", Locale.getDefault())
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(millis))