package com.example.enigmafocus.manager

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.enigmafocus.core.scheduler.ScheduleEvaluator
import com.example.enigmafocus.data.AppPreferences
import com.example.enigmafocus.service.FocusAccessibilityService
import com.example.enigmafocus.service.FocusForegroundService

object FocusSessionManager {

    private const val TAG = "FocusSessionManager"

    fun syncSentinelService(context: Context) {
        try {
            val shouldRun = AppPreferences.isFocusActive() ||
                            AppPreferences.isAlwaysBlockEnabled() ||
                            AppPreferences.isHourlyLimitEnabled() ||
                            ScheduleEvaluator.isFocusActive()

            if (shouldRun && !AppPreferences.isFocusActive()) {
                val intent = Intent(context, FocusForegroundService::class.java).apply {
                    action = FocusForegroundService.ACTION_START_SENTINEL
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } else if (!shouldRun) {
                val intent = Intent(context, FocusForegroundService::class.java).apply {
                    action = FocusForegroundService.ACTION_STOP
                }
                context.startService(intent)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to sync sentinel foreground service from current context", e)
        }
    }

    fun startSession(context: Context, durationMinutes: Int) {
        val endTimestamp = if (durationMinutes > 0) {
            System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
        } else {
            0L // Continuous
        }

        // Clear any leftover whitelist so blocking is 100% active
        AppPreferences.clearTemporaryWhitelist()
        FocusAccessibilityService.clearDebounce()

        AppPreferences.setFocusActive(true, endTimestamp, durationMinutes)

        // Turn on Grayscale only if specifically enabled
        if (AppPreferences.isAutoGrayscaleEnabled()) {
            GrayscaleManager.setGrayscaleEnabled(context, true)
        } else {
            // Explicitly force color mode if auto grayscale is off
            GrayscaleManager.setGrayscaleEnabled(context, false)
        }

        // Start Foreground Service for ongoing timer notification
        try {
            val intent = Intent(context, FocusForegroundService::class.java).apply {
                action = FocusForegroundService.ACTION_START
                putExtra(FocusForegroundService.EXTRA_DURATION_MINUTES, durationMinutes)
                putExtra(FocusForegroundService.EXTRA_END_TIMESTAMP, endTimestamp)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting FocusForegroundService", e)
        }
    }

    fun stopSession(context: Context) {
        AppPreferences.setFocusActive(false, 0L, 0)
        AppPreferences.clearTemporaryWhitelist()
        FocusAccessibilityService.clearDebounce()

        // Always restore full normal colors on session stop
        GrayscaleManager.setGrayscaleEnabled(context, false)

        // Stop Foreground Service (or transition to Sentinel if Always Block is on)
        try {
            val intent = Intent(context, FocusForegroundService::class.java).apply {
                action = FocusForegroundService.ACTION_STOP
            }
            context.startService(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping FocusForegroundService", e)
        }
    }

    fun toggleSession(context: Context, defaultDurationMinutes: Int = 25) {
        if (AppPreferences.isFocusActive()) {
            stopSession(context)
        } else {
            startSession(context, defaultDurationMinutes)
        }
    }
}
