package com.example.loopatempo.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.graphics.scale
import java.io.File
import java.util.UUID

fun saveIconFromUri(context: Context, uri: Uri): String {
    val original = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        ?: error("Cannot read image")
    val max = 192f
    val ratio = minOf(max / original.width, max / original.height, 1f)
    val scaled = original.scale(
        (original.width * ratio).toInt().coerceAtLeast(1),
        (original.height * ratio).toInt().coerceAtLeast(1)
    )
    val dir = File(context.filesDir, "icons").apply { mkdirs() }
    val file = File(dir, "${UUID.randomUUID()}.png")
    file.outputStream().use { scaled.compress(Bitmap.CompressFormat.PNG, 100, it) }
    return "file:${file.absolutePath}"
}