package com.example.loopatempo.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.loopatempo.data.AppMinutes
import com.example.loopatempo.data.CheckInEntity
import com.example.loopatempo.data.HabitEntity
import com.example.loopatempo.data.HabitStats
import com.example.loopatempo.data.UsageMinutesEntity
import com.example.loopatempo.data.buildAnalytics
import com.example.loopatempo.data.buildAppMinutes
import com.example.loopatempo.data.buildMonthStats
import com.example.loopatempo.data.formatWeekdays
import com.example.loopatempo.data.monthsWithData
import com.example.loopatempo.data.productiveWeekdays
import com.example.loopatempo.data.productiveWeekdaysIn
import com.example.loopatempo.share.shareAnalyticsCard
import com.example.loopatempo.share.shareStreakCard
import com.example.loopatempo.ui.components.AppIcon
import com.example.loopatempo.ui.components.HeaderCard
import com.example.loopatempo.ui.components.SectionDivider
import com.example.loopatempo.ui.components.ShieldCard
import com.example.loopatempo.ui.components.lazyScrollbar
import com.example.loopatempo.ui.components.verticalScrollbar
import com.example.loopatempo.ui.icons.HabitIcon
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun AnalyticsScreen(
    habits: List<HabitEntity>,
    checkIns: List<CheckInEntity>,
    usage: List<UsageMinutesEntity>,
    contentPadding: PaddingValues
) {
    val context = LocalContext.current
    val today = LocalDate.now()
    val summary = remember(habits, checkIns) { buildAnalytics(habits, checkIns) }
    val allTimeWeekdays = remember(checkIns) { productiveWeekdays(checkIns) }
    val appMinutes = remember(habits, usage) { buildAppMinutes(habits, usage) }
    val scroll = rememberScrollState()

    val appShare = summary.bestApp?.let { best ->
        { shareStreakCard(context, best.habit, best.longestStreak) }
    }
    val customShare = summary.bestCustom?.let { best ->
        { shareStreakCard(context, best.habit, best.longestStreak) }
    }
    val summaryLines = listOf(
        "Active days: ${summary.activeDays}",
        "Apps: ${summary.appCount}  ·  Custom tasks: ${summary.customCount}",
        "Best app streak: " +
                (summary.bestApp?.let { "${it.habit.name} · ${it.longestStreak} days" } ?: "none yet"),
        "Best custom streak: " +
                (summary.bestCustom?.let { "${it.habit.name} · ${it.longestStreak} days" } ?: "none yet"),
        "All-time productive weekday: ${formatWeekdays(allTimeWeekdays)}"
    )
    val summaryShare: () -> Unit = {
        shareAnalyticsCard(context, "${summary.totalCheckIns} check-ins", summaryLines)
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
        HeaderCard(title = "Analytics")

        StatCard(title = "Best streak on app", onShare = appShare) {
            BestStreakRow(summary.bestApp, "No app streaks yet")
        }

        StatCard(title = "Best streak on custom task", onShare = customShare) {
            BestStreakRow(summary.bestCustom, "No custom task streaks yet")
        }

        StatCard(title = "Minutes per app") {
            AppMinutesList(appMinutes)
        }

        StatCard(title = "Check-ins by weekday") {
            val weekdays = DayOfWeek.entries
            BarChart(
                values = weekdays.map { summary.weekdayCounts[it] ?: 0 },
                labels = weekdays.map { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) },
                highlight = allTimeWeekdays?.singleOrNull()?.let { weekdays.indexOf(it) }
            )
        }

        MonthlyReviewCard(checkIns = checkIns, today = today)

        StatCard(title = "Summary", onShare = summaryShare) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatBox(
                    label = "Total check-ins",
                    value = summary.totalCheckIns.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    label = "Total active days",
                    value = summary.activeDays.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatBox(
                    label = "Apps",
                    value = summary.appCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    label = "Custom tasks",
                    value = summary.customCount.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = "All-time productive weekday: ${formatWeekdays(allTimeWeekdays)}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun StatCard(title: String, onShare: (() -> Unit)? = null, content: @Composable () -> Unit) {
    ShieldCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    SectionDivider(title)
                }
                if (onShare != null) {
                    IconButton(onClick = onShare) {
                        Icon(imageVector = Icons.Filled.Share, contentDescription = "Share $title")
                    }
                }
            }
            content()
        }
    }
}

@Composable
private fun BestStreakRow(best: HabitStats?, emptyText: String) {
    if (best == null) {
        Text(emptyText, style = MaterialTheme.typography.bodyLarge)
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HabitGlyph(best.habit, Modifier.size(40.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(best.habit.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${best.longestStreak} days in a row",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun AppMinutesList(items: List<AppMinutes>) {
    if (items.isEmpty()) {
        Text(
            text = "No app time recorded yet. It appears once an app you added is used.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        val max = items.first().totalMillis.coerceAtLeast(1L)
        val total = items.sumOf { it.totalMillis }
        Text(
            text = "Total: ${formatDuration(total)}",
            style = MaterialTheme.typography.bodyMedium
        )
        items.take(8).forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                HabitGlyph(item.habit, Modifier.size(28.dp))
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = item.habit.name,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = formatDuration(item.totalMillis),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    LinearProgressIndicator(
                        progress = { item.totalMillis.toFloat() / max },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlyReviewCard(checkIns: List<CheckInEntity>, today: LocalDate) {
    val current = YearMonth.from(today)
    var month by remember { mutableStateOf(current) }
    var picking by remember { mutableStateOf(false) }
    val stats = remember(checkIns, month) { buildMonthStats(checkIns, month) }
    val monthWeekdays = remember(checkIns, month) { productiveWeekdaysIn(checkIns, month) }
    val formatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()) }

    ShieldCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionDivider("Monthly Review")

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = month.format(formatter),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { picking = true }) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = "Choose month",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            MonthGrid(month = month, dayCounts = stats.dayCounts, today = today)

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatBox(
                    label = "Check-ins",
                    value = stats.totalCheckIns.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    label = "Active days",
                    value = stats.activeDays.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "Most productive weekday: ${formatWeekdays(monthWeekdays)}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }

    if (picking) {
        MonthPicker(
            months = monthsWithData(checkIns, current),
            formatter = formatter,
            onPick = {
                month = it
                picking = false
            },
            onDismiss = { picking = false }
        )
    }
}

@Composable
private fun StatBox(label: String, value: String, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MonthGrid(month: YearMonth, dayCounts: Map<Long, Int>, today: LocalDate) {
    val offset = month.atDay(1).dayOfWeek.value - 1
    val length = month.lengthOfMonth()
    val rows = (offset + length + 6) / 7
    val primary = MaterialTheme.colorScheme.primary
    val empty = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    val outline = MaterialTheme.colorScheme.outline
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val cellShape = RoundedCornerShape(8.dp)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DayOfWeek.entries.forEach { day ->
                Text(
                    text = day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        repeat(rows) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(7) { col ->
                    val dayNumber = row * 7 + col - offset + 1
                    if (dayNumber in 1..length) {
                        val date = month.atDay(dayNumber)
                        val count = dayCounts[date.toEpochDay()] ?: 0
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(cellShape)
                                .background(
                                    if (count > 0) {
                                        primary.copy(alpha = (0.3f + 0.2f * count).coerceAtMost(1f))
                                    } else {
                                        empty
                                    }
                                )
                                .then(
                                    if (date == today) {
                                        Modifier.border(2.dp, outline, cellShape)
                                    } else {
                                        Modifier
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayNumber.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (count >= 3) onPrimary else onSurface
                            )
                        }
                    } else {
                        Spacer(Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthPicker(
    months: List<YearMonth>,
    formatter: DateTimeFormatter,
    onPick: (YearMonth) -> Unit,
    onDismiss: () -> Unit
) {
    val listState = rememberLazyListState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose month") },
        text = {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .lazyScrollbar(listState)
            ) {
                items(months) { m ->
                    TextButton(
                        onClick = { onPick(m) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(m.format(formatter), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun HabitGlyph(habit: HabitEntity, modifier: Modifier) {
    val pkg = habit.packageName
    if (pkg != null) {
        AppIcon(packageName = pkg, modifier = modifier)
    } else {
        HabitIcon(habit.iconKey, modifier)
    }
}

private fun formatDuration(millis: Long): String {
    val minutes = millis / 60_000
    return if (minutes < 60) "$minutes min" else "${minutes / 60} h ${minutes % 60} min"
}