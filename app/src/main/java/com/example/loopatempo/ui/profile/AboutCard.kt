package com.example.loopatempo.ui.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.loopatempo.LoopATempoApplication
import com.example.loopatempo.data.ReleaseInfo
import com.example.loopatempo.data.ReleaseNotes
import com.example.loopatempo.ui.components.HeaderCard
import com.example.loopatempo.ui.components.SectionDivider
import com.example.loopatempo.ui.components.ShieldCard
import com.example.loopatempo.ui.components.verticalScrollbar
import com.example.loopatempo.update.ApkDownloader
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

private const val RELEASES_PAGE = "https://github.com/galaxyjammed/LoopATempo/releases"

private sealed interface ReleaseState {
    data object Loading : ReleaseState
    data class Loaded(val releases: List<ReleaseInfo>) : ReleaseState
    data object Failed : ReleaseState
}

private fun openUrl(context: Context, url: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

@Suppress("DEPRECATION")
@Composable
fun AboutScreen(contentPadding: PaddingValues, update: ReleaseInfo?, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val version = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "unknown"
    }
    var reloadKey by remember { mutableIntStateOf(0) }
    var state by remember { mutableStateOf<ReleaseState>(ReleaseState.Loading) }
    var updateStatus by remember { mutableStateOf<String?>(null) }
    var askInstallPermission by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()
    val warn = update != null
    val errorColor = MaterialTheme.colorScheme.error
    val errorContainer = MaterialTheme.colorScheme.errorContainer
    val onErrorContainer = MaterialTheme.colorScheme.onErrorContainer

    LaunchedEffect(reloadKey) {
        state = ReleaseState.Loading
        state = try {
            ReleaseState.Loaded(
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { ReleaseNotes.fetch() }
            )
        } catch (e: Exception) {
            ReleaseState.Failed
        }
    }

    fun startUpdate(release: ReleaseInfo) {
        val apk = release.apkUrl
        if (apk == null) {
            openUrl(context, release.url)
            return
        }
        if (!context.packageManager.canRequestPackageInstalls()) {
            askInstallPermission = true
            return
        }
        val id = ApkDownloader.start(context, apk, "LoopATempo-${release.tag}.apk")
        scope.launch {
            (context.applicationContext as LoopATempoApplication).repository
                .savePref(ApkDownloader.PENDING_KEY, id.toString())
        }
        updateStatus = "Downloading ${release.tag}. The installer opens when the download finishes."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp)
            .verticalScrollbar(scroll)
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeaderCard(title = "About", onBack = onBack)

        if (update != null) {
            ShieldCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = errorContainer,
                borderColor = errorColor
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "A new version is available",
                        style = MaterialTheme.typography.titleMedium,
                        color = onErrorContainer
                    )
                    Text(
                        text = "LoopATempo ${update.tag} is ready to download.",
                        style = MaterialTheme.typography.bodySmall,
                        color = onErrorContainer
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { startUpdate(update) }) {
                            Text("Update now")
                        }
                        OutlinedButton(onClick = { openUrl(context, update.url) }) {
                            Text("View Changelog")
                        }
                    }
                    updateStatus?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = onErrorContainer)
                    }
                }
            }
        }

        ShieldCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = if (warn) errorContainer else null,
            borderColor = if (warn) errorColor else null
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SectionDivider("LoopATempo")
                Text("Version $version", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Build daily streaks by checking off habits and the apps you use every day.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        ShieldCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = if (warn) errorContainer else null,
            borderColor = if (warn) errorColor else null
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionDivider("Release notes")
                when (val s = state) {
                    ReleaseState.Loading -> Text(
                        text = "Loading release notes…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ReleaseState.Failed -> {
                        Text(
                            text = "Couldn't load release notes. Check your connection and try again.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(onClick = { reloadKey++ }) { Text("Try again") }
                    }
                    is ReleaseState.Loaded -> {
                        if (s.releases.isEmpty()) {
                            Text(
                                text = "No releases published yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        s.releases.forEach { ReleaseEntry(it) }
                    }
                }
                OutlinedButton(
                    onClick = { openUrl(context, RELEASES_PAGE) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View all releases on GitHub")
                }
            }
        }

        ShieldCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SectionDivider("Privacy")
                Text(
                    text = "Your habits, check-ins and settings stay on your phone. Network use is limited to " +
                            "reading public release notes from GitHub, downloading an update when you tap " +
                            "Update now, and downloading website icons from the sites in your website tasks. " +
                            "If Website auto-check is on, LoopATempo reads only the domain in your browser's " +
                            "address bar, and never stores URLs or history. Nothing about you is sent anywhere else.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (askInstallPermission) {
        AlertDialog(
            onDismissRequest = { askInstallPermission = false },
            title = { Text("Allow installs from LoopATempo") },
            text = {
                Text(
                    "Android needs your permission before LoopATempo can install updates. Turn on " +
                            "\"Allow from this source\", then press Update now again."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    askInstallPermission = false
                    context.startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                            Uri.parse("package:${context.packageName}")
                        )
                    )
                }) {
                    Text("Open settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { askInstallPermission = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ReleaseEntry(release: ReleaseInfo) {
    val body = ReleaseNotes.clean(release.body)
    val date = formatDate(release.publishedAt)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = release.name, style = MaterialTheme.typography.titleSmall)
        Text(
            text = if (release.prerelease) "$date · Pre-release" else date,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (body.isNotEmpty()) {
            Text(text = body, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun formatDate(iso: String): String =
    runCatching {
        DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
            .withZone(ZoneId.systemDefault())
            .format(Instant.parse(iso))
    }.getOrDefault(iso)