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
import com.example.enigmafocus.core.interceptor.FocusInterceptor
import com.example.enigmafocus.core.scheduler.ScheduleEvaluator
import com.example.enigmafocus.data.AppPreferences
import com.example.enigmafocus.data.FocusEventLogger
import com.example.enigmafocus.ui.block.BlockActivity
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

        @Volatile
        var isConnected: Boolean = false
            private set

        @Volatile
        var lastHeartbeatTime: Long = 0L
            private set

        fun recordHeartbeat() {
            lastHeartbeatTime = System.currentTimeMillis()
        }

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

        fun restartAccessibilityService(context: Context) {
            try {
                val myComponent = ComponentName(context, FocusAccessibilityService::class.java).flattenToString()
                val currentSetting = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                ) ?: ""

                val services = currentSetting.split(":")
                    .filter { it.isNotBlank() && it != myComponent }
                    .toMutableList()

                // Step 1: Remove our service component
                Settings.Secure.putString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                    services.joinToString(":")
                )

                // Step 2: Re-add our service component to trigger framework rebind
                services.add(myComponent)
                Settings.Secure.putString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                    services.joinToString(":")
                )
                Log.i(TAG, "🔄 Cycled ENABLED_ACCESSIBILITY_SERVICES to rebind service")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to cycle accessibility service setting", e)
            }
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var watchdogJob: Job? = null

    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                when (intent?.action) {
                    Intent.ACTION_SCREEN_OFF -> {
                        SleepOverlayManager.resetDismissedTime()
                    }
                    Intent.ACTION_SCREEN_ON -> {
                        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? android.app.KeyguardManager
                        if (keyguardManager?.isKeyguardLocked == false) {
                            if (ScheduleEvaluator.isSleepScheduleActive() && SleepOverlayManager.canShowNudge()) {
                                SleepOverlayManager.showSleepNudge(this@FocusAccessibilityService)
                            }
                        }
                    }
                    Intent.ACTION_USER_PRESENT -> {
                        if (ScheduleEvaluator.isSleepScheduleActive() && SleepOverlayManager.canShowNudge()) {
                            SleepOverlayManager.showSleepNudge(this@FocusAccessibilityService)
                        }
                    }
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Error in screenStateReceiver", e)
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isConnected = true
        recordHeartbeat()
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

        Log.i(TAG, "⚡ AccessibilityService connected and ready!")
        startWatchdog()
    }

    private fun startWatchdog() {
        watchdogJob?.cancel()
        watchdogJob = serviceScope.launch {
            var blockedPackageCounter = 0
            while (isActive) {
                recordHeartbeat()
                try {
                    val root = rootInActiveWindow
                    val pkg = root?.packageName?.toString() ?: ""

                    if (ScheduleEvaluator.isSleepScheduleActive()) {
                        checkSleepSchedule(pkg)
                    } else if (pkg.isNotEmpty()) {
                        checkAndBlockPackage(pkg)
                        if (root != null) {
                            checkBrowserUrls(root, pkg)
                        }

                        // Failsafe 3: If a blocked package persists in the active window
                        // without either the overlay showing or BlockActivity active, kick to HOME screen
                        if (FocusInterceptor.shouldBlockPackage(packageName, pkg) &&
                            !BlockOverlayManager.isShowing() &&
                            !BlockActivity.isBlockScreenShowing
                        ) {
                            blockedPackageCounter++
                            if (blockedPackageCounter >= 3) { // ~900ms of bypassed blocked app
                                Log.w(TAG, "🚨 Failsafe triggered: Blocked app $pkg detected without block screen. Sending to HOME!")
                                performGlobalAction(GLOBAL_ACTION_HOME)
                                blockedPackageCounter = 0
                            }
                        } else {
                            blockedPackageCounter = 0
                        }
                    }
                } catch (t: Throwable) {
                    // Prevent any exception from killing the watchdog coroutine
                    Log.e(TAG, "Error in watchdog iteration", t)
                }
                delay(300)
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        recordHeartbeat()

        try {
            val pkgCharSequence = event.packageName ?: return
            val packageName = pkgCharSequence.toString()

            if (ScheduleEvaluator.isSleepScheduleActive()) {
                checkSleepSchedule(packageName)
            } else {
                checkAndBlockPackage(packageName)
                val root = rootInActiveWindow
                if (root != null) {
                    checkBrowserUrls(root, packageName)
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Unhandled exception in onAccessibilityEvent", t)
        }
    }

    private fun checkBrowserUrls(root: AccessibilityNodeInfo, packageName: String) {
        if (!FocusInterceptor.BROWSER_PACKAGES.contains(packageName)) return

        val isFocusActive = ScheduleEvaluator.isFocusActive() || AppPreferences.isAlwaysBlockEnabled()
        if (!isFocusActive) return

        val detectedDomain = FocusInterceptor.findBlockedDomainInNode(root) ?: return

        if (AppPreferences.isTemporarilyWhitelisted(packageName)) return
        if (BlockOverlayManager.isShowing()) return

        val now = System.currentTimeMillis()
        if (detectedDomain == lastBlockedPkg && (now - lastBlockedTime) < 1000) return

        lastBlockedPkg = detectedDomain
        lastBlockedTime = now

        Log.i(TAG, "🛑 Intercepted blocked web domain in browser: $detectedDomain")
        vibratePhone()
        FocusEventLogger.logEvent(this, "url_blocked", mapOf("domain" to detectedDomain, "browser" to packageName))
        BlockOverlayManager.showOverlay(this, "Web ($detectedDomain)") {
            clearDebounce()
        }
    }

    private fun checkSleepSchedule(packageName: String) {
        if (!ScheduleEvaluator.isSleepScheduleActive()) return

        if (FocusInterceptor.isEmergencyPackage(this.packageName, packageName) ||
            packageName == "android" ||
            packageName == "com.android.systemui" ||
            packageName.contains("systemui")
        ) {
            return
        }

        if (SleepOverlayManager.canShowNudge() && !BlockOverlayManager.isShowing()) {
            vibratePhone()
            FocusEventLogger.logEvent(this, "bedtime_lockout", mapOf("trigger_package" to packageName))
            SleepOverlayManager.showSleepNudge(this)
        }
    }

    private fun checkAndBlockPackage(packageName: String) {
        if (FocusInterceptor.isLauncherPackage(packageName)) {
            if (BlockOverlayManager.isShowing()) {
                BlockOverlayManager.hideOverlay(this)
            }
            return
        }

        if (!FocusInterceptor.shouldBlockPackage(this.packageName, packageName)) {
            // Only dismiss overlay if the user actually navigated to an unblocked user app
            // (Exclude our own app, system UI, Android system, and keyboard input method)
            if (BlockOverlayManager.isShowing() &&
                packageName != this.packageName &&
                packageName != "android" &&
                !packageName.contains("systemui") &&
                !packageName.contains("inputmethod")
            ) {
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
        FocusEventLogger.logEvent(this, "app_blocked", mapOf("package" to packageName))

        // Failsafe 1: Launch full-screen BlockActivity immediately to guarantee occlusion
        try {
            val blockIntent = Intent(this, BlockActivity::class.java).apply {
                putExtra(BlockActivity.EXTRA_BLOCKED_PACKAGE, packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(blockIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting BlockActivity failsafe", e)
        }

        // Failsafe 2: Also attach TYPE_ACCESSIBILITY_OVERLAY for instant reactive barrier
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
        isConnected = false
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
