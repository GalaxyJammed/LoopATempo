package com.example.loopatempo.service

import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object BackgroundTracking {
    private const val WORK_NAME = "streak_periodic_check"

    fun apply(context: Context, keepAlive: Boolean) {
        val workManager = WorkManager.getInstance(context)
        if (keepAlive) {
            workManager.cancelUniqueWork(WORK_NAME)
            TrackingService.start(context)
        } else {
            context.stopService(Intent(context, TrackingService::class.java))
            val request = PeriodicWorkRequestBuilder<TrackingWorker>(15, TimeUnit.MINUTES).build()
            workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
        }
    }
}