package com.example.loopatempo.ui.filter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.loopatempo.ui.components.ShieldCard

@Composable
fun FilterDialog(
    options: ListOptions,
    allowStreak: Boolean,
    onChange: (ListOptions) -> Unit,
    onDismiss: () -> Unit
) {
    var onMainPage by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        ShieldCard(modifier = Modifier.wrapContentWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Filter",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "You can combine a filter from Main and a filter from Sub!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = onMainPage,
                        onClick = { onMainPage = true },
                        label = { Text("Main") }
                    )
                    FilterChip(
                        selected = !onMainPage,
                        onClick = { onMainPage = false },
                        label = { Text("Sub") }
                    )
                }

                if (onMainPage) {
                    MainOptions(options, onChange)
                } else {
                    SubOptions(options, allowStreak, onChange)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Done") }
                }
            }
        }
    }
}

@Composable
private fun MainOptions(options: ListOptions, onChange: (ListOptions) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Enable Main Filter",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = if (options.mainEnabled) "On" else "Off",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.width(8.dp))
        Switch(
            checked = options.mainEnabled,
            onCheckedChange = { onChange(options.copy(mainEnabled = it)) }
        )
    }

    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

    SectionTitle("Type")
    RadioOption(
        label = "Apps first",
        selected = options.main == MainChoice.APPS_FIRST,
        enabled = options.mainEnabled
    ) { onChange(options.copy(main = MainChoice.APPS_FIRST)) }
    RadioOption(
        label = "Custom tasks first",
        selected = options.main == MainChoice.CUSTOM_FIRST,
        enabled = options.mainEnabled
    ) { onChange(options.copy(main = MainChoice.CUSTOM_FIRST)) }

    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

    SectionTitle("Completion")
    RadioOption(
        label = "Completed",
        selected = options.main == MainChoice.COMPLETED_FIRST,
        enabled = options.mainEnabled
    ) { onChange(options.copy(main = MainChoice.COMPLETED_FIRST)) }
    RadioOption(
        label = "None Completed",
        selected = options.main == MainChoice.NOT_COMPLETED_FIRST,
        enabled = options.mainEnabled
    ) { onChange(options.copy(main = MainChoice.NOT_COMPLETED_FIRST)) }
}

@Composable
private fun SubOptions(options: ListOptions, allowStreak: Boolean, onChange: (ListOptions) -> Unit) {
    if (allowStreak) {
        SectionTitle("Streak")
        RadioOption("Highest first", options.sort == SortChoice.STREAK_HIGHEST) {
            onChange(options.copy(sort = SortChoice.STREAK_HIGHEST))
        }
        RadioOption("Lowest first", options.sort == SortChoice.STREAK_LOWEST) {
            onChange(options.copy(sort = SortChoice.STREAK_LOWEST))
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
    }

    SectionTitle("Alphabetical")
    RadioOption("A to Z", options.sort == SortChoice.NAME_AZ) {
        onChange(options.copy(sort = SortChoice.NAME_AZ))
    }
    RadioOption("Z to A", options.sort == SortChoice.NAME_ZA) {
        onChange(options.copy(sort = SortChoice.NAME_ZA))
    }

    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

    SectionTitle("Date created")
    RadioOption("Oldest first", options.sort == SortChoice.OLDEST) {
        onChange(options.copy(sort = SortChoice.OLDEST))
    }
    RadioOption("Newest first", options.sort == SortChoice.NEWEST) {
        onChange(options.copy(sort = SortChoice.NEWEST))
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun RadioOption(
    label: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null, enabled = enabled)
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            }
        )
    }
}