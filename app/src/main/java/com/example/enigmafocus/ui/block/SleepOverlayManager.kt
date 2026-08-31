package com.example.enigmafocus.ui.block

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.PixelFormat
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.enigmafocus.data.AppPreferences
import com.example.enigmafocus.data.AppStrings
import com.example.enigmafocus.theme.EnigmaFocusTheme

object SleepOverlayManager {

    private const val TAG = "SleepOverlayManager"
    private var sleepOverlayView: ComposeView? = null
    private var isSleepOverlayShowing = false
    private var lastDismissedTime = 0L
    private const val SNOOZE_DURATION_MILLIS = 10 * 60 * 1000L // 10 minutes

    private class SleepLifecycleOwner : LifecycleOwner, SavedStateRegistryOwner {
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

    private var currentLifecycleOwner: SleepLifecycleOwner? = null

    fun isShowing(): Boolean = isSleepOverlayShowing

    fun canShowNudge(): Boolean {
        if (isSleepOverlayShowing) return false
        val now = System.currentTimeMillis()
        return (now - lastDismissedTime) >= SNOOZE_DURATION_MILLIS
    }

    fun showSleepNudge(service: AccessibilityService) {
        if (isSleepOverlayShowing) return

        try {
            val windowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager

            val layoutParams = WindowManager.LayoutParams().apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
                type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
                flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                format = PixelFormat.TRANSLUCENT
                gravity = Gravity.CENTER
            }

            val lifecycleOwner = SleepLifecycleOwner()
            currentLifecycleOwner = lifecycleOwner

            val isEng = AppPreferences.isEnglish()

            val composeView = ComposeView(service).apply {
                setViewTreeLifecycleOwner(lifecycleOwner)
                setViewTreeSavedStateRegistryOwner(lifecycleOwner)
                setContent {
                    EnigmaFocusTheme(darkTheme = true) {
                        SleepNudgeScreen(
                            isEng = isEng,
                            onGoToSleep = {
                                dismiss(service)
                                service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                                    service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
                                }
                            },
                            onSnooze = {
                                dismiss(service)
                            }
                        )
                    }
                }
            }

            windowManager.addView(composeView, layoutParams)
            sleepOverlayView = composeView
            isSleepOverlayShowing = true
            Log.i(TAG, "🌙 Sleep reminder overlay displayed on screen")
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying sleep overlay", e)
        }
    }

    fun dismiss(context: Context) {
        if (!isSleepOverlayShowing || sleepOverlayView == null) return

        lastDismissedTime = System.currentTimeMillis()
        try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.removeView(sleepOverlayView)
        } catch (e: Exception) {
            Log.e(TAG, "Error removing sleep overlay", e)
        } finally {
            currentLifecycleOwner?.destroy()
            currentLifecycleOwner = null
            sleepOverlayView = null
            isSleepOverlayShowing = false
            Log.i(TAG, "Sleep overlay dismissed")
        }
    }
}

@Composable
fun SleepNudgeScreen(
    isEng: Boolean = true,
    onGoToSleep: () -> Unit,
    onSnooze: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "moonGlow")
    val moonScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "moonScale"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xEB070A12)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111728)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Glowing Moon Icon
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .scale(moonScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFF3F51B5), Color(0xFF1A237E))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = "Sleep",
                            tint = Color(0xFFFFE082),
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = AppStrings.get("sleep_nudge_title", isEng),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = AppStrings.get("sleep_nudge_body", isEng),
                        fontSize = 14.sp,
                        color = Color(0xFFC5CAE9),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(26.dp))

                    // Primary Action: Lock and Sleep
                    Button(
                        onClick = onGoToSleep,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C6BC0))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(AppStrings.get("btn_sleep_lock", isEng), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Secondary Action: Snooze 10 min
                    OutlinedButton(
                        onClick = onSnooze,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(AppStrings.get("btn_sleep_snooze", isEng), fontSize = 13.sp, color = Color(0xFF9FA8DA))
                    }
                }
            }
        }
    }
}
