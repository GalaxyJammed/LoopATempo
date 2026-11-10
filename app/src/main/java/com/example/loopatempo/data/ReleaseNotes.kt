package com.example.loopatempo.data

import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray
import org.json.JSONObject

data class ReleaseInfo(
    val name: String,
    val tag: String,
    val publishedAt: String,
    val body: String,
    val url: String,
    val prerelease: Boolean,
    val apkUrl: String? = null
)

object ReleaseNotes {
    private const val API_URL = "https://api.github.com/repos/galaxyjammed/LoopATempo/releases"

    fun fetch(): List<ReleaseInfo> {
        val conn = URL(API_URL).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 10_000
        conn.readTimeout = 15_000
        conn.setRequestProperty("Accept", "application/vnd.github+json")
        conn.setRequestProperty("User-Agent", "LoopATempo-App")
        try {
            val code = conn.responseCode
            if (code !in 200..299) throw IllegalStateException("GitHub returned $code")
            val text = conn.inputStream.bufferedReader().use { it.readText() }
            val array = JSONArray(text)
            return (0 until array.length())
                .map { array.getJSONObject(it) }
                .filter { !it.optBoolean("draft", false) }
                .map { r ->
                    val tag = r.str("tag_name")
                    ReleaseInfo(
                        name = r.str("name").ifBlank { tag },
                        tag = tag,
                        publishedAt = r.str("published_at"),
                        body = r.str("body"),
                        url = r.str("html_url"),
                        prerelease = r.optBoolean("prerelease", false),
                        apkUrl = apkUrlOf(r)
                    )
                }
        } finally {
            conn.disconnect()
        }
    }

    private fun apkUrlOf(release: JSONObject): String? {
        val assets = release.optJSONArray("assets") ?: return null
        return (0 until assets.length())
            .map { assets.getJSONObject(it) }
            .firstOrNull { it.optString("name").endsWith(".apk", ignoreCase = true) }
            ?.optString("browser_download_url")
            ?.takeIf { it.isNotBlank() }
    }

    fun clean(markdown: String): String =
        markdown.lines().joinToString("\n") { line ->
            line.trim()
                .trimStart('#')
                .trim()
                .replace(Regex("^[*-] "), "• ")
                .replace("**", "")
                .replace("__", "")
                .replace(Regex("`([^`]*)`"), "$1")
                .replace(Regex("\\[([^\\]]+)\\]\\([^)]+\\)"), "$1")
        }.trim()

    private fun JSONObject.str(key: String): String =
        if (isNull(key)) "" else getString(key)
}