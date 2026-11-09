package com.example.loopatempo.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.loopatempo.LoopATempoApplication
import com.example.loopatempo.data.SettingsEntity
import com.example.loopatempo.notify.ReminderNotifier
import com.example.loopatempo.notify.StreakNotifier
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val appContext = context.applicationContext
        val app = appContext as LoopATempoApplication
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val db = app.database
                val settings = db.settingsDao().observe().first() ?: SettingsEntity()
                val habits = db.habitDao().getAll()
                val hour = intent.getIntExtra(ReminderPlanner.EXTRA_HOUR, -1)

                if (settings.reminderEnabled && hour >= 0 && StreakNotifier.canPost(appContext)) {
                    val today = LocalDate.now().toEpochDay()
                    val done = db.checkInDao().observeForDay(today).first().map { it.habitId }.toSet()
                    val due = habits.filter {
                        ReminderPlanner.effectiveMinutes(it, settings) / 60 == hour && it.id !in done
                    }
                    if (due.isNotEmpty()) {
                        ReminderNotifier.notify(appContext, hour, due)
                    }
                }

                ReminderPlanner.reschedule(appContext, habits, settings)
            } finally {
                pending.finish()
            }
        }
    }
}