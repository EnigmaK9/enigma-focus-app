package com.example.enigmafocus.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.enigmafocus.data.AppPreferences
import com.example.enigmafocus.data.FocusInterval
import com.example.enigmafocus.ui.block.BlockOverlayManager
import com.example.enigmafocus.ui.block.SleepOverlayManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

class FocusAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "FocusAccessibility"
        private var lastBlockedTime = 0L
        private var lastBlockedPkg = ""

        fun clearDebounce() {
            lastBlockedPkg = ""
            lastBlockedTime = 0L
        }

        fun isAccessibilityServiceEnabled(context: Context): Boolean {
            val expectedComponentName = ComponentName(context, FocusAccessibilityService::class.java)
            val enabledServicesSetting = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServicesSetting)

            while (colonSplitter.hasNext()) {
                val componentNameString = colonSplitter.next()
                val enabledComponent = ComponentName.unflattenFromString(componentNameString)
                if (enabledComponent != null && (enabledComponent == expectedComponentName || componentNameString.contains(context.packageName))) {
                    return true
                }
            }
            return false
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var watchdogJob: Job? = null

    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    // Reset 1-minute snooze when screen is turned off so next unlock locks immediately
                    SleepOverlayManager.resetDismissedTime()
                }
                Intent.ACTION_SCREEN_ON -> {
                    val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? android.app.KeyguardManager
                    // If device is not locked with PIN/Pattern, show sleep nudge immediately
                    if (keyguardManager?.isKeyguardLocked == false) {
                        if (isSleepScheduleActive() && SleepOverlayManager.canShowNudge()) {
                            SleepOverlayManager.showSleepNudge(this@FocusAccessibilityService)
                        }
                    }
                }
                Intent.ACTION_USER_PRESENT -> {
                    // Device was just unlocked with PIN/Fingerprint/Pattern
                    if (isSleepScheduleActive() && SleepOverlayManager.canShowNudge()) {
                        SleepOverlayManager.showSleepNudge(this@FocusAccessibilityService)
                    }
                }
            }
        }
    }

    private val browserPackages = setOf(
        "com.android.chrome",
        "com.brave.browser",
        "com.microsoft.emmx",
        "org.mozilla.firefox",
        "com.opera.browser",
        "com.sec.android.app.sbrowser",
        "com.duckduckgo.mobile.android"
    )

    private val blockedWebDomains = listOf(
        "instagram.com",
        "reddit.com",
        "tiktok.com",
        "twitter.com",
        "x.com",
        "youtube.com",
        "facebook.com",
        "twitch.tv",
        "web.whatsapp.com",
        "whatsapp.com"
    )

    override fun onServiceConnected() {
        super.onServiceConnected()
        try {
            AppPreferences.init(applicationContext)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing preferences in service", e)
        }

        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                         AccessibilityEvent.TYPE_WINDOWS_CHANGED or
                         AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 20
        }
        this.serviceInfo = info

        try {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_USER_PRESENT)
            }
            registerReceiver(screenStateReceiver, filter)
        } catch (e: Exception) {
            Log.e(TAG, "Error registering screen state receiver", e)
        }

        Log.i(TAG, "AccessibilityService connected and ready!")

        startWatchdog()
    }

    private fun startWatchdog() {
        watchdogJob?.cancel()
        watchdogJob = serviceScope.launch {
            while (isActive) {
                try {
                    val root = rootInActiveWindow
                    val pkg = root?.packageName?.toString() ?: ""

                    if (isSleepScheduleActive()) {
                        checkSleepSchedule(pkg)
                    } else if (pkg.isNotEmpty()) {
                        checkAndBlockPackage(pkg)
                        if (root != null) {
                            checkBrowserUrls(root, pkg)
                        }
                    }
                } catch (e: Exception) {
                    // Ignore window retrieval errors
                }
                delay(300)
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val pkgCharSequence = event.packageName ?: return
        val packageName = pkgCharSequence.toString()

        if (isSleepScheduleActive()) {
            checkSleepSchedule(packageName)
        } else {
            checkAndBlockPackage(packageName)
            val root = rootInActiveWindow
            if (root != null) {
                checkBrowserUrls(root, packageName)
            }
        }
    }

    private fun checkBrowserUrls(root: AccessibilityNodeInfo, packageName: String) {
        if (!browserPackages.contains(packageName)) return

        val isScheduledActive = AppPreferences.isAnyScheduledIntervalActive()
        val isFocusActive = AppPreferences.isFocusActive() || isScheduledActive
        val isAlwaysBlock = AppPreferences.isAlwaysBlockEnabled()

        if (!isFocusActive && !isAlwaysBlock) return

        val detectedUrl = findUrlInNode(root)?.lowercase() ?: return
        for (domain in blockedWebDomains) {
            if (detectedUrl.contains(domain)) {
                if (AppPreferences.isTemporarilyWhitelisted(packageName)) return

                if (BlockOverlayManager.isShowing()) return

                val now = System.currentTimeMillis()
                if (domain == lastBlockedPkg && (now - lastBlockedTime) < 1000) return

                lastBlockedPkg = domain
                lastBlockedTime = now

                Log.i(TAG, "🛑 Intercepted blocked web domain in browser: $domain")
                vibratePhone()
                BlockOverlayManager.showOverlay(this, "Web ($domain)") {
                    clearDebounce()
                }
                break
            }
        }
    }

    private fun findUrlInNode(node: AccessibilityNodeInfo?, depth: Int = 0): String? {
        if (node == null || depth > 6) return null
        val text = node.text?.toString()
        if (text != null && (text.contains(".") || text.startsWith("http"))) {
            for (domain in blockedWebDomains) {
                if (text.contains(domain, ignoreCase = true)) {
                    return text
                }
            }
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            val found = findUrlInNode(child, depth + 1)
            if (found != null) return found
        }
        return null
    }

    private fun isSleepInterval(interval: FocusInterval): Boolean {
        val label = interval.label.lowercase()
        val isOvernightHours = interval.startHour > interval.endHour && (interval.startHour >= 20 || interval.endHour <= 9)
        return label.contains("dormir") ||
               label.contains("descanso") ||
               label.contains("sueño") ||
               label.contains("noche") ||
               label.contains("sleep") ||
               label.contains("rest") ||
               isOvernightHours
    }

    private fun isSleepScheduleActive(): Boolean {
        val active = AppPreferences.getActiveScheduledInterval()
        if (active != null && isSleepInterval(active)) return true

        for (interval in AppPreferences.getIntervals()) {
            if (interval.isEnabled && isSleepInterval(interval) && interval.isCurrentlyActive()) {
                return true
            }
        }

        // Fallback: 22:30 to 06:30 overnight check
        val cal = Calendar.getInstance()
        val currentMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val isOvernight = currentMinutes >= (22 * 60 + 30) || currentMinutes < (6 * 60 + 30)
        return isOvernight
    }

    private fun isEmergencyPackage(packageName: String): Boolean {
        return packageName == this.packageName ||
               packageName == "com.android.phone" ||
               packageName == "com.google.android.dialer" ||
               packageName == "com.android.dialer" ||
               packageName == "com.samsung.android.dialer" ||
               packageName == "com.android.server.telecom" ||
               packageName == "com.android.incallui" ||
               packageName.contains("dialer") ||
               packageName.contains("telecom") ||
               packageName.contains("incallui") ||
               packageName.contains("deskclock") ||
               packageName.contains("clockpackage")
    }

    private fun checkSleepSchedule(packageName: String) {
        if (!isSleepScheduleActive()) return

        if (isEmergencyPackage(packageName) ||
            packageName == "android" ||
            packageName == "com.android.systemui" ||
            packageName.contains("systemui")
        ) {
            return
        }

        if (SleepOverlayManager.canShowNudge() && !BlockOverlayManager.isShowing()) {
            vibratePhone()
            SleepOverlayManager.showSleepNudge(this)
        }
    }

    private fun isLauncherPackage(packageName: String): Boolean {
        return packageName.contains("launcher") ||
               packageName == "com.miui.home" ||
               packageName == "com.sec.android.app.launcher" ||
               packageName == "com.huawei.android.launcher" ||
               packageName == "com.oppo.launcher" ||
               packageName == "com.vivo.launcher"
    }

    private fun checkAndBlockPackage(packageName: String) {
        if (packageName == this.packageName ||
            packageName == "android" ||
            packageName == "com.android.systemui" ||
            packageName.contains("systemui") ||
            packageName.contains("inputmethod")
        ) {
            return
        }

        if (isLauncherPackage(packageName)) {
            if (BlockOverlayManager.isShowing()) {
                BlockOverlayManager.hideOverlay(this)
            }
            return
        }

        try {
            AppPreferences.init(applicationContext)
        } catch (e: Exception) {
            // Ignore
        }

        val isScheduledActive = AppPreferences.isAnyScheduledIntervalActive()
        val isFocusActive = AppPreferences.isFocusActive() || isScheduledActive
        val isAlwaysBlock = AppPreferences.isAlwaysBlockEnabled()

        if (!isFocusActive && !isAlwaysBlock) {
            if (BlockOverlayManager.isShowing()) {
                BlockOverlayManager.hideOverlay(this)
            }
            return
        }

        val isBlocked = AppPreferences.isAppBlocked(packageName)
        if (!isBlocked) {
            if (BlockOverlayManager.isShowing() && BlockOverlayManager.getCurrentPackage() != packageName) {
                BlockOverlayManager.hideOverlay(this)
            }
            return
        }

        if (AppPreferences.isTemporarilyWhitelisted(packageName)) {
            if (BlockOverlayManager.isShowing()) {
                BlockOverlayManager.hideOverlay(this)
            }
            return
        }

        if (BlockOverlayManager.isShowing()) {
            return
        }

        val now = System.currentTimeMillis()
        if (packageName == lastBlockedPkg && (now - lastBlockedTime) < 1000) {
            return
        }
        lastBlockedPkg = packageName
        lastBlockedTime = now

        Log.i(TAG, "🛑 Displaying breathing overlay for blocked app: $packageName")

        vibratePhone()

        BlockOverlayManager.showOverlay(this, packageName) {
            clearDebounce()
        }
    }

    private fun vibratePhone() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(100)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error vibrating", e)
        }
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(screenStateReceiver)
        } catch (e: Exception) {
            // Ignore
        }
        watchdogJob?.cancel()
        BlockOverlayManager.hideOverlay(this)
        SleepOverlayManager.dismiss(this)
        super.onDestroy()
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility Service interrupted")
        BlockOverlayManager.hideOverlay(this)
        SleepOverlayManager.dismiss(this)
    }
}
