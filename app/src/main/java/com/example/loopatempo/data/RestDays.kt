package com.example.loopatempo.data

import java.time.Instant
import java.time.ZoneId

data class RestSnapshot(
    val displayed: Int,
    val runs: Map<Long, Int>
)

object RestDays {
    const val EARN_EVERY = 5
    const val MAX_HELD = 3

    fun snapshot(habits: List<HabitEntity>, checkIns: List<CheckInEntity>, today: Long): RestSnapshot {
        val zone = ZoneId.systemDefault()
        val createdDay = habits.associate { h ->
            h.id to Instant.ofEpochMilli(h.createdAt).atZone(zone).toLocalDate().toEpochDay()
        }
        val checkedByDay = checkIns.groupBy({ it.day }, { it.habitId }).mapValues { it.value.toSet() }
        val runs = HashMap<Long, Int>()
        habits.forEach { runs[it.id] = 0 }

        var held = 0
        var day = createdDay.values.minOrNull() ?: today
        while (day < today) {
            val checked = checkedByDay[day].orEmpty()
            val missed = habits.filter { createdDay.getValue(it.id) <= day && it.id !in checked }
            val streakAtRisk = missed.any { runs.getValue(it.id) > 0 }
            val covered = streakAtRisk && held > 0
            if (covered) held--

            for (h in habits) {
                val id = h.id
                runs[id] = when {
                    id in checked -> runs.getValue(id) + 1
                    covered -> runs.getValue(id)
                    else -> 0
                }
            }
            held = (held + checked.size / EARN_EVERY).coerceAtMost(MAX_HELD)
            day++
        }

        val todayChecked = checkedByDay[today].orEmpty().size
        return RestSnapshot(
            displayed = (held + todayChecked / EARN_EVERY).coerceAtMost(MAX_HELD),
            runs = runs
        )
    }
}