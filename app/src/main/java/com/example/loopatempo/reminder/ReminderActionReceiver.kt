package com.example.loopatempo.reminder

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.loopatempo.LoopATempoApplication
import com.example.loopatempo.data.AppDatabase
import com.example.loopatempo.data.CheckInEntity
import com.example.loopatempo.data.HabitEntity
import com.example.loopatempo.data.SettingsEntity
import com.example.loopatempo.notify.ReminderNotifier
import com.example.loopatempo.widget.HabitWidgetProvider
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ReminderActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val appContext = context.applicationContext
        val app = appContext as LoopATempoApplication
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val db = app.database
                val today = LocalDate.now().toEpochDay()
                val hour = intent.getIntExtra(EXTRA_HOUR, -1)
                when (intent.action) {
                    ACTION_MARK_DONE -> {
                        val id = intent.getLongExtra(EXTRA_HABIT_ID, -1L)
                        if (id >= 0) db.checkInDao().insert(CheckInEntity(habitId = id, day = today))
                    }
                    ACTION_MARK_ALL -> {
                        dueHabits(db, hour)
                            .filter { it.packageName == null }
                            .forEach { db.checkInDao().insert(CheckInEntity(habitId = it.id, day = today)) }
                    }
                }
                if (hour >= 0) {
                    ReminderNotifier.update(appContext, hour, dueHabits(db, hour))
                }
                HabitWidgetProvider.refreshAll(appContext)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_MARK_DONE = "com.example.loopatempo.action.MARK_DONE"
        const val ACTION_MARK_ALL = "com.example.loopatempo.action.MARK_ALL"
        const val EXTRA_HABIT_ID = "habit_id"
        const val EXTRA_HOUR = "hour"

        private val FLAGS = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        fun markDoneIntent(context: Context, habitId: Long, hour: Int): PendingIntent {
            val intent = Intent(context, ReminderActionReceiver::class.java)
                .setAction(ACTION_MARK_DONE)
                .putExtra(EXTRA_HABIT_ID, habitId)
                .putExtra(EXTRA_HOUR, hour)
            return PendingIntent.getBroadcast(context, "done-$habitId".hashCode(), intent, FLAGS)
        }

        fun markAllIntent(context: Context, hour: Int): PendingIntent {
            val intent = Intent(context, ReminderActionReceiver::class.java)
                .setAction(ACTION_MARK_ALL)
                .putExtra(EXTRA_HOUR, hour)
            return PendingIntent.getBroadcast(context, "all-$hour".hashCode(), intent, FLAGS)
        }

        suspend fun dueHabits(db: AppDatabase, hour: Int): List<HabitEntity> {
            val settings = db.settingsDao().observe().first() ?: SettingsEntity()
            val today = LocalDate.now().toEpochDay()
            val done = db.checkInDao().observeForDay(today).first().map { it.habitId }.toSet()
            return db.habitDao().getAll().filter {
                ReminderPlanner.effectiveMinutes(it, settings) / 60 == hour && it.id !in done
            }
        }
    }
}