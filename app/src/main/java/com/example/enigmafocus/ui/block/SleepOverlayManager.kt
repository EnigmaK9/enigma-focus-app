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
import androidx.compose.runtime.setValue
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
    private const val SNOOZE_DURATION_MILLIS = 1 * 60 * 1000L // Strictly 1 minute (60 seconds)

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

    fun resetDismissedTime() {
        lastDismissedTime = 0L
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
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_FULLSCREEN
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
                            onEmergencyOneMin = {
                                dismiss(service)
                            }
                        )
                    }
                }
            }

            windowManager.addView(composeView, layoutParams)
            sleepOverlayView = composeView
            isSleepOverlayShowing = true
            Log.i(TAG, "🌙 Aggressive sleep lock overlay displayed on screen")
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
            Log.i(TAG, "Sleep overlay dismissed for 1 min")
        }
    }
}

@Composable
fun SleepNudgeScreen(
    isEng: Boolean = true,
    onGoToSleep: () -> Unit,
    onEmergencyOneMin: () -> Unit
) {
    val startTime = androidx.compose.runtime.saveable.rememberSaveable { System.currentTimeMillis() }
    var secondsLeft by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableIntStateOf(10) }
    var breathingPhase by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableStateOf(
            if (isEng) "Inhale deeply through your nose" else "Inhala profundamente por la nariz"
        )
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            val elapsed = (System.currentTimeMillis() - startTime) / 1000
            val remaining = (10 - elapsed).coerceAtLeast(0).toInt()
            secondsLeft = remaining
            breathingPhase = when (remaining) {
                in 8..10 -> if (isEng) "Inhale deeply through your nose" else "Inhala profundamente por la nariz"
                in 5..7 -> if (isEng) "Hold your breath calmly" else "Sostén el aire con calma"
                in 1..4 -> if (isEng) "Exhale slowly and release tension" else "Exhala suavemente y suelta tensión"
                else -> if (isEng) "Take a conscious choice" else "Toma una decisión consciente"
            }
            if (remaining <= 0) break
            kotlinx.coroutines.delay(250)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "moonGlow")
    val moonScale by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "moonScale"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xF2060911)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Badge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF19233A)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = null,
                            tint = Color(0xFFFFE082),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEng) "🌙 Sleep Mode Active (22:30 - 06:30)" else "🌙 Horario de Sueño Activo (22:30 - 06:30)",
                            color = Color(0xFFFFE082),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = AppStrings.get("sleep_nudge_title", isEng),
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = AppStrings.get("sleep_nudge_body", isEng),
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = Color(0xFFC5CAE9),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            // Central Moon & Mindful Breathing Circle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier.size(190.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer pulsating halo
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .scale(moonScale)
                            .clip(CircleShape)
                            .background(Color(0xFF3F51B5).copy(alpha = 0.22f))
                    )

                    // Middle ring
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .scale(moonScale * 0.94f)
                            .clip(CircleShape)
                            .background(Color(0xFF303F9F).copy(alpha = 0.35f))
                    )

                    // Core Circle
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(
                                if (secondsLeft > 0) Color(0xFF3F51B5) else Color(0xFF1A237E)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (secondsLeft > 0) {
                                Text(
                                    text = "${secondsLeft}s",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isEng) "Breathe" else "Respira",
                                    fontSize = 10.sp,
                                    color = Color(0xFFC5CAE9)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Bedtime,
                                    contentDescription = null,
                                    tint = Color(0xFFFFE082),
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = breathingPhase,
                    style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (secondsLeft > 0) Color(0xFF9FA8DA) else Color(0xFFFFE082),
                    textAlign = TextAlign.Center
                )
            }

            // Bottom Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Primary Action: Turn off screen & Sleep
                Button(
                    onClick = onGoToSleep,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = AppStrings.get("btn_sleep_lock", isEng),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val isStrict = AppPreferences.isStrictModeEnabled()

                if (!isStrict) {
                    // Secondary Action: Emergency 1 min (disabled during 10s breathing)
                    OutlinedButton(
                        onClick = onEmergencyOneMin,
                        enabled = secondsLeft == 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = if (secondsLeft > 0) {
                                String.format(AppStrings.get("btn_sleep_snooze_wait", isEng), secondsLeft)
                            } else {
                                AppStrings.get("btn_sleep_snooze", isEng)
                            },
                            color = if (secondsLeft == 0) Color(0xFFFFB74D) else Color(0xFF555555),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF241515))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = AppStrings.get("sleep_strict_warning", isEng),
                            color = Color(0xFFFF8A80),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
