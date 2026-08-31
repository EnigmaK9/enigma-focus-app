package com.example.enigmafocus.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.enigmafocus.data.AppPreferences
import com.example.enigmafocus.manager.FocusSessionManager
import com.example.enigmafocus.manager.GrayscaleManager

class FocusCommandReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "FocusCommandReceiver"
        const val ACTION_START_SESSION = "com.example.enigmafocus.action.START_SESSION"
        const val ACTION_STOP_SESSION = "com.example.enigmafocus.action.STOP_SESSION"
        const val ACTION_TOGGLE_GRAYSCALE = "com.example.enigmafocus.action.TOGGLE_GRAYSCALE"
        const val ACTION_SET_GRAYSCALE = "com.example.enigmafocus.action.SET_GRAYSCALE"
        const val ACTION_SET_ALWAYS_BLOCK = "com.example.enigmafocus.action.SET_ALWAYS_BLOCK"

        const val EXTRA_DURATION_MINUTES = "duration_minutes"
        const val EXTRA_ENABLED = "enabled"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.i(TAG, "Received CLI/Tasker intent action: $action")

        try {
            AppPreferences.init(context)

            when (action) {
                ACTION_START_SESSION -> {
                    val duration = intent.getIntExtra(EXTRA_DURATION_MINUTES, 25)
                    FocusSessionManager.startSession(context, duration)
                    Log.i(TAG, "Started focus session for $duration minutes via command")
                }
                ACTION_STOP_SESSION -> {
                    FocusSessionManager.stopSession(context)
                    Log.i(TAG, "Stopped focus session via command")
                }
                ACTION_TOGGLE_GRAYSCALE -> {
                    GrayscaleManager.toggleGrayscale(context)
                    Log.i(TAG, "Toggled grayscale via command")
                }
                ACTION_SET_GRAYSCALE -> {
                    val enabled = intent.getBooleanExtra(EXTRA_ENABLED, true)
                    GrayscaleManager.setGrayscaleEnabled(context, enabled)
                    Log.i(TAG, "Set grayscale to $enabled via command")
                }
                ACTION_SET_ALWAYS_BLOCK -> {
                    val enabled = intent.getBooleanExtra(EXTRA_ENABLED, true)
                    AppPreferences.setAlwaysBlockEnabled(enabled)
                    Log.i(TAG, "Set always block to $enabled via command")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling command intent", e)
        }
    }
}
