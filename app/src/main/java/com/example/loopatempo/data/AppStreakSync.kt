package com.example.loopatempo.data

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import com.example.loopatempo.hasUsageAccess
import com.example.loopatempo.notify.StreakNotifier
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.first

class AppStreakSync(private val context: Context, private val db: AppDatabase) {

    suspend fun run() {
        if (!hasUsageAccess(context)) return

        val today = LocalDate.now()
        val todayEpoch = today.toEpochDay()
        val start = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val now = System.currentTimeMillis()

        val usedMillis = millisByPackage(start, now)
        if (usedMillis.isEmpty()) return

        val settings = db.settingsDao().observe().first() ?: SettingsEntity()
        val appHabits = db.habitDao().observeHabits().first().filter { it.packageName != null }
        val checkedIds = db.checkInDao().observeForDay(todayEpoch).first().map { it.habitId }.toSet()

        for (habit in appHabits) {
            val pkg = habit.packageName ?: continue
            val used = usedMillis[pkg] ?: continue
            val thresholdMillis = (habit.thresholdMinutes ?: settings.defaultThresholdMinutes) * 60_000L
            if (habit.id in checkedIds || used < thresholdMillis) continue

            db.checkInDao().insert(CheckInEntity(habitId = habit.id, day = todayEpoch))
            if (settings.notificationsEnabled) {
                StreakNotifier.notifyCompleted(context, habit.name)
            }
        }
    }

    private fun millisByPackage(start: Long, end: Long): Map<String, Long> {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val events = usm.queryEvents(start, end)
        val event = UsageEvents.Event()
        val openSince = mutableMapOf<String, Long>()
        val totals = mutableMapOf<String, Long>()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName ?: continue
            if (pkg == context.packageName) continue
            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> openSince[pkg] = event.timeStamp
                UsageEvents.Event.ACTIVITY_PAUSED -> {
                    val from = openSince.remove(pkg) ?: continue
                    totals[pkg] = (totals[pkg] ?: 0L) + (event.timeStamp - from)
                }
            }
        }

        openSince.forEach { (pkg, from) ->
            totals[pkg] = (totals[pkg] ?: 0L) + (end - from)
        }
        return totals
    }
}