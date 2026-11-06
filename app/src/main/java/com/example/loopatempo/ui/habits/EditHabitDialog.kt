package com.example.loopatempo.ui.habits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.loopatempo.data.HabitEntity
import com.example.loopatempo.ui.icons.IconPicker

@Composable
fun EditHabitDialog(
    habit: HabitEntity,
    defaultThreshold: Int,
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                    IconPicker(selectedKey = iconKey, onSelect = { iconKey = it })
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
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
                            iconKey = if (isApp) habit.iconKey else iconKey
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
}