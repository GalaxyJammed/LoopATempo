package com.example.loopatempo.data

import java.time.LocalDate

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val progress: Int,
    val target: Int
) {
    val unlocked: Boolean get() = progress >= target
    val fraction: Float get() = (progress.toFloat() / target).coerceIn(0f, 1f)
}

fun buildAchievements(habits: List<HabitEntity>, checkIns: List<CheckInEntity>): List<Achievement> {
    val daysByHabit = checkIns.groupBy({ it.habitId }, { it.day }).mapValues { it.value.toSet() }
    val bestStreak = habits.maxOfOrNull { achievementRun(daysByHabit[it.id].orEmpty()) } ?: 0
    val totalCheckIns = checkIns.size
    val habitCount = habits.size
    val appCount = habits.count { it.packageName != null }
    val websiteCount = habits.count { it.website != null }
    val perDay = checkIns.groupingBy { it.day }.eachCount()
    val bestDay = perDay.values.maxOrNull() ?: 0
    val weekdaysUsed = checkIns.map { LocalDate.ofEpochDay(it.day).dayOfWeek }.toSet().size

    return listOf(
        Achievement("first", "First step", "Complete your first check-in", minOf(totalCheckIns, 1), 1),
        Achievement("streak7", "One week strong", "Reach a 7-day streak on any habit", bestStreak, 7),
        Achievement("streak14", "Two weeks", "Reach a 14-day streak on any habit", bestStreak, 14),
        Achievement("streak30", "Monthly habit", "Reach a 30-day streak on any habit", bestStreak, 30),
        Achievement("streak100", "Centurion", "Reach a 100-day streak on any habit", bestStreak, 100),
        Achievement("streak180", "Half year", "Reach a 180-day streak on any habit", bestStreak, 180),
        Achievement("total50", "Getting going", "Make 50 check-ins in total", totalCheckIns, 50),
        Achievement("total200", "Dedicated", "Make 200 check-ins in total", totalCheckIns, 200),
        Achievement("collector", "Collector", "Add 5 habits or apps", habitCount, 5),
        Achievement("busyBee", "Busy bee", "Have 10 habits at once", habitCount, 10),
        Achievement("connected", "Connected", "Add 3 app habits", appCount, 3),
        Achievement("webWanderer", "Web wanderer", "Add a custom task with a website", websiteCount, 1),
        Achievement("productive", "Productive day", "Complete 5 habits on one day", bestDay, 5),
        Achievement(
            "perfect",
            "Perfect day",
            "Complete every habit on one day",
            bestDay,
            habitCount.coerceAtLeast(1)
        ),
        Achievement("wellRounded", "Well-rounded", "Check in on all 7 weekdays", weekdaysUsed, 7)
    )
}

private fun achievementRun(days: Set<Long>): Int {
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