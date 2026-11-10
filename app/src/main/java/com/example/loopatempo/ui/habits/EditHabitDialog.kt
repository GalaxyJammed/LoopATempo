package com.example.loopatempo.ui.habits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.loopatempo.data.HabitEntity
import com.example.loopatempo.data.WebsiteMatcher
import com.example.loopatempo.ui.components.MinuteOfDayPicker
import com.example.loopatempo.ui.components.formatMinutes
import com.example.loopatempo.ui.components.verticalScrollbar
import com.example.loopatempo.ui.icons.IconPicker

@Composable
fun EditHabitDialog(
    habit: HabitEntity,
    defaultThreshold: Int,
    defaultReminderMinutes: Int,
    reminderEnabled: Boolean,
    onSave: (HabitEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val isApp = habit.packageName != null
    var name by remember { mutableStateOf(habit.name) }
    var threshold by remember {
        mutableStateOf((habit.thresholdMinutes ?: defaultThreshold).toString())
    }
    var thresholdEdited by remember { mutableStateOf(false) }
    var iconKey by remember { mutableStateOf(habit.iconKey) }
    var website by remember { mutableStateOf(habit.website.orEmpty()) }
    var reminder by remember { mutableStateOf(habit.reminderMinutes) }
    var pickingTime by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()
    val websiteValid = isApp || website.isBlank() || WebsiteMatcher.normalizeHost(website) != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit") },
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
                    label = { Text("Name") },
                    singleLine = true
                )
                if (isApp) {
                    OutlinedTextField(
                        value = threshold,
                        onValueChange = {
                            threshold = it.filter(Char::isDigit).take(3)
                            thresholdEdited = true
                        },
                        label = { Text("Minutes to count as a streak") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                } else {
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
                }

                HorizontalDivider()
                if (reminderEnabled) {
                    Text(
                        text = "Reminder",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = reminder?.let { "Set to ${formatMinutes(it)}" }
                            ?: "Uses default (${formatMinutes(defaultReminderMinutes)})",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { pickingTime = true }) {
                            Text("Set time")
                        }
                        if (reminder != null) {
                            TextButton(onClick = { reminder = null }) {
                                Text("Use default")
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Enable Habit Reminders",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && websiteValid,
                onClick = {
                    onSave(
                        habit.copy(
                            name = name.trim(),
                            thresholdMinutes = if (isApp) {
                                if (thresholdEdited) {
                                    threshold.toIntOrNull()?.takeIf { it > 0 }
                                } else {
                                    habit.thresholdMinutes
                                }
                            } else {
                                null
                            },
                            iconKey = if (isApp) habit.iconKey else iconKey,
                            website = if (isApp) null else website.trim().ifBlank { null },
                            reminderMinutes = if (reminderEnabled) reminder else habit.reminderMinutes
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (pickingTime) {
        MinuteOfDayPicker(
            title = "Reminder time",
            initialMinutes = reminder ?: defaultReminderMinutes,
            onConfirm = { minutes ->
                reminder = minutes
                pickingTime = false
            },
            onDismiss = { pickingTime = false }
        )
    }
}