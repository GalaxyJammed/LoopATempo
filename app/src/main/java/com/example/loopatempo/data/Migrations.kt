package com.example.loopatempo.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        if (!db.hasColumn("habits", "iconKey")) {
            db.execSQL("ALTER TABLE habits ADD COLUMN iconKey TEXT")
        }
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        if (!db.hasColumn("settings", "notificationsEnabled")) {
            db.execSQL("ALTER TABLE settings ADD COLUMN notificationsEnabled INTEGER NOT NULL DEFAULT 0")
        }
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        if (!db.hasColumn("settings", "keepAlive")) {
            db.execSQL("ALTER TABLE settings ADD COLUMN keepAlive INTEGER NOT NULL DEFAULT 0")
        }
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS prefs (prefKey TEXT NOT NULL, prefValue TEXT NOT NULL, PRIMARY KEY(prefKey))"
        )
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        if (!db.hasColumn("habits", "reminderMinutes")) {
            db.execSQL("ALTER TABLE habits ADD COLUMN reminderMinutes INTEGER")
        }
        if (!db.hasColumn("settings", "reminderEnabled")) {
            db.execSQL("ALTER TABLE settings ADD COLUMN reminderEnabled INTEGER NOT NULL DEFAULT 1")
        }
        if (!db.hasColumn("settings", "reminderMinutes")) {
            db.execSQL("ALTER TABLE settings ADD COLUMN reminderMinutes INTEGER NOT NULL DEFAULT 1200")
        }
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        if (!db.hasColumn("habits", "website")) {
            db.execSQL("ALTER TABLE habits ADD COLUMN website TEXT")
        }
        if (!db.hasColumn("settings", "websiteAutoCheck")) {
            db.execSQL("ALTER TABLE settings ADD COLUMN websiteAutoCheck INTEGER NOT NULL DEFAULT 0")
        }
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        if (!db.hasColumn("settings", "widgetHideCompleted")) {
            db.execSQL("ALTER TABLE settings ADD COLUMN widgetHideCompleted INTEGER NOT NULL DEFAULT 0")
        }
        if (!db.hasColumn("settings", "widgetStrikeCompleted")) {
            db.execSQL("ALTER TABLE settings ADD COLUMN widgetStrikeCompleted INTEGER NOT NULL DEFAULT 0")
        }
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS usage_minutes (packageName TEXT NOT NULL, day INTEGER NOT NULL, millis INTEGER NOT NULL, PRIMARY KEY(packageName, day))"
        )
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS settings_new (
                id INTEGER NOT NULL,
                defaultThresholdMinutes INTEGER NOT NULL,
                darkTheme INTEGER NOT NULL,
                notificationsEnabled INTEGER NOT NULL DEFAULT 0,
                keepAlive INTEGER NOT NULL DEFAULT 0,
                reminderEnabled INTEGER NOT NULL DEFAULT 1,
                reminderMinutes INTEGER NOT NULL DEFAULT 1200,
                websiteAutoCheck INTEGER NOT NULL DEFAULT 0,
                widgetHideCompleted INTEGER NOT NULL DEFAULT 0,
                widgetStrikeCompleted INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(id)
            )"""
        )
        db.execSQL(
            "INSERT INTO settings_new (id, defaultThresholdMinutes, darkTheme, notificationsEnabled, keepAlive, reminderEnabled, reminderMinutes, websiteAutoCheck, widgetHideCompleted, widgetStrikeCompleted) " +
                    "SELECT id, defaultThresholdMinutes, darkTheme, notificationsEnabled, keepAlive, reminderEnabled, reminderMinutes, websiteAutoCheck, widgetHideCompleted, widgetStrikeCompleted FROM settings"
        )
        db.execSQL("DROP TABLE settings")
        db.execSQL("ALTER TABLE settings_new RENAME TO settings")
    }
}

private fun SupportSQLiteDatabase.hasColumn(table: String, column: String): Boolean =
    query("PRAGMA table_info($table)").use { cursor ->
        val nameIndex = cursor.getColumnIndexOrThrow("name")
        var found = false
        while (cursor.moveToNext()) {
            if (cursor.getString(nameIndex) == column) {
                found = true
                break
            }
        }
        found
    }