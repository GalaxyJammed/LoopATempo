package com.example.loopatempo.notify

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.loopatempo.R
import com.example.loopatempo.data.HabitEntity

object ReminderNotifier {
    private const val CHANNEL_ID = "habit_reminders"
    private const val BASE_ID = 3000

    @SuppressLint("MissingPermission")
    fun notify(context: Context, hour: Int, due: List<HabitEntity>) {
        createChannel(context)
        val text = if (due.size == 1) {
            "You haven't completed your \"${due.first().name}\" task!"
        } else {
            "You haven't completed a bunch of tasks!"
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle("Habit reminder")
            .setContentText(text)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(BASE_ID + hour, notification)
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Habit reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}