package com.example.enigmafocus.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityEvent
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
        Log.i(TAG, "AccessibilityService connected and ready!")

        startWatchdog()
    }

    private fun startWatchdog() {
        watchdogJob?.cancel()
        watchdogJob = serviceScope.launch {
            while (isActive) {
                try {
                    val root = rootInActiveWindow
                    val pkg = root?.packageName?.toString()
                    if (pkg != null) {
                        checkAndBlockPackage(pkg)
                        checkSleepSchedule(pkg)
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
        checkAndBlockPackage(packageName)
        checkSleepSchedule(packageName)
    }

    private fun isSleepInterval(interval: FocusInterval): Boolean {
        val label = interval.label.lowercase()
        return label.contains("dormir") ||
               label.contains("descanso") ||
               label.contains("sueño") ||
               label.contains("noche") ||
               (interval.startHour >= 21 && interval.endHour <= 8)
    }

    private fun checkSleepSchedule(packageName: String) {
        val activeInterval = AppPreferences.getActiveScheduledInterval() ?: return
        if (!isSleepInterval(activeInterval)) return

        // If user is inside launcher or our app or systemui, do not force popup immediately
        if (packageName == this.packageName ||
            packageName == "android" ||
            packageName == "com.android.systemui" ||
            packageName.contains("launcher")
        ) {
            return
        }

        // If sleep nudge can be shown (e.g. initial or every 10 min after snooze)
        if (SleepOverlayManager.canShowNudge() && !BlockOverlayManager.isShowing()) {
            SleepOverlayManager.showSleepNudge(this)
        }
    }

    private fun checkAndBlockPackage(packageName: String) {
        // If current app is launcher or system UI or our own app, dismiss overlay if showing
        if (packageName == this.packageName ||
            packageName == "android" ||
            packageName == "com.android.systemui" ||
            packageName == "com.google.android.apps.nexuslauncher" ||
            packageName == "com.android.launcher3" ||
            packageName.contains("launcher")
        ) {
            if (BlockOverlayManager.isShowing() && packageName != this.packageName) {
                BlockOverlayManager.hideOverlay(this)
            }
            return
        }

        try {
            AppPreferences.init(applicationContext)
        } catch (e: Exception) {
            // Ignore
        }

        // Check if focus session is active, a scheduled interval is active, or always-block is enabled
        val isScheduledActive = AppPreferences.isAnyScheduledIntervalActive()
        val isFocusActive = AppPreferences.isFocusActive() || isScheduledActive
        val isAlwaysBlock = AppPreferences.isAlwaysBlockEnabled()

        if (!isFocusActive && !isAlwaysBlock) {
            if (BlockOverlayManager.isShowing()) {
                BlockOverlayManager.hideOverlay(this)
            }
            return
        }

        // Check if the opened app is in the blocked list
        val isBlocked = AppPreferences.isAppBlocked(packageName)
        if (!isBlocked) {
            if (BlockOverlayManager.isShowing()) {
                BlockOverlayManager.hideOverlay(this)
            }
            return
        }

        // Check if user has temporary whitelist grace period
        if (AppPreferences.isTemporarilyWhitelisted(packageName)) {
            if (BlockOverlayManager.isShowing()) {
                BlockOverlayManager.hideOverlay(this)
            }
            return
        }

        // If overlay is already showing for this package, return
        if (BlockOverlayManager.isShowing() && lastBlockedPkg == packageName) {
            return
        }

        val now = System.currentTimeMillis()
        if (packageName == lastBlockedPkg && (now - lastBlockedTime) < 300) {
            return
        }
        lastBlockedPkg = packageName
        lastBlockedTime = now

        Log.i(TAG, "🛑 Displaying breathing overlay for blocked app: $packageName")

        // 1. Trigger light haptic feedback
        vibratePhone()

        // 2. Display the breathing overlay on top of the window
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
