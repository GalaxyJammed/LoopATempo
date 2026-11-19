package com.example.loopatempo.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.loopatempo.LoopATempoApplication
import com.example.loopatempo.data.SettingsEntity
import com.example.loopatempo.reminder.ReminderPlanner
import com.example.loopatempo.service.BackgroundTracking
import com.example.loopatempo.service.TrackingService
import com.example.loopatempo.service.TrackingStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        val appContext = context.applicationContext
        val app = appContext as LoopATempoApplication
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val settings = app.database.settingsDao().observe().first() ?: SettingsEntity()
                val result = if (settings.keepAlive) {
                    runCatching { TrackingService.start(appContext) }.fold(
                        onSuccess = { "service started" },
                        onFailure = {
                            BackgroundTracking.apply(appContext, false)
                            "service blocked, background checks used instead"
                        }
                    )
                } else {
                    BackgroundTracking.apply(appContext, false)
                    "Keep tracking alive is off, background checks used"
                }
                app.repository.savePref(
                    TrackingStatus.BOOT_KEY,
                    "${System.currentTimeMillis()}|$result"
                )
                ReminderPlanner.reschedule(appContext, app.database.habitDao().getAll(), settings)
            } finally {
                pending.finish()
            }
        }
    }
}