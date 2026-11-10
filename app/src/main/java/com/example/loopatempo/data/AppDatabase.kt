package com.example.loopatempo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [HabitEntity::class, CheckInEntity::class, SettingsEntity::class, PrefEntity::class],
    version = 7,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun checkInDao(): CheckInDao
    abstract fun settingsDao(): SettingsDao
    abstract fun prefDao(): PrefDao
}

fun buildDatabase(context: Context): AppDatabase =
    Room.databaseBuilder(context, AppDatabase::class.java, "loopatempo.db")
        .addMigrations(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
            MIGRATION_6_7
        )
        .build()