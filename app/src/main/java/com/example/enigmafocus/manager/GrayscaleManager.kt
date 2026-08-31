package com.example.enigmafocus.manager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log

object GrayscaleManager {

    private const val TAG = "GrayscaleManager"
    private const val SETTING_DALTONIZER_ENABLED = "accessibility_display_daltonizer_enabled"
    private const val SETTING_DALTONIZER = "accessibility_display_daltonizer"

    // 0 = Monochrome (Grayscale), -1 = Disabled, 11 = Protanomaly, 12 = Deuteranomaly, 13 = Tritanomaly
    private const val DALTONIZER_MONOCHROME = 0
    private const val DALTONIZER_DISABLED = -1

    fun hasSecureSettingsPermission(context: Context): Boolean {
        return context.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED
    }

    fun isGrayscaleActive(context: Context): Boolean {
        return try {
            val isEnabled = Settings.Secure.getInt(
                context.contentResolver,
                SETTING_DALTONIZER_ENABLED,
                0
            ) == 1
            val mode = Settings.Secure.getInt(
                context.contentResolver,
                SETTING_DALTONIZER,
                DALTONIZER_DISABLED
            )
            isEnabled && mode == DALTONIZER_MONOCHROME
        } catch (e: Exception) {
            Log.e(TAG, "Error checking grayscale state", e)
            false
        }
    }

    fun setGrayscaleEnabled(context: Context, enabled: Boolean): Boolean {
        if (!hasSecureSettingsPermission(context)) {
            Log.w(TAG, "Cannot change grayscale: WRITE_SECURE_SETTINGS permission not granted")
            return false
        }

        return try {
            val contentResolver = context.contentResolver
            if (enabled) {
                Settings.Secure.putInt(contentResolver, SETTING_DALTONIZER_ENABLED, 1)
                Settings.Secure.putInt(contentResolver, SETTING_DALTONIZER, DALTONIZER_MONOCHROME)
            } else {
                Settings.Secure.putInt(contentResolver, SETTING_DALTONIZER_ENABLED, 0)
                Settings.Secure.putInt(contentResolver, SETTING_DALTONIZER, DALTONIZER_DISABLED)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle grayscale", e)
            false
        }
    }

    fun toggleGrayscale(context: Context): Boolean {
        val currentState = isGrayscaleActive(context)
        return setGrayscaleEnabled(context, !currentState)
    }

    fun getAdbCommand(context: Context): String {
        return "adb shell pm grant ${context.packageName} android.permission.WRITE_SECURE_SETTINGS"
    }
}
