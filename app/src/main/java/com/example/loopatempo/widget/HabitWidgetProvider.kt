package com.example.loopatempo.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.example.loopatempo.LoopATempoApplication
import com.example.loopatempo.R
import com.example.loopatempo.data.CheckInEntity
import com.example.loopatempo.data.HabitEntity
import com.example.loopatempo.data.SettingsEntity
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val TAG = "HabitWidget"

class HabitWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Log.i(TAG, "onUpdate for ${appWidgetIds.toList()}")
        refreshAll(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action != ACTION_TOGGLE) return
        val habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1L)
        if (habitId < 0) return

        val app = context.applicationContext as LoopATempoApplication
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val today = LocalDate.now().toEpochDay()
                val checked = app.database.checkInDao()
                    .observeForDay(today).first().any { it.habitId == habitId }
                if (checked) {
                    app.database.checkInDao().delete(habitId, today)
                } else {
                    app.database.checkInDao().insert(CheckInEntity(habitId = habitId, day = today))
                }
                refreshAll(context)
            } catch (e: Exception) {
                Log.e(TAG, "Toggle failed", e)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_TOGGLE = "com.example.loopatempo.widget.TOGGLE"
        const val EXTRA_HABIT_ID = "habit_id"
        const val GROUP_KEY = "widget.groupCompleted"
        private const val MAX_ROWS = 15
        private val FLAGS = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        fun refreshAll(context: Context) {
            val app = context.applicationContext as LoopATempoApplication
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                try {
                    val manager = AppWidgetManager.getInstance(context)
                    val ids = manager.getAppWidgetIds(ComponentName(context, HabitWidgetProvider::class.java))
                    if (ids.isEmpty()) return@launch

                    val today = LocalDate.now().toEpochDay()
                    val habits = app.database.habitDao().getAll().sortedBy { it.createdAt }
                    val done = app.database.checkInDao().observeForDay(today).first().map { it.habitId }.toSet()
                    val settings = app.database.settingsDao().observe().first() ?: SettingsEntity()
                    val grouped = app.database.prefDao().getAll()
                        .firstOrNull { it.prefKey == GROUP_KEY }?.prefValue == "ON"
                    val views = buildViews(context, habits, done, settings, grouped)
                    ids.forEach { id ->
                        try {
                            manager.updateAppWidget(id, views)
                        } catch (e: Exception) {
                            Log.e(TAG, "updateAppWidget failed for $id", e)
                        }
                    }
                    Log.i(TAG, "Updated ${ids.size} widget(s) with ${habits.size} habit(s)")
                } catch (e: Exception) {
                    Log.e(TAG, "Widget refresh failed", e)
                }
            }
        }

        private fun buildViews(
            context: Context,
            habits: List<HabitEntity>,
            done: Set<Long>,
            settings: SettingsEntity,
            grouped: Boolean
        ): RemoteViews =
            try {
                buildTaskViews(context, habits, done, settings, grouped)
            } catch (e: Exception) {
                Log.e(TAG, "Building widget views failed", e)
                RemoteViews(context.packageName, R.layout.widget_habit).apply {
                    setTextViewText(R.id.widget_title, "Couldn't load tasks")
                    setTextViewText(R.id.widget_more, "Open LoopATempo to refresh")
                    setViewVisibility(R.id.widget_more, View.VISIBLE)
                }
            }

        private fun buildTaskViews(
            context: Context,
            habits: List<HabitEntity>,
            done: Set<Long>,
            settings: SettingsEntity,
            grouped: Boolean
        ): RemoteViews {
            val visible = habits.filter { !(settings.widgetHideCompleted && it.id in done) }
            val ordered = if (grouped) visible.sortedBy { if (it.id in done) 1 else 0 } else visible

            val views = RemoteViews(context.packageName, R.layout.widget_habit)
            views.setTextViewText(R.id.widget_title, if (habits.isEmpty()) "No habits yet" else "Today")
            views.removeAllViews(R.id.habit_container)

            var shown = 0
            var lastDone: Boolean? = null
            for (habit in ordered) {
                if (shown >= MAX_ROWS) break
                val isDone = habit.id in done
                if (grouped && isDone != lastDone) {
                    views.addView(
                        R.id.habit_container,
                        sectionHeader(context, if (isDone) "Completed" else "Not completed")
                    )
                    lastDone = isDone
                }
                views.addView(R.id.habit_container, buildRow(context, habit, isDone, settings.widgetStrikeCompleted))
                shown++
            }

            val hidden = habits.size - visible.size
            val overflow = ordered.size - shown
            val notes = listOfNotNull(
                if (hidden > 0) "$hidden hidden" else null,
                if (overflow > 0) "+$overflow more in the app" else null
            )
            views.setTextViewText(R.id.widget_more, notes.joinToString("  ·  "))
            views.setViewVisibility(R.id.widget_more, if (notes.isEmpty()) View.GONE else View.VISIBLE)
            return views
        }

        private fun sectionHeader(context: Context, label: String): RemoteViews =
            RemoteViews(context.packageName, R.layout.widget_section_header).apply {
                setTextViewText(R.id.section_label, label)
            }

        private fun buildRow(context: Context, habit: HabitEntity, isDone: Boolean, strike: Boolean): RemoteViews {
            val row = RemoteViews(context.packageName, R.layout.widget_habit_row)
            row.setTextViewText(R.id.row_box, if (isDone) "☑" else "☐")
            row.setTextViewText(R.id.row_name, nameText(habit.name, isDone && strike))

            val pkg = habit.packageName
            if (pkg != null) {
                context.packageManager.getLaunchIntentForPackage(pkg)?.let { launch ->
                    launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    row.setOnClickPendingIntent(
                        R.id.row_root,
                        PendingIntent.getActivity(context, "open-$pkg".hashCode(), launch, FLAGS)
                    )
                }
            } else {
                val toggle = Intent(context, HabitWidgetProvider::class.java)
                    .setAction(ACTION_TOGGLE)
                    .putExtra(EXTRA_HABIT_ID, habit.id)
                row.setOnClickPendingIntent(
                    R.id.row_root,
                    PendingIntent.getBroadcast(context, "toggle-${habit.id}".hashCode(), toggle, FLAGS)
                )
            }
            return row
        }

        private fun nameText(name: String, strike: Boolean): CharSequence {
            if (!strike) return name
            return SpannableString(name).apply {
                setSpan(StrikethroughSpan(), 0, name.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }
}