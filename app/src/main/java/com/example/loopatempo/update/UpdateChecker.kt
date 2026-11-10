package com.example.loopatempo.update

import android.content.Context
import com.example.loopatempo.data.ReleaseInfo
import com.example.loopatempo.data.ReleaseNotes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UpdateChecker {
    fun installedVersion(context: Context): String =
        context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()

    suspend fun latestNewer(context: Context): ReleaseInfo? = withContext(Dispatchers.IO) {
        try {
            val latest = ReleaseNotes.fetch().firstOrNull()
            if (latest != null && isNewer(latest.tag, installedVersion(context))) latest else null
        } catch (e: Exception) {
            null
        }
    }

    fun isNewer(latestTag: String, current: String): Boolean {
        val a = parts(latestTag)
        val b = parts(current)
        for (i in 0 until maxOf(a.size, b.size)) {
            val x = a.getOrElse(i) { 0 }
            val y = b.getOrElse(i) { 0 }
            if (x != y) return x > y
        }
        return false
    }

    private fun parts(version: String): List<Int> =
        version.trim().trimStart('v', 'V').split('.').map { it.takeWhile(Char::isDigit).toIntOrNull() ?: 0 }
}