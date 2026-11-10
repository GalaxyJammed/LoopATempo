package com.example.loopatempo.service

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.loopatempo.LoopATempoApplication
import com.example.loopatempo.data.WebsiteMatcher
import com.example.loopatempo.notify.StreakNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val TAG = "WebsiteWatch"

private val SUPPORTED_BROWSERS = setOf(
    "com.android.chrome",
    "com.microsoft.emmx",
    "com.brave.browser",
    "org.mozilla.firefox",
    "com.sec.android.app.sbrowser",
    "com.opera.browser"
)

private val ADDRESS_BAR_HINTS = listOf("location_bar", "url_bar", "url_field", "url_view", "omnibox")

fun isWebsiteServiceEnabled(context: Context): Boolean {
    val enabled = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    val component = ComponentName(context, WebsiteWatchService::class.java).flattenToString()
    return enabled.split(':').any { it.equals(component, ignoreCase = true) }
}

class WebsiteWatchService : AccessibilityService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var lastHost: String? = null
    private var lastVisitAt = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            handleEvent(event)
        } catch (e: Throwable) {
            Log.e(TAG, "Error while handling event", e)
        }
    }

    private fun handleEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        if (pkg !in SUPPORTED_BROWSERS) return
        val root = rootInActiveWindow ?: return
        val raw = findAddressBarText(root, pkg) ?: return
        val host = WebsiteMatcher.normalizeHost(raw) ?: return

        val now = System.currentTimeMillis()
        if (host == lastHost && now - lastVisitAt < 10_000L) return
        lastHost = host
        lastVisitAt = now
        Log.i(TAG, "Visit detected in $pkg: $host")

        val app = applicationContext as LoopATempoApplication
        scope.launch {
            try {
                val settings = app.database.settingsDao().observe().first() ?: return@launch
                if (!settings.websiteAutoCheck) return@launch
                val completed = WebsiteMatcher.onVisit(app.database, host)
                Log.i(TAG, "Checked ${completed.size} website task(s) for $host")
                if (settings.notificationsEnabled) {
                    completed.forEach { StreakNotifier.notifyCompleted(applicationContext, it.name) }
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Error while checking tasks", e)
            }
        }
    }

    private fun findAddressBarText(root: AccessibilityNodeInfo, pkg: String): String? =
        findByViewId(root, pkg) ?: findEditTextWithDomain(root)

    private fun findByViewId(node: AccessibilityNodeInfo?, pkg: String): String? {
        if (node == null) return null
        val id = node.viewIdResourceName.orEmpty()
        if (id.startsWith("$pkg:id/") && ADDRESS_BAR_HINTS.any { id.contains(it) }) {
            val text = node.text?.toString()?.takeIf { it.isNotBlank() }
                ?: node.contentDescription?.toString()?.takeIf { it.isNotBlank() }
            if (text != null) return text
        }
        for (i in 0 until node.childCount) {
            findByViewId(node.getChild(i), pkg)?.let { return it }
        }
        return null
    }

    private fun findEditTextWithDomain(node: AccessibilityNodeInfo?): String? {
        if (node == null) return null
        val text = node.text?.toString()
        val isEditText = node.className?.contains("EditText") == true
        if (isEditText && !text.isNullOrBlank() && text.contains('.') && !text.contains(' ')) {
            return text
        }
        for (i in 0 until node.childCount) {
            findEditTextWithDomain(node.getChild(i))?.let { return it }
        }
        return null
    }

    override fun onInterrupt() {
        Log.w(TAG, "Service interrupted")
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}