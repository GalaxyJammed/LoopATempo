package com.example.loopatempo.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.loopatempo.LoopATempoApplication
import com.example.loopatempo.service.BackgroundTracking
import com.example.loopatempo.service.TrackingService
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
                val keepAlive = app.database.settingsDao().observe().first()?.keepAlive ?: false
                if (keepAlive) {
                    runCatching { TrackingService.start(appContext) }
                        .onFailure { BackgroundTracking.apply(appContext, false) }
                } else {
                    BackgroundTracking.apply(appContext, false)
                }
            } finally {
                pending.finish()
            }
        }
    }
}