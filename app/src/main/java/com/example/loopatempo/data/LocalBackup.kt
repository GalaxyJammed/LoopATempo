package com.example.loopatempo.data

import android.content.Context
import android.util.Base64
import androidx.room.withTransaction
import java.io.File
import java.util.UUID
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

object LocalBackup {
    private const val APP_NAME = "LoopATempo"
    private const val FORMAT_VERSION = 1

    fun isLoopATempoBackup(json: String): Boolean =
        runCatching { JSONObject(json).optString("app") == APP_NAME }.getOrDefault(false)

    suspend fun exportJson(db: AppDatabase): String {
        val root = JSONObject()
            .put("app", APP_NAME)
            .put("formatVersion", FORMAT_VERSION)
            .put("exportedAt", System.currentTimeMillis())

        db.settingsDao().observe().first()?.let { s ->
            root.put(
                "settings",
                JSONObject()
                    .put("nickname", s.nickname)
                    .put("defaultThresholdMinutes", s.defaultThresholdMinutes)
                    .put("darkTheme", s.darkTheme)
                    .put("notificationsEnabled", s.notificationsEnabled)
                    .put("keepAlive", s.keepAlive)
            )
        }

        val habits = JSONArray()
        db.habitDao().getAll().forEach { h ->
            habits.put(
                JSONObject()
                    .put("id", h.id)
                    .put("name", h.name)
                    .put("packageName", h.packageName ?: JSONObject.NULL)
                    .put("thresholdMinutes", h.thresholdMinutes ?: JSONObject.NULL)
                    .put("createdAt", h.createdAt)
                    .put("iconKey", h.iconKey ?: JSONObject.NULL)
                    .put("iconImage", iconAsBase64(h.iconKey) ?: JSONObject.NULL)
            )
        }
        root.put("habits", habits)

        val checkIns = JSONArray()
        db.checkInDao().getAll().forEach { c ->
            checkIns.put(JSONObject().put("habitId", c.habitId).put("day", c.day))
        }
        root.put("checkIns", checkIns)

        val prefs = JSONArray()
        db.prefDao().getAll().forEach { p ->
            prefs.put(JSONObject().put("key", p.prefKey).put("value", p.prefValue))
        }
        root.put("prefs", prefs)

        return root.toString()
    }

    suspend fun restoreJson(context: Context, db: AppDatabase, json: String) {
        val root = JSONObject(json)
        require(root.optString("app") == APP_NAME) { "This file is not a LoopATempo backup" }
        val iconsDir = File(context.filesDir, "icons").apply { mkdirs() }

        val habitArray = root.getJSONArray("habits")
        val habits = (0 until habitArray.length()).map { i ->
            val h = habitArray.getJSONObject(i)
            val iconKey = if (!h.isNull("iconImage")) {
                val file = File(iconsDir, "${UUID.randomUUID()}.png")
                file.writeBytes(Base64.decode(h.getString("iconImage"), Base64.NO_WRAP))
                "file:${file.absolutePath}"
            } else {
                h.optNullableString("iconKey")
            }
            HabitEntity(
                id = h.getLong("id"),
                name = h.getString("name"),
                packageName = h.optNullableString("packageName"),
                thresholdMinutes = if (h.isNull("thresholdMinutes")) null else h.getInt("thresholdMinutes"),
                createdAt = h.getLong("createdAt"),
                iconKey = iconKey
            )
        }

        val checkInArray = root.getJSONArray("checkIns")
        val checkIns = (0 until checkInArray.length()).map { i ->
            val c = checkInArray.getJSONObject(i)
            CheckInEntity(habitId = c.getLong("habitId"), day = c.getLong("day"))
        }

        val prefArray = root.getJSONArray("prefs")
        val prefs = (0 until prefArray.length()).map { i ->
            val p = prefArray.getJSONObject(i)
            PrefEntity(prefKey = p.getString("key"), prefValue = p.getString("value"))
        }

        val s = root.optJSONObject("settings")
        val settings = SettingsEntity(
            id = 0,
            nickname = s?.optString("nickname", "") ?: "",
            defaultThresholdMinutes = s?.optInt("defaultThresholdMinutes", 5) ?: 5,
            darkTheme = s?.optBoolean("darkTheme", true) ?: true,
            notificationsEnabled = s?.optBoolean("notificationsEnabled", false) ?: false,
            keepAlive = s?.optBoolean("keepAlive", true) ?: true
        )

        db.withTransaction {
            db.checkInDao().deleteAll()
            db.habitDao().deleteAll()
            db.settingsDao().deleteAll()
            db.prefDao().deleteAll()
            db.habitDao().insertAll(habits)
            db.checkInDao().insertAll(checkIns)
            db.settingsDao().save(settings)
            db.prefDao().saveAll(prefs)
        }
    }

    private fun iconAsBase64(iconKey: String?): String? {
        if (iconKey == null || !iconKey.startsWith("file:")) return null
        val file = File(iconKey.removePrefix("file:"))
        if (!file.exists()) return null
        return Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
    }

    private fun JSONObject.optNullableString(key: String): String? =
        if (isNull(key)) null else getString(key)
}