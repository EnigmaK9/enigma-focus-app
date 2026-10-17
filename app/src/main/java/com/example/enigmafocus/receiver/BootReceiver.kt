package com.example.enigmafocus.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.enigmafocus.data.AppPreferences
import com.example.enigmafocus.manager.FocusSessionManager
import com.example.enigmafocus.service.FocusAccessibilityService

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.i(TAG, "Received broadcast action: $action")

        if (action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            try {
                AppPreferences.init(context)
                FocusSessionManager.syncSentinelService(context)
                if (!FocusAccessibilityService.isConnected) {
                    FocusAccessibilityService.restartAccessibilityService(context)
                }
                Log.i(TAG, "Enigma Focus preferences & sentinel service initialized on boot/update")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing on boot", e)
            }
        }
    }
}
