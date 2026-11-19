package com.example.loopatempo.share

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import com.example.loopatempo.data.HabitEntity
import java.io.File

private const val APP_URL = "https://github.com/galaxyjammed/LoopATempo"
private const val CARD_W = 1080
private const val CARD_H = 1350

fun shareStreakCard(context: Context, habit: HabitEntity, streak: Int) {
    val unit = if (streak == 1) "day" else "days"
    val bitmap = renderCard(
        icon = iconFor(context, habit),
        title = habit.name.take(28),
        headline = "$streak $unit",
        lines = listOf("Streak in a row", "Keep yours going with LoopATempo")
    )
    send(context, bitmap, "I've kept a $streak-day streak on \"${habit.name}\" with LoopATempo!")
}

fun shareAnalyticsCard(context: Context, headline: String, lines: List<String>) {
    val bitmap = renderCard(
        icon = null,
        title = "My LoopATempo stats",
        headline = headline,
        lines = lines.map { it.take(60) }
    )
    send(context, bitmap, "Here are my habit stats from LoopATempo!")
}

private fun iconFor(context: Context, habit: HabitEntity): Bitmap? {
    val pkg = habit.packageName
    if (pkg != null) {
        return runCatching { context.packageManager.getApplicationIcon(pkg).toBitmap(256, 256) }.getOrNull()
    }
    val key = habit.iconKey ?: return null
    return if (key.startsWith("file:")) BitmapFactory.decodeFile(key.removePrefix("file:")) else null
}

private fun renderCard(icon: Bitmap?, title: String, headline: String, lines: List<String>): Bitmap {
    val bitmap = Bitmap.createBitmap(CARD_W, CARD_H, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.rgb(0x1C, 0x1B, 0x1F))

    val panel = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(0x2B, 0x29, 0x30) }
    canvas.drawRoundRect(RectF(60f, 60f, CARD_W - 60f, CARD_H - 60f), 56f, 56f, panel)

    icon?.let {
        val size = 220
        val scaled = Bitmap.createScaledBitmap(it, size, size, true)
        canvas.drawBitmap(scaled, (CARD_W - size) / 2f, 150f, null)
    }

    val main = Color.rgb(0xE6, 0xE1, 0xE5)
    val accent = Color.rgb(0x64, 0xB5, 0xF6)
    val muted = Color.rgb(0xCA, 0xC4, 0xD0)
    val faint = Color.rgb(0x93, 0x8F, 0x99)

    drawCentered(canvas, title, 500f, 56f, main, bold = true)
    drawCentered(canvas, headline, 650f, 130f, accent, bold = true)
    lines.forEachIndexed { i, line -> drawCentered(canvas, line, 750f + i * 70f, 42f, muted) }
    drawCentered(canvas, "Tracked with LoopATempo", 1180f, 42f, accent, bold = true)
    drawCentered(canvas, APP_URL.removePrefix("https://"), 1240f, 34f, faint)
    return bitmap
}

private fun drawCentered(
    canvas: Canvas,
    text: String,
    y: Float,
    size: Float,
    color: Int,
    bold: Boolean = false
) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        textSize = size
        textAlign = Paint.Align.CENTER
        typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
    }
    canvas.drawText(text, CARD_W / 2f, y, paint)
}

private fun send(context: Context, bitmap: Bitmap, message: String) {
    val dir = File(context.cacheDir, "shared").apply { mkdirs() }
    val file = File(dir, "loopatempo-${System.currentTimeMillis()}.png")
    file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, "$message $APP_URL")
        clipData = ClipData.newRawUri("", uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(
        Intent.createChooser(intent, "Share your progress").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}