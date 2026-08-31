package com.example.enigmafocus.ui.block

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.enigmafocus.service.FocusAccessibilityService
import com.example.enigmafocus.theme.EnigmaFocusTheme
import kotlinx.coroutines.delay

class BlockActivity : ComponentActivity() {

    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "extra_blocked_package"
        var isBlockScreenShowing = false
    }

    private val blockedPkgState = mutableStateOf("")
    private val appNameState = mutableStateOf("")
    private val sessionCounter = mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isBlockScreenShowing = true

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
    var secondsLeft by remember { mutableIntStateOf(10) }
    var breathingPhase by remember { mutableStateOf("Inhala profundo...") }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            breathingPhase = when {
                secondsLeft in 8..10 -> "Inhala profundamente..."
                secondsLeft in 5..7 -> "Sostén el aire..."
                else -> "Exhala despacio y relaja..."
            }
            delay(1000)
            secondsLeft--
        }
        breathingPhase = "¡Excelente! Mente despejada."
    }

    val infiniteTransition = rememberInfiniteTransition(label = "breatheAnimation")
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breatheScale"
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF2E1C1C))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFFFF8A80),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Acceso bloqueado: $appName",
                        color = Color(0xFFFF8A80),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Toma una pausa consciente",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Rompe el ciclo del impulso automático. Respira antes de decidir.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9E9E9E),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

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
                                text = "Respira",
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
                        text = "Volver a mi enfoque",
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
                            text = if (secondsLeft > 0) "Pausa de 1 min (Espera $secondsLeft s)" else "Usar app por 1 minuto",
                            color = if (secondsLeft == 0) Color(0xFFFFB74D) else Color(0xFF555555),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF241C14))
                        .padding(vertical = 10.dp, horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔒 Modo Estricto: Sin excepciones",
                        color = Color(0xFFFFB74D),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
