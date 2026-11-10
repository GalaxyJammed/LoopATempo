package com.example.loopatempo.ui.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.loopatempo.data.HabitRepository
import com.example.loopatempo.data.SettingsEntity
import com.example.loopatempo.ui.components.SettingRow
import com.example.loopatempo.widget.HabitWidgetProvider
import kotlinx.coroutines.launch

@Composable
fun WidgetSettingsRows(repository: HabitRepository, current: SettingsEntity) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val groupRaw by repository.observePref(HabitWidgetProvider.GROUP_KEY)
        .collectAsStateWithLifecycle(initialValue = null)
    val grouped = groupRaw == "ON"

    SettingRow(
        icon = Icons.Filled.VisibilityOff,
        title = "Hide completed tasks",
        subtitle = "Leave finished tasks out of the widget",
        trailing = {
            Switch(
                checked = current.widgetHideCompleted,
                onCheckedChange = { on ->
                    scope.launch {
                        repository.updateSettings { it.copy(widgetHideCompleted = on) }
                        HabitWidgetProvider.refreshAll(context)
                    }
                }
            )
        }
    )
    SettingRow(
        icon = Icons.Filled.Visibility,
        title = "Cross out completed tasks",
        subtitle = "Strike through tasks you've finished in the widget",
        trailing = {
            Switch(
                checked = current.widgetStrikeCompleted,
                onCheckedChange = { on ->
                    scope.launch {
                        repository.updateSettings { it.copy(widgetStrikeCompleted = on) }
                        HabitWidgetProvider.refreshAll(context)
                    }
                }
            )
        }
    )
    SettingRow(
        icon = Icons.Filled.FilterList,
        title = "Widget filter",
        subtitle = "Filter tasks by Not Completed/Completed",
        trailing = {
            Switch(
                checked = grouped,
                onCheckedChange = { on ->
                    scope.launch {
                        repository.savePref(HabitWidgetProvider.GROUP_KEY, if (on) "ON" else "OFF")
                        HabitWidgetProvider.refreshAll(context)
                    }
                }
            )
        }
    )
}