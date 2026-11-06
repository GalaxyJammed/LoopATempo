package com.example.loopatempo.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap

data class LaunchableApp(
    val label: String,
    val packageName: String,
    val icon: ImageBitmap
)

fun loadLaunchableApps(context: Context): List<LaunchableApp> {
    val pm = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return pm.queryIntentActivities(intent, 0)
        .mapNotNull { info ->
            runCatching {
                LaunchableApp(
                    label = info.loadLabel(pm).toString(),
                    packageName = info.activityInfo.packageName,
                    icon = info.loadIcon(pm).toBitmap(96, 96).asImageBitmap()
                )
            }.getOrNull()
        }
        .filter { it.packageName != context.packageName }
        .distinctBy { it.packageName }
        .sortedBy { it.label.lowercase() }
}

fun loadAppIconBitmap(context: Context, packageName: String): ImageBitmap? =
    try {
        context.packageManager.getApplicationIcon(packageName).toBitmap(96, 96).asImageBitmap()
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }