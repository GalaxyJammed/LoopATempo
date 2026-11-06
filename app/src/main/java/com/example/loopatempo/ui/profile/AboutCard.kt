package com.example.loopatempo.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.loopatempo.ui.components.HeaderCard
import com.example.loopatempo.ui.components.SectionDivider
import com.example.loopatempo.ui.components.ShieldCard
import com.example.loopatempo.ui.components.verticalScrollbar

@Suppress("DEPRECATION")
@Composable
fun AboutScreen(contentPadding: PaddingValues, onBack: () -> Unit) {
    val context = LocalContext.current
    val version = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "unknown"
    }
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SectionDivider("Privacy")
                Text(
                    text = "Everything stays on your phone. No accounts, no internet, no analytics. " +
                            "Export a backup in Settings if you want a copy.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}