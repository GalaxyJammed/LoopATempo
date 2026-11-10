package com.example.loopatempo.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.loopatempo.data.HabitRepository
import com.example.loopatempo.data.SettingsEntity
import com.example.loopatempo.notify.StreakNotifier
import com.example.loopatempo.ui.backup.LocalBackupRows
import com.example.loopatempo.ui.components.HeaderCard
import com.example.loopatempo.ui.components.MinuteOfDayPicker
import com.example.loopatempo.ui.components.SectionDivider
import com.example.loopatempo.ui.components.SettingRow
import com.example.loopatempo.ui.components.formatMinutes
import com.example.loopatempo.ui.components.verticalScrollbar
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    repository: HabitRepository,
    contentPadding: PaddingValues,
    onBack: () -> Unit
) {
    val settings by repository.observeSettings().collectAsStateWithLifecycle(initialValue = null)
    val current = settings ?: SettingsEntity()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val scroll = rememberScrollState()
    var editingThreshold by remember { mutableStateOf(false) }
    var editingReminderTime by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scope.launch { repository.updateSettings { it.copy(notificationsEnabled = true) } }
        }
    }

    val reminderPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    val onNotificationsToggle: (Boolean) -> Unit = { on ->
        val alreadyAllowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        when {
            !on -> scope.launch { repository.updateSettings { it.copy(notificationsEnabled = false) } }
            alreadyAllowed -> scope.launch { repository.updateSettings { it.copy(notificationsEnabled = true) } }
            else -> permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val onReminderToggle: (Boolean) -> Unit = { on ->
        scope.launch { repository.updateSettings { it.copy(reminderEnabled = on) } }
        if (on && !StreakNotifier.canPost(context)) {
            reminderPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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
        HeaderCard(title = "Settings", onBack = onBack)

        SectionDivider("Appearance")
        SettingRow(
            icon = Icons.Filled.DarkMode,
            title = "Dark mode",
            subtitle = if (current.darkTheme) "Dark" else "Light",
            trailing = {
                Switch(
                    checked = current.darkTheme,
                    onCheckedChange = { on ->
                        scope.launch { repository.updateSettings { it.copy(darkTheme = on) } }
                    }
                )
            }
        )

        SectionDivider("Tasks")
        SettingRow(
            icon = Icons.Filled.Timer,
            title = "Default streak time",
            subtitle = "Apps you haven't changed in Edit count after this",
            onClick = { editingThreshold = true },
            trailing = { Text("${current.defaultThresholdMinutes} min") }
        )
        SettingRow(
            icon = Icons.Filled.Sync,
            title = "Keep tracking alive",
            subtitle = if (current.keepAlive) {
                "On: silent notification, checks every 30 seconds"
            } else {
                "Off: no notification, checks about every 15 minutes"
            },
            trailing = {
                Switch(
                    checked = current.keepAlive,
                    onCheckedChange = { on ->
                        scope.launch { repository.updateSettings { it.copy(keepAlive = on) } }
                    }
                )
            }
        )
        WebsiteAutoCheckRow(repository)

        SectionDivider("Reminders")
        SettingRow(
            icon = Icons.Filled.Alarm,
            title = "Habit reminders",
            subtitle = "Remind me about habits I haven't completed",
            trailing = {
                Switch(
                    checked = current.reminderEnabled,
                    onCheckedChange = onReminderToggle
                )
            }
        )
        if (current.reminderEnabled) {
            SettingRow(
                icon = Icons.Filled.Schedule,
                title = "Default reminder time",
                subtitle = "Used for habits without their own time",
                onClick = { editingReminderTime = true },
                trailing = { Text(formatMinutes(current.reminderMinutes)) }
            )
        }
        SettingRow(
            icon = Icons.Filled.Notifications,
            title = "Streak completed alerts",
            subtitle = "Notify me when an app streak is completed",
            trailing = {
                Switch(
                    checked = current.notificationsEnabled,
                    onCheckedChange = onNotificationsToggle
                )
            }
        )

        SectionDivider("Backup")
        LocalBackupRows()
    }

    if (editingThreshold) {
        ThresholdDialog(
            current = current.defaultThresholdMinutes,
            onSave = { minutes ->
                scope.launch { repository.updateSettings { it.copy(defaultThresholdMinutes = minutes) } }
                editingThreshold = false
            },
            onDismiss = { editingThreshold = false }
        )
    }

    if (editingReminderTime) {
        MinuteOfDayPicker(
            title = "Default reminder time",
            initialMinutes = current.reminderMinutes,
            onConfirm = { minutes ->
                scope.launch { repository.updateSettings { it.copy(reminderMinutes = minutes) } }
                editingReminderTime = false
            },
            onDismiss = { editingReminderTime = false }
        )
    }
}

@Composable
private fun ThresholdDialog(
    current: Int,
    onSave: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(current.toString()) }
    val parsed = text.toIntOrNull()?.takeIf { it in 1..999 }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Default streak time") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.filter(Char::isDigit).take(3) },
                label = { Text("Minutes") },
                supportingText = { Text("Used for apps you haven't changed in Edit") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            TextButton(
                enabled = parsed != null,
                onClick = { parsed?.let(onSave) }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}