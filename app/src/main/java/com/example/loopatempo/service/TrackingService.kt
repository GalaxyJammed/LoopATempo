package com.example.loopatempo.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.loopatempo.LoopATempoApplication
import com.example.loopatempo.R
import com.example.loopatempo.data.AppStreakSync
import com.example.loopatempo.hasUsageAccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TrackingService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var loopStarted = false

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startInForeground()
        if (!loopStarted) {
            loopStarted = true
            scope.launch {
                val app = application as LoopATempoApplication
                while (true) {
                    if (!hasUsageAccess(this@TrackingService)) {
                        stopSelf()
                        break
                    }
                    AppStreakSync(this@TrackingService, app.database).run()
                    delay(CHECK_INTERVAL_MS)
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Streak tracking",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun startInForeground() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle("LoopATempo is tracking your streaks")
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    companion object {
        private const val CHANNEL_ID = "streak_tracking"
        private const val NOTIFICATION_ID = 1001
        private const val CHECK_INTERVAL_MS = 30_000L

        fun start(context: Context) {
            context.startForegroundService(Intent(context, TrackingService::class.java))
        }
    }
}