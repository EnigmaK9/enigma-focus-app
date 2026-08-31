package com.example.enigmafocus.ui.block

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.enigmafocus.data.AppPreferences
import com.example.enigmafocus.theme.EnigmaFocusTheme

object BlockOverlayManager {

    private const val TAG = "BlockOverlayManager"
    private var overlayView: ComposeView? = null
    private var isOverlayShowing = false

    private class OverlayLifecycleOwner : LifecycleOwner, SavedStateRegistryOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)
        private val savedStateRegistryController = SavedStateRegistryController.create(this)

        init {
            savedStateRegistryController.performRestore(null)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }

        override val lifecycle: Lifecycle get() = lifecycleRegistry
        override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

        fun destroy() {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        }
    }

    private var currentLifecycleOwner: OverlayLifecycleOwner? = null

    fun isShowing(): Boolean = isOverlayShowing

    fun showOverlay(
        service: AccessibilityService,
        blockedPackage: String,
        onUnlockTemporary: () -> Unit
    ) {
        if (isOverlayShowing) return

        try {
            val windowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager

            val appName = try {
                val appInfo = service.packageManager.getApplicationInfo(blockedPackage, 0)
                service.packageManager.getApplicationLabel(appInfo).toString()
            } catch (e: PackageManager.NameNotFoundException) {
                blockedPackage
            }

            val layoutParams = WindowManager.LayoutParams().apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
                type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
                flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_FULLSCREEN
                format = PixelFormat.TRANSLUCENT
                gravity = Gravity.CENTER
            }

            val lifecycleOwner = OverlayLifecycleOwner()
            currentLifecycleOwner = lifecycleOwner

            val composeView = ComposeView(service).apply {
                setViewTreeLifecycleOwner(lifecycleOwner)
                setViewTreeSavedStateRegistryOwner(lifecycleOwner)
                setContent {
                    EnigmaFocusTheme(darkTheme = true) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = Color(0xFF0F1412)
                        ) {
                            DirectBreathingBlockScreen(
                                appName = appName,
                                packageName = blockedPackage,
                                onGoHome = {
                                    hideOverlay(service)
                                    service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
                                },
                                onUnlockTemporary = {
                                    AppPreferences.setTemporaryWhitelist(blockedPackage, 1)
                                    hideOverlay(service)
                                    onUnlockTemporary()
                                }
                            )
                        }
                    }
                }
            }

            windowManager.addView(composeView, layoutParams)
            overlayView = composeView
            isOverlayShowing = true
            Log.i(TAG, "✅ TYPE_ACCESSIBILITY_OVERLAY attached and showing on top!")
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying accessibility overlay", e)
        }
    }

    fun hideOverlay(context: Context) {
        if (!isOverlayShowing || overlayView == null) return

        try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.removeView(overlayView)
        } catch (e: Exception) {
            Log.e(TAG, "Error removing overlay", e)
        } finally {
            currentLifecycleOwner?.destroy()
            currentLifecycleOwner = null
            overlayView = null
            isOverlayShowing = false
            Log.i(TAG, "Overlay dismissed cleanly")
        }
    }
}
