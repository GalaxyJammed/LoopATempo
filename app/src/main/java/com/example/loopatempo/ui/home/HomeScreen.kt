package com.example.loopatempo.ui.home

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.loopatempo.data.HabitRepository
import com.example.loopatempo.data.HomeItem
import com.example.loopatempo.ui.components.AppIcon
import com.example.loopatempo.ui.components.HeaderCard
import com.example.loopatempo.ui.components.SectionDivider
import com.example.loopatempo.ui.components.ShieldCard
import com.example.loopatempo.ui.components.lazyScrollbar
import com.example.loopatempo.ui.filter.ListOptions
import com.example.loopatempo.ui.filter.ListRow
import com.example.loopatempo.ui.filter.arrangeRows
import com.example.loopatempo.ui.icons.HabitIcon
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    repository: HabitRepository,
    homeItems: List<HomeItem>,
    contentPadding: PaddingValues,
    options: ListOptions
) {
    val today = LocalDate.now()
    val todayEpoch = today.toEpochDay()
    val rows = remember(homeItems, options) { arrangeRows(homeItems, options) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val dateText = today.format(DateTimeFormatter.ofPattern("MMM d, EEEE", Locale.getDefault()))
    val doneCount = homeItems.count { it.checkedToday }
    val subtitle = if (homeItems.isEmpty()) dateText else "$dateText  ·  $doneCount/${homeItems.size} done"

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
            HeaderCard(title = "LoopATempo", subtitle = subtitle)
        }
        if (homeItems.isEmpty()) {
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
                is ListRow.Entry -> HabitRow(row.item) { checked ->
                    scope.launch { repository.setChecked(row.item.habit.id, todayEpoch, checked) }
                }
            }
        }
    }
}

@Composable
private fun HabitRow(item: HomeItem, onCheckedChange: (Boolean) -> Unit) {
    val pkg = item.habit.packageName
    ShieldCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (pkg != null) {
                AppIcon(packageName = pkg, modifier = Modifier.size(40.dp))
            } else {
                Checkbox(checked = item.checkedToday, onCheckedChange = onCheckedChange)
                if (item.habit.iconKey != null) {
                    Spacer(Modifier.width(8.dp))
                    HabitIcon(item.habit.iconKey, Modifier.size(28.dp))
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.habit.name, style = MaterialTheme.typography.titleMedium)
                Text("Streak: ${item.streak}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}