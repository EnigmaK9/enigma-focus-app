package com.example.enigmafocus.core.interceptor

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityWindowInfo

data class DetectedWindowThreat(
    val packageName: String,
    val isPictureInPicture: Boolean
)

object MultiWindowDetector {

    private const val TAG = "MultiWindowDetector"

    fun findBlockedWindowThreat(
        service: AccessibilityService,
        ownPackageName: String
    ): DetectedWindowThreat? {
        try {
            val windows = service.windows
            if (!windows.isNullOrEmpty()) {
                for (window in windows) {
                    val isPip = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        window.isInPictureInPictureMode
                    } else {
                        false
                    }

                    val isAppWindow = window.type == AccessibilityWindowInfo.TYPE_APPLICATION ||
                                      window.type == AccessibilityWindowInfo.TYPE_SPLIT_SCREEN_DIVIDER ||
                                      isPip

                    if (isAppWindow) {
                        val root = window.root ?: continue
                        val pkg = root.packageName?.toString() ?: continue
                        if (FocusInterceptor.shouldBlockPackage(ownPackageName, pkg)) {
                            Log.i(TAG, "🎯 Detected blocked package in multi-window/PiP: $pkg (pip=$isPip)")
                            return DetectedWindowThreat(packageName = pkg, isPictureInPicture = isPip)
                        }
                    }
                }
            }

            // Fallback to rootInActiveWindow if windows list was empty or unavailable
            val activeRoot = service.rootInActiveWindow
            val activePkg = activeRoot?.packageName?.toString()
            if (!activePkg.isNullOrBlank() && FocusInterceptor.shouldBlockPackage(ownPackageName, activePkg)) {
                return DetectedWindowThreat(packageName = activePkg, isPictureInPicture = false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error inspecting accessibility windows", e)
        }
        return null
    }
}
