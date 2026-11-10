package com.example.loopatempo.ui.home

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    repository: HabitRepository,
    homeItems: List<HomeItem>,
    restDays: Int,
    contentPadding: PaddingValues,
    options: ListOptions,
    onAppCheckAttempt: () -> Unit
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
        item {
            RestDaysCard(restDays)
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
                is ListRow.Entry -> HabitRow(
                    item = row.item,
                    onCheckedChange = { checked ->
                        scope.launch { repository.setChecked(row.item.habit.id, todayEpoch, checked) }
                    },
                    onAppCheckAttempt = onAppCheckAttempt
                )
            }
        }
    }
}

@Composable
private fun RestDaysCard(count: Int) {
    val flame = Color(0xFF1E88E5)
    ShieldCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Rest days", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (count == 0) {
                        "Complete 5 habits in one day to earn one"
                    } else {
                        "Used automatically if a streak would break"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            repeat(count) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = "Rest day",
                    tint = flame,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(4.dp))
            }
        }
    }
}

@Composable
private fun HabitRow(
    item: HomeItem,
    onCheckedChange: (Boolean) -> Unit,
    onAppCheckAttempt: () -> Unit
) {
    val context = LocalContext.current
    val pkg = item.habit.packageName
    val website = item.habit.website
    val websiteHost = website?.let { WebsiteMatcher.normalizeHost(it) }
    val streakText = if (item.streak > 0) "Streak: ${item.streak} 🔥" else "Streak: ${item.streak}"

    ShieldCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.checkedToday,
                onCheckedChange = { checked ->
                    if (pkg != null) onAppCheckAttempt() else onCheckedChange(checked)
                }
            )
            when {
                pkg != null -> {
                    Spacer(Modifier.width(8.dp))
                    AppIcon(packageName = pkg, modifier = Modifier.size(40.dp))
                }
                item.habit.iconKey != null -> {
                    Spacer(Modifier.width(8.dp))
                    HabitIcon(item.habit.iconKey, Modifier.size(28.dp))
                }
                websiteHost != null -> {
                    Spacer(Modifier.width(8.dp))
                    WebsiteIcon(websiteHost, Modifier.size(28.dp))
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.habit.name, style = MaterialTheme.typography.titleMedium)
                Text(streakText, style = MaterialTheme.typography.bodyMedium)
            }
            if (website != null) {
                IconButton(onClick = { openWebsite(context, website) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Open website"
                    )
                }
            }
        }
    }
}

private fun openWebsite(context: Context, website: String) {
    val url = if (website.startsWith("http")) website else "https://$website"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        .addCategory(Intent.CATEGORY_BROWSABLE)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No browser found to open $url", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Couldn't open $url: ${e.message}", Toast.LENGTH_LONG).show()
    }
}