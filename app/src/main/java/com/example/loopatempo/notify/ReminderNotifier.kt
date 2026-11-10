package com.example.loopatempo.notify

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.loopatempo.R
import com.example.loopatempo.data.HabitEntity
import com.example.loopatempo.reminder.ReminderActionReceiver

object ReminderNotifier {
    private const val CHANNEL_ID = "habit_reminders"
    private const val BASE_ID = 3000

    fun update(context: Context, hour: Int, due: List<HabitEntity>) {
        if (due.isEmpty()) {
            NotificationManagerCompat.from(context).cancel(BASE_ID + hour)
        } else {
            notify(context, hour, due)
        }
    }

    @SuppressLint("MissingPermission")
    fun notify(context: Context, hour: Int, due: List<HabitEntity>) {
        if (!StreakNotifier.canPost(context)) return
        createChannel(context)

        val single = due.singleOrNull()
        val text = if (single != null) {
            "You haven't completed your \"${single.name}\" task!"
        } else {
            "You haven't completed a bunch of tasks!"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle("Habit reminder")
            .setContentText(text)
            .setAutoCancel(true)

        if (single != null) {
            val pkg = single.packageName
            if (pkg != null) {
                openAppIntent(context, pkg)?.let { builder.addAction(0, "Open app", it) }
            } else {
                builder.addAction(
                    0,
                    "Mark as done",
                    ReminderActionReceiver.markDoneIntent(context, single.id, hour)
                )
            }
        } else if (due.any { it.packageName == null }) {
            builder.addAction(
                0,
                "Mark all custom tasks as done",
                ReminderActionReceiver.markAllIntent(context, hour)
            )
        }

        NotificationManagerCompat.from(context).notify(BASE_ID + hour, builder.build())
    }

    private fun openAppIntent(context: Context, pkg: String): PendingIntent? {
        val launch = context.packageManager.getLaunchIntentForPackage(pkg) ?: return null
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return PendingIntent.getActivity(
            context,
            "open-$pkg".hashCode(),
            launch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
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