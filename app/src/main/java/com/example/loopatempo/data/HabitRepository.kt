package com.example.loopatempo.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

data class HomeItem(
    val habit: HabitEntity,
    val checkedToday: Boolean,
    val streak: Int
)

class HabitRepository(private val db: AppDatabase) {

    fun observeHabits(): Flow<List<HabitEntity>> = db.habitDao().observeHabits()

    fun observeSettings(): Flow<SettingsEntity?> = db.settingsDao().observe()

    fun observeCheckIns(): Flow<List<CheckInEntity>> = db.checkInDao().observeAll()

    fun observePref(key: String): Flow<String?> = db.prefDao().observe(key)

    suspend fun savePref(key: String, value: String) {
        db.prefDao().save(PrefEntity(prefKey = key, prefValue = value))
    }

    fun observeHomeItems(today: Long): Flow<List<HomeItem>> =
        combine(db.habitDao().observeHabits(), db.checkInDao().observeAll()) { habits, checkIns ->
            val daysByHabit = checkIns
                .groupBy({ it.habitId }, { it.day })
                .mapValues { it.value.toSet() }
            habits.map { habit ->
                val days = daysByHabit[habit.id].orEmpty()
                HomeItem(
                    habit = habit,
                    checkedToday = today in days,
                    streak = currentStreak(days, today)
                )
            }
        }

    suspend fun addHabit(name: String, packageName: String?, iconKey: String?, reminderMinutes: Int? = null) {
        db.habitDao().insert(
            HabitEntity(
                name = name,
                packageName = packageName,
                thresholdMinutes = null,
                createdAt = System.currentTimeMillis(),
                iconKey = iconKey,
                reminderMinutes = reminderMinutes
            )
        )
    }

    suspend fun updateHabit(habit: HabitEntity) {
        db.habitDao().update(habit)
    }

    suspend fun deleteHabit(habit: HabitEntity) {
        db.habitDao().delete(habit)
    }

    suspend fun setChecked(habitId: Long, day: Long, checked: Boolean) {
        if (checked) {
            db.checkInDao().insert(CheckInEntity(habitId = habitId, day = day))
        } else {
            db.checkInDao().delete(habitId, day)
        }
    }

    suspend fun ensureSettings() {
        if (db.settingsDao().observe().first() == null) {
            db.settingsDao().save(SettingsEntity())
        }
    }

    suspend fun updateSettings(change: (SettingsEntity) -> SettingsEntity) {
        val current = db.settingsDao().observe().first() ?: SettingsEntity()
        db.settingsDao().save(change(current))
    }

    private fun currentStreak(days: Set<Long>, today: Long): Int {
        var day = if (today in days) today else today - 1
        var count = 0
        while (day in days) {
            count++
            day--
        }
        return count
    }
}