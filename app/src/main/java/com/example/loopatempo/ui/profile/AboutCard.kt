package com.example.loopatempo.ui.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.loopatempo.data.ReleaseInfo
import com.example.loopatempo.data.ReleaseNotes
import com.example.loopatempo.ui.components.HeaderCard
import com.example.loopatempo.ui.components.SectionDivider
import com.example.loopatempo.ui.components.ShieldCard
import com.example.loopatempo.ui.components.verticalScrollbar
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val RELEASES_PAGE = "https://github.com/galaxyjammed/LoopATempo/releases"

private sealed interface ReleaseState {
    data object Loading : ReleaseState
    data class Loaded(val releases: List<ReleaseInfo>) : ReleaseState
    data object Failed : ReleaseState
}

@Suppress("DEPRECATION")
@Composable
fun AboutScreen(contentPadding: PaddingValues, onBack: () -> Unit) {
    val context = LocalContext.current
    val version = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "unknown"
    }
    var reloadKey by remember { mutableIntStateOf(0) }
    var state by remember { mutableStateOf<ReleaseState>(ReleaseState.Loading) }
    val scroll = rememberScrollState()

    LaunchedEffect(reloadKey) {
        state = ReleaseState.Loading
        state = try {
            ReleaseState.Loaded(withContext(Dispatchers.IO) { ReleaseNotes.fetch() })
        } catch (e: Exception) {
            ReleaseState.Failed
        }
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

        ShieldCard(modifier = Modifier.fillMaxWidth()) {
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

        ShieldCard(modifier = Modifier.fillMaxWidth()) {
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
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(RELEASES_PAGE)))
                    },
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
                    text = "Everything you track stays on your phone. The only network request is an " +
                            "optional read of the public release notes from GitHub when you open this screen. " +
                            "Export a backup in Settings if you want a copy.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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