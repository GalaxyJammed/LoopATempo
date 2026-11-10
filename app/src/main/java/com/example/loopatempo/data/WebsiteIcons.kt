package com.example.loopatempo.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.scale
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

object WebsiteIcons {
    private const val MAX_BYTES = 256 * 1024

    private sealed interface Download {
        data class Ok(val bitmap: Bitmap) : Download
        data object Missing : Download
        data object Failed : Download
    }

    private fun dir(context: Context): File =
        File(context.filesDir, "favicons").apply { mkdirs() }

    fun cached(context: Context, host: String): Bitmap? {
        val file = File(dir(context), "$host.png")
        return if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
    }

    fun load(context: Context, host: String): Bitmap? {
        cached(context, host)?.let { return it }
        val folder = dir(context)
        val missingMarker = File(folder, "$host.none")
        if (missingMarker.exists()) return null

        return when (val result = download(host)) {
            is Download.Ok -> {
                val scaled = result.bitmap.scale(96, 96)
                File(folder, "$host.png").outputStream().use {
                    scaled.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
                scaled
            }
            Download.Missing -> {
                missingMarker.createNewFile()
                null
            }
            Download.Failed -> null
        }
    }

    private fun download(host: String): Download = try {
        val conn = URL("https://$host/favicon.ico").openConnection() as HttpURLConnection
        conn.connectTimeout = 5_000
        conn.readTimeout = 5_000
        conn.instanceFollowRedirects = true
        conn.setRequestProperty("User-Agent", "LoopATempo-App")
        try {
            if (conn.responseCode !in 200..299) {
                Download.Missing
            } else {
                val bytes = conn.inputStream.use { readLimited(it, MAX_BYTES) }
                val bitmap = if (bytes != null) BitmapFactory.decodeByteArray(bytes, 0, bytes.size) else null
                if (bitmap != null) Download.Ok(bitmap) else Download.Missing
            }
        } finally {
            conn.disconnect()
        }
    } catch (e: Exception) {
        Download.Failed
    }

    private fun readLimited(stream: InputStream, max: Int): ByteArray? {
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (true) {
            val read = stream.read(buffer)
            if (read == -1) break
            out.write(buffer, 0, read)
            if (out.size() > max) return null
        }
        return if (out.size() == 0) null else out.toByteArray()
    }
}