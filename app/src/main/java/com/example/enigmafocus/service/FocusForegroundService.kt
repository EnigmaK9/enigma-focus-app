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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit

class FocusForegroundService : Service() {

    companion object {
        private const val TAG = "FocusForegroundService"
        const val ACTION_START = "ACTION_START_FOCUS"
        const val ACTION_STOP = "ACTION_STOP_FOCUS"
        const val EXTRA_DURATION_MINUTES = "extra_duration_minutes"
        const val EXTRA_END_TIMESTAMP = "extra_end_timestamp"
        private const val NOTIFICATION_ID = 1001
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null
    private var lastRestartAttemptTime = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        if (action == ACTION_STOP) {
            stopFocusService()
            return START_NOT_STICKY
        }

        if (action == ACTION_START) {
            val endTimestamp = intent.getLongExtra(EXTRA_END_TIMESTAMP, 0L)
            val durationMinutes = intent.getIntExtra(EXTRA_DURATION_MINUTES, 25)
            startForegroundWithNotification(endTimestamp, durationMinutes)
            startTimer(endTimestamp)
        }

        return START_STICKY
    }

    private fun startForegroundWithNotification(endTimestamp: Long, durationMinutes: Int) {
        val isEng = AppPreferences.isEnglish()
        val initialText = if (endTimestamp > 0) {
            if (isEng) "Focus Mode active: $durationMinutes min" else "Modo Enfoque activo: $durationMinutes min"
        } else {
            if (isEng) "Continuous Focus Mode active" else "Modo Enfoque continuo activo"
        }

        val notification = buildNotification(initialText)

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

    private fun buildNotification(contentText: String): Notification {
        val isEng = AppPreferences.isEnglish()
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingOpenApp = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, FocusForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val pendingStop = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, FocusApp.FOCUS_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(if (isEng) "🛡️ Focus Session" else "🛡️ Sesión de Concentración")
            .setContentText(contentText)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingOpenApp)
            .addAction(0, if (isEng) "Stop" else "Detener", pendingStop)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun startTimer(endTimestamp: Long) {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            var cycleCount = 0
            while (isActive) {
                cycleCount++
                try {
                    // 1. Session Duration & Notification update
                    if (endTimestamp > 0) {
                        val remainingMillis = endTimestamp - System.currentTimeMillis()
                        if (remainingMillis <= 0) {
                            // Completed!
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
                        notificationManager.notify(NOTIFICATION_ID, buildNotification(formatted))
                    }

                    // 2. Health Check Accessibility Service every 2 seconds
                    if (cycleCount % 2 == 0) {
                        checkAndRecoverAccessibilityService()
                    }

                    // 3. Fallback: Secondary Foreground Usage Watchdog
                    checkForegroundPackageFallback()

                } catch (t: Throwable) {
                    Log.e(TAG, "Error in foreground timer loop", t)
                }

                delay(1000)
            }
        }
    }

    private fun checkAndRecoverAccessibilityService() {
        if (!AppPreferences.isFocusActive() && !AppPreferences.isAlwaysBlockEnabled() && !ScheduleEvaluator.isFocusActive()) {
            return
        }

        val isEnabled = FocusAccessibilityService.isAccessibilityServiceEnabled(this)
        val isConnected = FocusAccessibilityService.isConnected
        val now = System.currentTimeMillis()
        val heartbeatStale = (now - FocusAccessibilityService.lastHeartbeatTime) > 4000

        if (!isEnabled || !isConnected || heartbeatStale) {
            if (now - lastRestartAttemptTime > 3000) {
                lastRestartAttemptTime = now
                Log.w(TAG, "⚠️ FocusAccessibilityService unhealthy (enabled=$isEnabled, connected=$isConnected, stale=$heartbeatStale). Triggering resurrection...")
                FocusAccessibilityService.restartAccessibilityService(this)
            }
        }
    }

    private fun checkForegroundPackageFallback() {
        val isFocusActive = AppPreferences.isFocusActive() || AppPreferences.isAlwaysBlockEnabled() || ScheduleEvaluator.isFocusActive()
        if (!isFocusActive) return

        val foregroundPkg = UsageStatsHelper.getForegroundPackage(this) ?: return
        if (FocusInterceptor.shouldBlockPackage(packageName, foregroundPkg)) {
            // Blocked app is in foreground! Launch BlockActivity as emergency barrier
            try {
                val blockIntent = Intent(this, BlockActivity::class.java).apply {
                    putExtra(BlockActivity.EXTRA_BLOCKED_PACKAGE, foregroundPkg)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
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
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun stopFocusService() {
        timerJob?.cancel()
        AppPreferences.setFocusActive(false, 0L, 0)
        if (AppPreferences.isAutoGrayscaleEnabled()) {
            GrayscaleManager.setGrayscaleEnabled(this, false)
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        timerJob?.cancel()
        super.onDestroy()
    }
}
