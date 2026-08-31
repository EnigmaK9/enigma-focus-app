package com.example.enigmafocus.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.enigmafocus.data.AppPreferences

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.i(TAG, "Received broadcast action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            try {
                AppPreferences.init(context)
                Log.i(TAG, "Enigma Focus preferences re-initialized on boot/update")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing on boot", e)
            }
        }
    }
}
