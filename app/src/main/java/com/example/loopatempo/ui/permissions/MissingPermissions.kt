package com.example.loopatempo.ui.permissions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.loopatempo.notify.StreakNotifier
import com.example.loopatempo.ui.components.HeaderCard
import com.example.loopatempo.ui.components.ShieldCard
import com.example.loopatempo.ui.components.verticalScrollbar

data class FixItem(
    val title: String,
    val body: String,
    val intent: Intent,
    val fixed: Boolean?
)

fun appDetailsIntent(context: Context): Intent =
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))

fun fixItems(context: Context): List<FixItem> {
    val pm = context.getSystemService(PowerManager::class.java)
    val batteryOk = pm.isIgnoringBatteryOptimizations(context.packageName)
    val notificationsOk = StreakNotifier.canPost(context)
    return listOf(
        FixItem(
            title = "Battery: No restrictions",
            body = "Stops Android from pausing LoopATempo in the background.",
            intent = Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:${context.packageName}")
            ),
            fixed = batteryOk
        ),
        FixItem(
            title = "Autostart: On",
            body = "If your phone has Autostart, Auto-launch, or Never sleeping for apps, turn it on for LoopATempo. The app can't check this setting.",
            intent = appDetailsIntent(context),
            fixed = null
        ),
        FixItem(
            title = "Notifications: Allowed",
            body = "Streak alerts can't appear until notifications are allowed.",
            intent = appDetailsIntent(context),
            fixed = notificationsOk
        )
    )
}

@Composable
fun rememberFixItems(): List<FixItem> {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var items by remember { mutableStateOf(fixItems(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                items = fixItems(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return items
}

@Composable
fun MissingPermissionsButton(onFix: () -> Unit, modifier: Modifier = Modifier) {
    val items = rememberFixItems()
    if (items.none { it.fixed == false }) return

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Missing Permissions",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "Tracking can stop in the background until this is fixed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(Modifier.width(12.dp))
            Button(onClick = onFix) { Text("Fix") }
        }
    }
}

@Composable
fun FixScreen(contentPadding: PaddingValues, onBack: () -> Unit) {
    val context = LocalContext.current
    val items = rememberFixItems()
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp)
            .verticalScrollbar(scroll)
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeaderCard(title = "Fix", onBack = onBack)
        items.forEach { item ->
            ShieldCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(item.title, style = MaterialTheme.typography.titleMedium)
                    Text(item.body, style = MaterialTheme.typography.bodySmall)
                    if (item.fixed == true) {
                        Text(
                            text = "Done",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Button(onClick = { context.startActivity(item.intent) }) {
                            Text("Open")
                        }
                    }
                }
            }
        }
    }
}