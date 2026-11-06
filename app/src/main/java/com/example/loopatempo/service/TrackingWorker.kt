package com.example.loopatempo.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.loopatempo.LoopATempoApplication
import com.example.loopatempo.data.AppStreakSync

class TrackingWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as LoopATempoApplication
        AppStreakSync(applicationContext, app.database).run()
        return Result.success()
    }
}