package com.example.loopatempo.ui.add

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.loopatempo.data.LaunchableApp
import com.example.loopatempo.data.loadLaunchableApps
import com.example.loopatempo.ui.components.MinuteOfDayPicker
import com.example.loopatempo.ui.components.ShieldCard
import com.example.loopatempo.ui.components.formatMinutes
import com.example.loopatempo.ui.components.lazyScrollbar
import com.example.loopatempo.ui.icons.IconPicker
import com.example.loopatempo.data.WebsiteMatcher
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.loopatempo.ui.components.verticalScrollbar

@Composable
fun AddChoiceDialog(
    onDismiss: () -> Unit,
    onCustom: () -> Unit,
    onApp: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        ShieldCard(modifier = Modifier.wrapContentWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Add streak",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                OutlinedButton(
                    onClick = onApp,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Filled.Apps, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("App")
                }
                OutlinedButton(
                    onClick = onCustom,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Custom task")
                }
            }
        }
    }
}

@Composable
fun CustomTaskDialog(
    reminderEnabled: Boolean,
    defaultReminderMinutes: Int,
    onDismiss: () -> Unit,
    onAdd: (String, String?, Int?, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var iconKey by remember { mutableStateOf<String?>(null) }
    var website by remember { mutableStateOf("") }
    var customTime by remember { mutableStateOf(false) }
    var chosenMinutes by remember { mutableStateOf(defaultReminderMinutes) }
    var pickingTime by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()
    val websiteValid = website.isBlank() || WebsiteMatcher.normalizeHost(website) != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom task") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 440.dp)
                    .verticalScrollbar(scroll)
                    .verticalScroll(scroll),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("e.g. Drink 10 cups of water") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = website,
                    onValueChange = { website = it },
                    label = { Text("Website (optional)") },
                    placeholder = { Text("youtube.com") },
                    supportingText = {
                        Text(
                            if (websiteValid) {
                                "Enter only the domain, like youtube.com. Don't add https://."
                            } else {
                                "That doesn't look like a website address."
                            }
                        )
                    },
                    singleLine = true,
                    isError = !websiteValid
                )
                IconPicker(
                    selectedKey = iconKey,
                    onSelect = { iconKey = it },
                    clearLabel = if (website.isNotBlank()) "Website icon" else "No icon",
                    hint = if (website.isNotBlank()) "Tap ✕ to use the website icon" else null
                )
                if (reminderEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Custom reminder time",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(checked = customTime, onCheckedChange = { customTime = it })
                    }
                    Text(
                        text = if (customTime) {
                            "Reminds at ${formatMinutes(chosenMinutes)}"
                        } else {
                            "Uses default (${formatMinutes(defaultReminderMinutes)})"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (customTime) {
                        OutlinedButton(onClick = { pickingTime = true }) {
                            Text("Set time")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onAdd(
                        name.trim(),
                        iconKey,
                        if (reminderEnabled && customTime) chosenMinutes else null,
                        website.trim().ifBlank { null }
                    )
                },
                enabled = name.isNotBlank() && websiteValid
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (pickingTime) {
        MinuteOfDayPicker(
            title = "Reminder time",
            initialMinutes = chosenMinutes,
            onConfirm = {
                chosenMinutes = it
                pickingTime = false
            },
            onDismiss = { pickingTime = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPickerSheet(
    trackedPackages: Set<String>,
    onDismiss: () -> Unit,
    onPick: (LaunchableApp) -> Unit
) {
    val context = LocalContext.current
    val apps = remember {
        loadLaunchableApps(context).filter { it.packageName !in trackedPackages }
    }
    var query by remember { mutableStateOf("") }
    val shown = apps.filter { it.label.contains(query.trim(), ignoreCase = true) }
    val listState = rememberLazyListState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search apps") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .lazyScrollbar(listState)
            ) {
                items(shown, key = { it.packageName }) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(app) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = app.icon,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = app.label,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}