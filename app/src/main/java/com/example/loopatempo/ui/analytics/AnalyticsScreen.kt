package com.example.loopatempo.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.loopatempo.data.CheckInEntity
import com.example.loopatempo.data.HabitEntity
import com.example.loopatempo.data.HabitStats
import com.example.loopatempo.data.buildAnalytics
import com.example.loopatempo.data.buildMonthStats
import com.example.loopatempo.data.monthsWithData
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
import com.example.loopatempo.data.formatWeekdays
import com.example.loopatempo.data.productiveWeekdays
import com.example.loopatempo.data.productiveWeekdaysIn

@Composable
fun AnalyticsScreen(
    habits: List<HabitEntity>,
    checkIns: List<CheckInEntity>,
    contentPadding: PaddingValues
) {
    val today = LocalDate.now()
    val summary = remember(habits, checkIns) { buildAnalytics(habits, checkIns) }
    val allTimeWeekdays = remember(checkIns) { productiveWeekdays(checkIns) }
    val scroll = rememberScrollState()

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

        StatCard(title = "Best streak on app") {
            BestStreakRow(summary.bestApp, "No app streaks yet")
        }

        StatCard(title = "Best streak on custom task") {
            BestStreakRow(summary.bestCustom, "No custom task streaks yet")
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

        StatCard(title = "Summary") {
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
private fun StatCard(title: String, content: @Composable () -> Unit) {
    ShieldCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionDivider(title)
            content()
        }
    }
}

@Composable
private fun BestStreakRow(best: HabitStats?, emptyText: String) {
    if (best == null) {
        Text(emptyText, style = MaterialTheme.typography.bodyLarge)
        return
    }
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