package com.example.loopatempo.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.loopatempo.data.HabitEntity
import com.example.loopatempo.data.SettingsEntity
import java.time.ZonedDateTime

object ReminderPlanner {
    const val EXTRA_HOUR = "reminder_hour"
    private const val REQUEST_CODE = 4101

    data class Plan(val triggerAtMillis: Long, val hour: Int)

    fun effectiveMinutes(habit: HabitEntity, settings: SettingsEntity): Int =
        habit.reminderMinutes ?: settings.reminderMinutes

    fun nextPlan(habits: List<HabitEntity>, settings: SettingsEntity, now: ZonedDateTime): Plan? {
        if (!settings.reminderEnabled || habits.isEmpty()) return null
        return habits
            .map { effectiveMinutes(it, settings) }
            .groupBy { it / 60 }
            .map { (hour, minutesOfDay) ->
                val earliest = minutesOfDay.min()
                var at = now.toLocalDate()
                    .atTime(earliest / 60, earliest % 60)
                    .atZone(now.zone)
                if (!at.isAfter(now)) at = at.plusDays(1)
                Plan(at.toInstant().toEpochMilli(), hour)
            }
            .minByOrNull { it.triggerAtMillis }
    }

    fun reschedule(context: Context, habits: List<HabitEntity>, settings: SettingsEntity) {
        val alarms = context.getSystemService(AlarmManager::class.java)
        alarms.cancel(buildIntent(context, 0))
        val plan = nextPlan(habits, settings, ZonedDateTime.now()) ?: return
        alarms.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            plan.triggerAtMillis,
            buildIntent(context, plan.hour)
        )
    }

    private fun buildIntent(context: Context, hour: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).putExtra(EXTRA_HOUR, hour)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}