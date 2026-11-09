package com.example.loopatempo.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

fun productiveWeekdays(checkIns: List<CheckInEntity>): List<DayOfWeek>? =
    rankWeekdays(checkIns)

fun productiveWeekdaysIn(checkIns: List<CheckInEntity>, month: YearMonth): List<DayOfWeek>? {
    val start = month.atDay(1).toEpochDay()
    val end = month.atEndOfMonth().toEpochDay()
    return rankWeekdays(checkIns.filter { it.day in start..end })
}

private fun rankWeekdays(checkIns: List<CheckInEntity>): List<DayOfWeek>? {
    if (checkIns.isEmpty()) return null
    val distinctDaysPerWeekday = checkIns
        .map { it.day }
        .distinct()
        .groupingBy { LocalDate.ofEpochDay(it).dayOfWeek }
        .eachCount()
    if (distinctDaysPerWeekday.values.none { it >= 2 }) return null

    val checksPerWeekday = checkIns.groupingBy { LocalDate.ofEpochDay(it.day).dayOfWeek }.eachCount()
    val top = checksPerWeekday.values.maxOrNull() ?: return null
    return checksPerWeekday.filterValues { it == top }.keys.sortedBy { it.value }
}

fun formatWeekdays(days: List<DayOfWeek>?): String =
    if (days == null) {
        "Not enough data"
    } else {
        days.joinToString("/") { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) }
    }