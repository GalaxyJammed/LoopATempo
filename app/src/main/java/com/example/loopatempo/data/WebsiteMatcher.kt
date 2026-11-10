package com.example.loopatempo.data

import java.time.LocalDate
import kotlinx.coroutines.flow.first

object WebsiteMatcher {

    fun normalizeHost(raw: String): String? {
        val host = raw.trim()
            .lowercase()
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .substringBefore('/')
            .substringBefore('?')
            .substringBefore('#')
        val valid = host.contains('.') &&
                host.none { it.isWhitespace() } &&
                !host.startsWith('.') &&
                !host.endsWith('.')
        return host.takeIf { valid }
    }

    suspend fun onVisit(db: AppDatabase, visitedHost: String): List<HabitEntity> {
        val today = LocalDate.now().toEpochDay()
        val checkedIds = db.checkInDao().observeForDay(today).first().map { it.habitId }.toSet()
        val completed = db.habitDao().getAll()
            .filter { it.packageName == null && it.id !in checkedIds }
            .filter { habit ->
                val listed = habit.website?.let { normalizeHost(it) }
                listed != null && (visitedHost == listed || visitedHost.endsWith(".$listed"))
            }
        completed.forEach { habit ->
            db.checkInDao().insert(CheckInEntity(habitId = habit.id, day = today))
        }
        return completed
    }
}