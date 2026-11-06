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