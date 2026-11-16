package com.example.loopatempo.data

import java.time.Instant
import java.time.ZoneId

data class RestSnapshot(
    val displayed: Int,
    val runs: Map<Long, Int>,
    val progress: Int,
    val target: Int
)

object RestDays {
    const val MAX_HELD = 3
    const val DAYS_PER_REST = 7
    const val MIN_HABITS = 3
    const val COMPLETION_PERCENT = 80

    fun requiredChecks(habitCount: Int): Int =
        (habitCount * COMPLETION_PERCENT + 99) / 100

    fun snapshot(habits: List<HabitEntity>, checkIns: List<CheckInEntity>, today: Long): RestSnapshot {
        val zone = ZoneId.systemDefault()
        val createdDay = habits.associate { h ->
            h.id to Instant.ofEpochMilli(h.createdAt).atZone(zone).toLocalDate().toEpochDay()
        }
        val checkedByDay = checkIns.groupBy({ it.day }, { it.habitId }).mapValues { it.value.toSet() }
        val runs = HashMap<Long, Int>()
        habits.forEach { runs[it.id] = 0 }

        var held = 0
        var progress = 0
        var day = createdDay.values.minOrNull() ?: today

        while (day <= today) {
            val active = habits.filter { createdDay.getValue(it.id) <= day }
            val checked = checkedByDay[day].orEmpty()
            val eligible = active.size >= MIN_HABITS &&
                    active.count { it.id in checked } >= requiredChecks(active.size)
            val streakAtRisk = active.any { it.id !in checked && runs.getValue(it.id) > 0 }
            val covered = streakAtRisk && held > 0
            if (covered) held--

            if (day < today) {
                for (h in habits) {
                    val id = h.id
                    runs[id] = when {
                        id in checked -> runs.getValue(id) + 1
                        covered -> runs.getValue(id)
                        else -> 0
                    }
                }
            }

            if (eligible && !covered) {
                progress++
                if (progress >= DAYS_PER_REST) {
                    progress = 0
                    held = (held + 1).coerceAtMost(MAX_HELD)
                }
            }
            day++
        }

        return RestSnapshot(
            displayed = held,
            runs = runs,
            progress = progress,
            target = DAYS_PER_REST
        )
    }
}