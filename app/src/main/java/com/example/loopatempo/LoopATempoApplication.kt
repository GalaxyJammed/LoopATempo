package com.example.loopatempo

import android.app.Application
import com.example.loopatempo.data.AppDatabase
import com.example.loopatempo.data.HabitRepository
import com.example.loopatempo.data.buildDatabase
import com.example.loopatempo.notify.StreakNotifier

class LoopATempoApplication : Application() {
    val database: AppDatabase by lazy { buildDatabase(this) }
    val repository: HabitRepository by lazy { HabitRepository(database) }

    override fun onCreate() {
        super.onCreate()
        StreakNotifier.createChannel(this)
    }
}