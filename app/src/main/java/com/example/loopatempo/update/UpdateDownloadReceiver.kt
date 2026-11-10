package com.example.loopatempo.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.loopatempo.LoopATempoApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class UpdateDownloadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        if (id < 0) return
        val pending = goAsync()
        val app = context.applicationContext as LoopATempoApplication
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val expected = app.repository.readPref(ApkDownloader.PENDING_KEY)?.toLongOrNull()
                if (expected == id) {
                    app.repository.savePref(ApkDownloader.PENDING_KEY, "")
                    val uri = context.getSystemService(DownloadManager::class.java).getUriForDownloadedFile(id)
                    if (uri != null) {
                        val install = Intent(Intent.ACTION_VIEW)
                            .setDataAndType(uri, "application/vnd.android.package-archive")
                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(install)
                    }
                }
            } catch (e: Exception) {
                // Nothing to do: the file stays in Downloads.
            } finally {
                pending.finish()
            }
        }
    }
}