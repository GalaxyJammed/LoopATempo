package com.example.loopatempo.update

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment

object ApkDownloader {
    const val PENDING_KEY = "update.downloadId"

    fun start(context: Context, url: String, fileName: String): Long {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("LoopATempo update")
            .setDescription(fileName)
            .setMimeType("application/vnd.android.package-archive")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        return context.getSystemService(DownloadManager::class.java).enqueue(request)
    }
}