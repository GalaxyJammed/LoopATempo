package com.example.loopatempo.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

data class HabitStats(
    val habit: HabitEntity,
    val longestStreak: Int
)

data class AnalyticsSummary(
    val bestApp: HabitStats?,
    val bestCustom: HabitStats?,
    val totalCheckIns: Int,
    val activeDays: Int,
    val appCount: Int,
    val customCount: Int,
    val weekdayCounts: Map<DayOfWeek, Int>
)

data class MonthStats(
    val month: YearMonth,
    val dayCounts: Map<Long, Int>,
    val totalCheckIns: Int,
    val activeDays: Int,
    val busiestDay: LocalDate?,
    val busiestDayCount: Int,
    val busiestWeekday: DayOfWeek?
)

fun buildAnalytics(habits: List<HabitEntity>, checkIns: List<CheckInEntity>): AnalyticsSummary {
    val daysByHabit = checkIns.groupBy({ it.habitId }, { it.day }).mapValues { it.value.toSet() }
    val stats = habits.map { habit ->
        HabitStats(habit, longestRun(daysByHabit[habit.id].orEmpty()))
    }.sortedWith(
        compareByDescending<HabitStats> { it.longestStreak }.thenBy { it.habit.name.lowercase() }
    )
    val perWeekday = checkIns.groupingBy { LocalDate.ofEpochDay(it.day).dayOfWeek }.eachCount()

    return AnalyticsSummary(
        bestApp = stats.firstOrNull { it.habit.packageName != null && it.longestStreak > 0 },
        bestCustom = stats.firstOrNull { it.habit.packageName == null && it.longestStreak > 0 },
        totalCheckIns = checkIns.size,
        activeDays = checkIns.map { it.day }.distinct().size,
        appCount = habits.count { it.packageName != null },
        customCount = habits.count { it.packageName == null },
        weekdayCounts = DayOfWeek.entries.associateWith { perWeekday[it] ?: 0 }
    )
}

fun buildMonthStats(checkIns: List<CheckInEntity>, month: YearMonth): MonthStats {
    val start = month.atDay(1).toEpochDay()
    val end = month.atEndOfMonth().toEpochDay()
    val inMonth = checkIns.filter { it.day in start..end }
    val dayCounts = inMonth.groupingBy { it.day }.eachCount()
    val busiestEntry = dayCounts.maxByOrNull { it.value }
    val weekdayCounts = inMonth.groupingBy { LocalDate.ofEpochDay(it.day).dayOfWeek }.eachCount()

    return MonthStats(
        month = month,
        dayCounts = dayCounts,
        totalCheckIns = inMonth.size,
        activeDays = dayCounts.size,
        busiestDay = busiestEntry?.let { LocalDate.ofEpochDay(it.key) },
        busiestDayCount = busiestEntry?.value ?: 0,
        busiestWeekday = weekdayCounts.maxByOrNull { it.value }?.key
    )
}

fun monthsWithData(checkIns: List<CheckInEntity>, current: YearMonth): List<YearMonth> {
    val earliest = checkIns.minOfOrNull { it.day }
        ?.let { YearMonth.from(LocalDate.ofEpochDay(it)) }
        ?: current
    val months = generateSequence(earliest) { it.plusMonths(1) }
        .takeWhile { !it.isAfter(current) }
        .toList()
    return (if (months.isEmpty()) listOf(current) else months).reversed()
}

private fun longestRun(days: Set<Long>): Int {
    var best = 0
    var run = 0
    var previous: Long? = null
    for (day in days.sorted()) {
        run = if (previous != null && day == previous + 1) run + 1 else 1
        if (run > best) best = run
        previous = day
    }
    return best
}