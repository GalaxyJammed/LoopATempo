package com.example.loopatempo.ui.habits

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.loopatempo.data.HabitEntity
import com.example.loopatempo.data.HabitRepository
import com.example.loopatempo.data.HomeItem
import com.example.loopatempo.data.WebsiteMatcher
import com.example.loopatempo.ui.components.AppIcon
import com.example.loopatempo.ui.components.HeaderCard
import com.example.loopatempo.ui.components.SectionDivider
import com.example.loopatempo.ui.components.ShieldCard
import com.example.loopatempo.ui.components.WebsiteIcon
import com.example.loopatempo.ui.components.lazyScrollbar
import com.example.loopatempo.ui.filter.ListOptions
import com.example.loopatempo.ui.filter.ListRow
import com.example.loopatempo.ui.filter.arrangeRows
import com.example.loopatempo.ui.icons.HabitIcon
import kotlinx.coroutines.launch

@Composable
fun HabitsScreen(
    repository: HabitRepository,
    habitItems: List<HomeItem>,
    defaultThreshold: Int,
    defaultReminderMinutes: Int,
    reminderEnabled: Boolean,
    contentPadding: PaddingValues,
    options: ListOptions,
    onRequestDelete: (HabitEntity) -> Unit
) {
    val rows = remember(habitItems, options) { arrangeRows(habitItems, options) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var editing by remember { mutableStateOf<HabitEntity?>(null) }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp)
            .lazyScrollbar(listState),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            HeaderCard(title = "Edit")
        }
        if (habitItems.isEmpty()) {
            item {
                Text("No habits yet. Tap + to add one.", style = MaterialTheme.typography.bodyLarge)
            }
        }
        items(
            rows,
            key = { row ->
                when (row) {
                    is ListRow.Header -> "header-${row.title}"
                    is ListRow.Entry -> "habit-${row.item.habit.id}"
                }
            }
        ) { row ->
            when (row) {
                is ListRow.Header -> SectionDivider(row.title)
                is ListRow.Entry -> HabitManageRow(
                    habit = row.item.habit,
                    defaultThreshold = defaultThreshold,
                    onEdit = { editing = row.item.habit },
                    onDelete = { onRequestDelete(row.item.habit) }
                )
            }
        }
    }

    editing?.let { habit ->
        EditHabitDialog(
            habit = habit,
            defaultThreshold = defaultThreshold,
            defaultReminderMinutes = defaultReminderMinutes,
            reminderEnabled = reminderEnabled,
            onSave = { updated ->
                scope.launch { repository.updateHabit(updated) }
                editing = null
            },
            onDismiss = { editing = null }
        )
    }
}

@Composable
private fun HabitManageRow(
    habit: HabitEntity,
    defaultThreshold: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    val pkg = habit.packageName
    ShieldCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when {
                pkg != null -> AppIcon(packageName = pkg, modifier = Modifier.size(40.dp))
                habit.iconKey != null -> HabitIcon(habit.iconKey, Modifier.size(40.dp))
                habit.website != null -> WebsiteIcon(
                    WebsiteMatcher.normalizeHost(habit.website) ?: habit.website,
                    Modifier.size(40.dp)
                )
                else -> Box(
                    modifier = Modifier
                        .size(40.dp)
                        .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(habit.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (pkg != null) {
                        "Counts after ${habit.thresholdMinutes ?: defaultThreshold} min"
                    } else {
                        "Custom task"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                        onClick = {
                            menuOpen = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            menuOpen = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}