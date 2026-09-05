package com.example.enigmafocus.manager

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.os.Process
import android.util.Log

object UsageStatsHelper {

    private const val TAG = "UsageStatsHelper"

    fun hasUsageStatsPermission(context: Context): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            Log.e(TAG, "Error checking usage stats permission", e)
            false
        }
    }

    fun getForegroundPackage(context: Context): String? {
        if (!hasUsageStatsPermission(context)) return null

        return try {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return null
            val endTime = System.currentTimeMillis()
            val startTime = endTime - 10000 // look back 10 seconds

            val events = usageStatsManager.queryEvents(startTime, endTime)
            val event = UsageEvents.Event()
            var lastForegroundPackage: String? = null
            var lastEventTime = 0L

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                val eventType = event.eventType
                if (eventType == UsageEvents.Event.ACTIVITY_RESUMED && event.timeStamp >= lastEventTime) {
                    lastForegroundPackage = event.packageName
                    lastEventTime = event.timeStamp
                }
            }
            lastForegroundPackage
        } catch (e: Exception) {
            Log.e(TAG, "Error getting foreground package from UsageStats", e)
            null
        }
    }
}
