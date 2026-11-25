package com.example.enigmafocus.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.enigmafocus.FocusApp
import com.example.enigmafocus.MainActivity
import com.example.enigmafocus.R
import com.example.enigmafocus.core.interceptor.FocusInterceptor
import com.example.enigmafocus.core.scheduler.ScheduleEvaluator
import com.example.enigmafocus.data.AppPreferences
import com.example.enigmafocus.manager.GrayscaleManager
import com.example.enigmafocus.manager.UsageStatsHelper
import com.example.enigmafocus.ui.block.BlockActivity
import com.example.enigmafocus.ui.block.BlockOverlayManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit

class FocusForegroundService : Service() {

    companion object {
        private const val TAG = "FocusForegroundService"
        const val ACTION_START = "ACTION_START_FOCUS"
        const val ACTION_START_SENTINEL = "ACTION_START_SENTINEL"
        const val ACTION_STOP = "ACTION_STOP_FOCUS"
        const val EXTRA_DURATION_MINUTES = "extra_duration_minutes"
        const val EXTRA_END_TIMESTAMP = "extra_end_timestamp"
        private const val NOTIFICATION_ID = 1001
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var timerJob: Job? = null
    private var lastRestartAttemptTime = 0L
    private var isSentinelOnlyMode = false
    private var loggedMissingSecureSettingsForRecovery = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            AppPreferences.init(applicationContext)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing preferences in foreground service", e)
        }

        val action = intent?.action

        if (action == ACTION_STOP) {
            stopFocusService()
            return if (AppPreferences.isAlwaysBlockEnabled() || AppPreferences.isHourlyLimitEnabled()) START_STICKY else START_NOT_STICKY
        }

        if (action == ACTION_START) {
            isSentinelOnlyMode = false
            val endTimestamp = intent.getLongExtra(EXTRA_END_TIMESTAMP, 0L)
            val durationMinutes = intent.getIntExtra(EXTRA_DURATION_MINUTES, 25)
            startForegroundWithNotification(endTimestamp, durationMinutes, sentinelOnly = false)
            startTimer(endTimestamp)
            return START_STICKY
        }

        // ACTION_START_SENTINEL or null intent (restarted by OS via START_STICKY)
        if (AppPreferences.isFocusActive()) {
            isSentinelOnlyMode = false
            val endTimestamp = AppPreferences.getFocusEndTimestamp()
            val durationMinutes = AppPreferences.getFocusDurationMinutes()
            startForegroundWithNotification(endTimestamp, durationMinutes, sentinelOnly = false)
            startTimer(endTimestamp)
        } else if (AppPreferences.isAlwaysBlockEnabled() || AppPreferences.isHourlyLimitEnabled() || ScheduleEvaluator.isFocusActive()) {
            startSentinelMode()
        } else {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        return START_STICKY
    }

    private fun startSentinelMode() {
        isSentinelOnlyMode = true
        startForegroundWithNotification(0L, 0, sentinelOnly = true)
        startTimer(0L)
    }

    private fun startForegroundWithNotification(endTimestamp: Long, durationMinutes: Int, sentinelOnly: Boolean) {
        val isEng = AppPreferences.isEnglish()
        val initialText = when {
            sentinelOnly -> if (isEng) "Distraction shield active" else "Escudo contra distracciones activo"
            endTimestamp > 0 -> if (isEng) "Focus Mode active: $durationMinutes min" else "Modo Enfoque activo: $durationMinutes min"
            else -> if (isEng) "Continuous Focus Mode active" else "Modo Enfoque continuo activo"
        }

        val notification = buildNotification(initialText, sentinelOnly)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(contentText: String, sentinelOnly: Boolean = false): Notification {
        val isEng = AppPreferences.isEnglish()
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingOpenApp = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (sentinelOnly) {
            if (isEng) "🛡️ Enigma Focus Shield" else "🛡️ Escudo Enigma Focus"
        } else {
            if (isEng) "🛡️ Focus Session" else "🛡️ Sesión de Concentración"
        }

        val builder = NotificationCompat.Builder(this, FocusApp.FOCUS_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(contentText)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingOpenApp)
            .setPriority(if (sentinelOnly) NotificationCompat.PRIORITY_MIN else NotificationCompat.PRIORITY_LOW)

        if (!sentinelOnly && !AppPreferences.isStrictModeEnabled()) {
            val stopIntent = Intent(this, FocusForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            val pendingStop = PendingIntent.getService(
                this, 1, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, if (isEng) "Stop" else "Detener", pendingStop)
        }

        return builder.build()
    }

    private fun isDeviceInteractive(): Boolean {
        return try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
            powerManager?.isInteractive ?: true
        } catch (e: Exception) {
            true
        }
    }

    private fun startTimer(endTimestamp: Long) {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            var cycleCount = 0
            while (isActive) {
                cycleCount++
                try {
                    // 1. Session Duration & Notification update
                    if (!isSentinelOnlyMode && endTimestamp > 0) {
                        val remainingMillis = endTimestamp - System.currentTimeMillis()
                        if (remainingMillis <= 0) {
                            onSessionCompleted()
                            break
                        }

                        val isEng = AppPreferences.isEnglish()
                        val hours = TimeUnit.MILLISECONDS.toHours(remainingMillis)
                        val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60
                        val seconds = TimeUnit.MILLISECONDS.toSeconds(remainingMillis) % 60
                        val prefix = if (isEng) "Time remaining" else "Tiempo restante"
                        val formatted = if (hours > 0) {
                            String.format(Locale.getDefault(), "$prefix: %02d:%02d:%02d", hours, minutes, seconds)
                        } else {
                            String.format(Locale.getDefault(), "$prefix: %02d:%02d", minutes, seconds)
                        }

                        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                        notificationManager.notify(NOTIFICATION_ID, buildNotification(formatted, sentinelOnly = false))
                    }

                    // Only run active watchdog checks when the screen is interactive (zero battery drain when screen off)
                    val interactive = isDeviceInteractive()
                    if (interactive) {
                        // 2. Health Check Accessibility Service every 2 seconds
                        if (cycleCount % 2 == 0) {
                            checkAndRecoverAccessibilityService()
                        }

                        // 3. Fallback: Secondary Foreground Usage Watchdog only when accessibility is unhealthy
                        if (!FocusAccessibilityService.isConnected || FocusAccessibilityService.isHeartbeatStale()) {
                            checkForegroundPackageFallback()
                        }
                    }

                } catch (t: Throwable) {
                    Log.e(TAG, "Error in foreground timer loop", t)
                }

                val nextDelayMs = if (isSentinelOnlyMode && !isDeviceInteractive()) 5000L else 1000L
                delay(nextDelayMs)
            }
        }
    }

    private fun checkAndRecoverAccessibilityService() {
        if (!AppPreferences.isFocusActive() &&
            !AppPreferences.isAlwaysBlockEnabled() &&
            !AppPreferences.isHourlyLimitEnabled() &&
            !ScheduleEvaluator.isFocusActive()
        ) {
            return
        }

        val isEnabled = FocusAccessibilityService.isAccessibilityServiceEnabled(this)
        val isConnected = FocusAccessibilityService.isConnected
        val now = System.currentTimeMillis()
        val heartbeatStale = isConnected && FocusAccessibilityService.isHeartbeatStale(now)

        if (!isEnabled || !isConnected || heartbeatStale) {
            if (!GrayscaleManager.hasSecureSettingsPermission(this)) {
                if (!loggedMissingSecureSettingsForRecovery) {
                    loggedMissingSecureSettingsForRecovery = true
                    Log.w(TAG, "⚠️ FocusAccessibilityService unhealthy (enabled=$isEnabled, connected=$isConnected, stale=$heartbeatStale), but WRITE_SECURE_SETTINGS is missing for auto-recovery.")
                }
                return
            }
            loggedMissingSecureSettingsForRecovery = false
            if (now - lastRestartAttemptTime > 5000L) {
                lastRestartAttemptTime = now
                Log.w(TAG, "⚠️ FocusAccessibilityService unhealthy (enabled=$isEnabled, connected=$isConnected, stale=$heartbeatStale). Triggering resurrection...")
                FocusAccessibilityService.restartAccessibilityService(this)
            }
        } else {
            loggedMissingSecureSettingsForRecovery = false
        }
    }

    private fun checkForegroundPackageFallback() {
        val isFocusActive = AppPreferences.isFocusActive() ||
            AppPreferences.isAlwaysBlockEnabled() ||
            AppPreferences.isHourlyLimitEnabled() ||
            ScheduleEvaluator.isFocusActive()
        if (!isFocusActive) return

        val foregroundPkg = UsageStatsHelper.getForegroundPackage(this) ?: return
        if (FocusInterceptor.shouldBlockPackage(packageName, foregroundPkg, context = this)) {
            if (BlockOverlayManager.isAnyBlockBarrierShowing()) return
            // Blocked app is in foreground! Launch BlockActivity as emergency barrier
            try {
                val blockIntent = Intent(this, BlockActivity::class.java).apply {
                    putExtra(BlockActivity.EXTRA_BLOCKED_PACKAGE, foregroundPkg)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_NO_ANIMATION
                }
                startActivity(blockIntent)
                Log.i(TAG, "🛡️ Fallback watchdog launched BlockActivity for $foregroundPkg")
            } catch (e: Exception) {
                Log.e(TAG, "Error launching BlockActivity fallback", e)
            }
        }
    }

    private fun onSessionCompleted() {
        AppPreferences.setFocusActive(false, 0L, 0)
        if (AppPreferences.isAutoGrayscaleEnabled()) {
            GrayscaleManager.setGrayscaleEnabled(this, false)
        }
        if (AppPreferences.isAlwaysBlockEnabled() || AppPreferences.isHourlyLimitEnabled() || ScheduleEvaluator.isFocusActive()) {
            startSentinelMode()
        } else {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun stopFocusService() {
        timerJob?.cancel()
        AppPreferences.setFocusActive(false, 0L, 0)
        if (AppPreferences.isAutoGrayscaleEnabled()) {
            GrayscaleManager.setGrayscaleEnabled(this, false)
        }
        if (AppPreferences.isAlwaysBlockEnabled() || AppPreferences.isHourlyLimitEnabled() || ScheduleEvaluator.isFocusActive()) {
            startSentinelMode()
        } else {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onDestroy() {
        timerJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }
}
