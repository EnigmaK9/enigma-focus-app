package com.example.enigmafocus.ui.block

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.enigmafocus.data.AppPreferences
import com.example.enigmafocus.data.AppStrings
import com.example.enigmafocus.service.FocusAccessibilityService
import com.example.enigmafocus.theme.EnigmaFocusTheme
import kotlinx.coroutines.delay

class BlockActivity : ComponentActivity() {

    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "blocked_package"
        var isBlockScreenShowing = false
    }

    private val blockedPkgState = mutableStateOf("")
    private val appNameState = mutableStateOf("")
    private val sessionCounter = mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppPreferences.init(applicationContext)

        updatePackageFromIntent(intent)

        setContent {
            EnigmaFocusTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0F1412)
                ) {
                    key(sessionCounter.intValue) {
                        DirectBreathingBlockScreen(
                            appName = appNameState.value,
                            packageName = blockedPkgState.value,
                            onGoHome = {
                                goToHomeScreen()
                            },
                            onUnlockTemporary = {
                                AppPreferences.setTemporaryWhitelist(blockedPkgState.value, 1)
                                FocusAccessibilityService.clearDebounce()
                                finishAndRemoveTask()
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        updatePackageFromIntent(intent)
        sessionCounter.intValue++
    }

    private fun updatePackageFromIntent(intent: Intent?) {
        val pkg = intent?.getStringExtra(EXTRA_BLOCKED_PACKAGE) ?: ""
        blockedPkgState.value = pkg
        appNameState.value = try {
            val appInfo = packageManager.getApplicationInfo(pkg, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            pkg.ifEmpty { "Esta aplicación" }
        }
    }

    private fun goToHomeScreen() {
        FocusAccessibilityService.clearDebounce()
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(homeIntent)
        finishAndRemoveTask()
    }

    override fun onResume() {
        super.onResume()
        isBlockScreenShowing = true
    }

    override fun onPause() {
        super.onPause()
        isBlockScreenShowing = false
    }

    override fun onDestroy() {
        isBlockScreenShowing = false
        FocusAccessibilityService.clearDebounce()
        super.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        goToHomeScreen()
    }
}

@Composable
fun DirectBreathingBlockScreen(
    appName: String,
    packageName: String,
    onGoHome: () -> Unit,
    onUnlockTemporary: () -> Unit
) {
    val startTime = rememberSaveable(packageName) { System.currentTimeMillis() }
    var secondsLeft by rememberSaveable(packageName) { mutableIntStateOf(10) }
    var breathingPhase by rememberSaveable(packageName) { mutableStateOf(if (isEng) "Inhale deeply through your nose" else "Inhala profundamente por la nariz") }

    LaunchedEffect(key1 = packageName) {
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
            delay(250)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "breathe")
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breatheScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top App Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A20)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF81C784),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${AppStrings.get("block_access_to", isEng)} $appName",
                        color = Color(0xFFA5D6A7),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = AppStrings.get("block_breathe_title", isEng),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = AppStrings.get("block_breathe_subtitle", isEng),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF8E8E8E),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Center Breathing Bubble
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier.size(220.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer pulsating halo
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(breatheScale)
                        .clip(CircleShape)
                        .background(Color(0xFF2E7D32).copy(alpha = 0.25f))
                )

                // Middle ring
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .scale(breatheScale * 0.92f)
                        .clip(CircleShape)
                        .background(Color(0xFF388E3C).copy(alpha = 0.40f))
                )

                // Core Circle
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(if (secondsLeft > 0) Color(0xFF4CAF50) else Color(0xFF1B5E20)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (secondsLeft > 0) {
                            Text(
                                text = "${secondsLeft}s",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isEng) "Breathe" else "Respira",
                                fontSize = 11.sp,
                                color = Color(0xFFC8E6C9)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = breathingPhase,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (secondsLeft > 0) Color(0xFFA5D6A7) else Color(0xFF81C784),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main Button: Keep Focus (Go Home)
            Button(
                onClick = onGoHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppStrings.get("btn_back_to_focus", isEng),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val isStrict = AppPreferences.isStrictModeEnabled()

            if (!isStrict) {
                // Secondary Button: Temporary Unlock (Only if strict mode is OFF)
                OutlinedButton(
                    onClick = onUnlockTemporary,
                    enabled = secondsLeft == 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = if (secondsLeft == 0) Color(0xFFFFB74D) else Color(0xFF555555),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (secondsLeft > 0) (if (isEng) "Pause for 1 min (Wait ${secondsLeft}s)" else "Pausa de 1 min (Espera $secondsLeft s)") else AppStrings.get("btn_use_1_min", isEng),
                            color = if (secondsLeft == 0) Color(0xFFFFB74D) else Color(0xFF555555),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF221A1A))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFFFF8A80),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEng) "Strict Mode Active: 1-minute unlocks disabled" else "Modo Estricto Activo: desbloqueos de 1 min deshabilitados",
                            color = Color(0xFFFF8A80),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
