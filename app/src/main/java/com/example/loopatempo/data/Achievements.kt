package com.example.loopatempo.data

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
    val bestDay = checkIns.groupingBy { it.day }.eachCount().values.maxOrNull() ?: 0

    return listOf(
        Achievement("first", "First step", "Complete your first check-in", minOf(totalCheckIns, 1), 1),
        Achievement("streak7", "One week strong", "Reach a 7-day streak on any habit", bestStreak, 7),
        Achievement("streak30", "Monthly habit", "Reach a 30-day streak on any habit", bestStreak, 30),
        Achievement("streak100", "Centurion", "Reach a 100-day streak on any habit", bestStreak, 100),
        Achievement("total50", "Getting going", "Make 50 check-ins in total", totalCheckIns, 50),
        Achievement("total200", "Dedicated", "Make 200 check-ins in total", totalCheckIns, 200),
        Achievement("collector", "Collector", "Add 5 habits or apps", habitCount, 5),
        Achievement(
            "perfect",
            "Perfect day",
            "Complete every habit on one day",
            bestDay,
            habitCount.coerceAtLeast(1)
        )
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