package com.example.enigmafocus.ui.main

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.enigmafocus.data.FocusInterval
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FocusScreen(
    state: MainUiState,
    viewModel: MainScreenViewModel,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var intervalToEdit by remember { mutableStateOf<FocusInterval?>(null) }
    var isCreatingNewInterval by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Warning banner if permissions are missing
        if (!state.isAccessibilityEnabled || !state.hasSecureSettingsPermission) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable { onNavigateToSettings() },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF3E1F1F)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFF8A80),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (!state.hasSecureSettingsPermission) "Permiso ADB pendiente para Escala de Grises" else "Accesibilidad desactivada",
                            color = Color(0xFFFF8A80),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Toca para revisar la configuración de permisos.",
                            color = Color(0xFFE0E0E0),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Active Scheduled Interval Live Alert
        if (state.activeScheduledInterval != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF133E24)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color(0xFF81C784),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Intervalo Programado Activo",
                            color = Color(0xFF81C784),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${state.activeScheduledInterval.label} (${state.activeScheduledInterval.formattedTimeRange()})",
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Hero Focus Card
        val activeBgColor by animateColorAsState(
            targetValue = if (state.isFocusActive) Color(0xFF1B3B2B) else Color(0xFF1E1E1E),
            label = "activeBgColor"
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = activeBgColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(if (state.isFocusActive) Color(0xFF2E7D32) else Color(0xFF2A2A2A)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (state.isFocusActive) {
                            val formattedTime = if (state.remainingMillis > 0) {
                                val hours = TimeUnit.MILLISECONDS.toHours(state.remainingMillis)
                                val minutes = TimeUnit.MILLISECONDS.toMinutes(state.remainingMillis) % 60
                                val seconds = TimeUnit.MILLISECONDS.toSeconds(state.remainingMillis) % 60
                                if (hours > 0) {
                                    String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
                                } else {
                                    String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
                                }
                            } else {
                                "ACTIVO"
                            }
                            Text(
                                text = formattedTime,
                                fontSize = if (state.remainingMillis >= 3600000L) 22.sp else 30.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "En concentración",
                                fontSize = 12.sp,
                                color = Color(0xFFA5D6A7)
                            )
                        } else {
                            val durationLabel = if (state.selectedDurationMinutes >= 60) {
                                val h = state.selectedDurationMinutes / 60
                                val m = state.selectedDurationMinutes % 60
                                if (m == 0) "$h h" else "$h h $m m"
                            } else {
                                "${state.selectedDurationMinutes} min"
                            }
                            Icon(
                                imageVector = if (state.selectedDurationMinutes >= 480) Icons.Default.Work else Icons.Default.Timer,
                                contentDescription = null,
                                tint = if (state.selectedDurationMinutes >= 480) Color(0xFF81C784) else Color(0xFF9E9E9E),
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = durationLabel,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = if (state.isFocusActive) "Sesión de Enfoque Activa" else "Sesión Manual Inmediata",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = if (state.isFocusActive)
                        "Instagram, Reddit y distracciones bloqueadas."
                    else
                        "Elige la duración o programa horarios automáticos abajo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFB0B0B0),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Start / Stop Main Button
                if (state.isFocusActive) {
                    Button(
                        onClick = { viewModel.stopFocusSession() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Detener Sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    val buttonText = if (state.selectedDurationMinutes >= 60) {
                        val h = state.selectedDurationMinutes / 60
                        val m = state.selectedDurationMinutes % 60
                        if (m == 0) "Iniciar Sesión ($h Horas)" else "Iniciar Sesión ($h h $m m)"
                    } else {
                        "Iniciar Sesión (${state.selectedDurationMinutes} min)"
                    }

                    Button(
                        onClick = { viewModel.startFocusSession() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(buttonText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Duration Presets
        AnimatedVisibility(visible = !state.isFocusActive) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        25 to "25 min",
                        60 to "1 hora",
                        240 to "4 horas",
                        540 to "9h Jornada"
                    ).forEach { (mins, label) ->
                        FilterChip(
                            selected = state.selectedDurationMinutes == mins,
                            onClick = { viewModel.setDuration(mins) },
                            label = { Text(label, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF4CAF50),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF242424),
                                labelColor = Color(0xFFB0B0B0)
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Scheduled Focus Intervals Card (Multiple work shifts / daily schedules)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Horarios e Intervalos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Bloqueo automático en tus horas de trabajo",
                            color = Color(0xFF8E8E8E),
                            fontSize = 12.sp
                        )
                    }

                    IconButton(onClick = { isCreatingNewInterval = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Añadir intervalo",
                            tint = Color(0xFF81C784)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (state.scheduledIntervals.isEmpty()) {
                    Text(
                        text = "No tienes intervalos programados. Pulsa + para añadir uno (ej. 07:00 a 16:30).",
                        color = Color(0xFF757575),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    state.scheduledIntervals.forEach { interval ->
                        val isCurrentActive = interval.isCurrentlyActive()
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrentActive) Color(0xFF183D29) else Color(0xFF282828)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = interval.label,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 15.sp
                                        )
                                        if (isCurrentActive) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFF2E7D32))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "EN CURSO",
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Alarm,
                                            contentDescription = null,
                                            tint = if (isCurrentActive) Color(0xFF81C784) else Color(0xFFB0B0B0),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = interval.formattedTimeRange(),
                                            color = if (isCurrentActive) Color(0xFF81C784) else Color(0xFFE0E0E0),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "• ${interval.formattedDays()}",
                                            color = Color(0xFF9E9E9E),
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                IconButton(onClick = { intervalToEdit = interval }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Editar",
                                        tint = Color(0xFFB0B0B0),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Switch(
                                    checked = interval.isEnabled,
                                    onCheckedChange = { viewModel.toggleInterval(interval.id) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF4CAF50)
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { isCreatingNewInterval = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF81C784))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Añadir Horario / Intervalo", color = Color(0xFF81C784))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Direct Controls Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Controles del Teléfono",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // 1. DIRECT Manual Grayscale Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (state.isGrayscaleActive) Color(0xFF2E7D32) else Color(0xFF2C2C2C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = null,
                            tint = if (state.isGrayscaleActive) Color.White else Color(0xFF9E9E9E),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Modo Escala de Grises",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (state.isGrayscaleActive) "Pantalla en Blanco y Negro" else "Pantalla a Todo Color",
                            color = if (state.isGrayscaleActive) Color(0xFF81C784) else Color(0xFF8E8E8E),
                            fontSize = 12.sp,
                            fontWeight = if (state.isGrayscaleActive) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    Switch(
                        checked = state.isGrayscaleActive,
                        onCheckedChange = {
                            if (state.hasSecureSettingsPermission) {
                                viewModel.toggleGrayscale()
                            } else {
                                onNavigateToSettings()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF4CAF50)
                        )
                    )
                }

                // 2. Auto Grayscale Option on Focus Start
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2C2C2C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (state.isAutoGrayscaleEnabled) Color(0xFF81C784) else Color(0xFF9E9E9E),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto Escala de Grises en Sesión",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (state.isAutoGrayscaleEnabled) "Se activa en blanco y negro al bloquear" else "Mantener a color durante la sesión",
                            color = Color(0xFF8E8E8E),
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = state.isAutoGrayscaleEnabled,
                        onCheckedChange = { viewModel.setAutoGrayscale(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF4CAF50)
                        )
                    )
                }

                // 3. Strict Mode (No breaks)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2C2C2C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = if (state.isStrictModeEnabled) Color(0xFFFFB74D) else Color(0xFF9E9E9E),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Modo Estricto (Sin Pausas)",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (state.isStrictModeEnabled) "Bloqueo total sin opción a usar 1 min" else "Permite desbloqueo de 1 min tras respirar",
                            color = Color(0xFF8E8E8E),
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = state.isStrictModeEnabled,
                        onCheckedChange = { viewModel.setStrictMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFFF9800)
                        )
                    )
                }

                // 4. Always Block Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2C2C2C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = null,
                            tint = Color(0xFFE57373),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bloqueo Siempre Activo",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Bloquear continuamente las 24 horas",
                            color = Color(0xFF8E8E8E),
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = state.isAlwaysBlockEnabled,
                        onCheckedChange = { viewModel.setAlwaysBlock(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF4CAF50)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Interval Edit / Create Dialog
    if (isCreatingNewInterval || intervalToEdit != null) {
        val initial = intervalToEdit ?: FocusInterval(
            label = "Nuevo Horario",
            startHour = 7,
            startMinute = 0,
            endHour = 16,
            endMinute = 30,
            isEnabled = true
        )

        var label by remember { mutableStateOf(initial.label) }
        var startHour by remember { mutableStateOf(initial.startHour) }
        var startMinute by remember { mutableStateOf(initial.startMinute) }
        var endHour by remember { mutableStateOf(initial.endHour) }
        var endMinute by remember { mutableStateOf(initial.endMinute) }
        var selectedDays by remember { mutableStateOf(initial.daysOfWeek) }

        val daysList = listOf(
            Calendar.MONDAY to "Lun",
            Calendar.TUESDAY to "Mar",
            Calendar.WEDNESDAY to "Mié",
            Calendar.THURSDAY to "Jue",
            Calendar.FRIDAY to "Vie",
            Calendar.SATURDAY to "Sáb",
            Calendar.SUNDAY to "Dom"
        )

        AlertDialog(
            onDismissRequest = {
                isCreatingNewInterval = false
                intervalToEdit = null
            },
            title = {
                Text(
                    text = if (intervalToEdit != null) "Editar Intervalo" else "Nuevo Intervalo",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("Nombre (ej. Jornada Laboral)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),
                            unfocusedBorderColor = Color(0xFF424242),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Horario de bloqueo:", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Start Time Button
                        OutlinedButton(
                            onClick = {
                                TimePickerDialog(
                                    context,
                                    { _, h, m ->
                                        startHour = h
                                        startMinute = m
                                    },
                                    startHour,
                                    startMinute,
                                    true
                                ).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Inicio", fontSize = 11.sp, color = Color(0xFFB0B0B0))
                                Text(
                                    String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF81C784)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // End Time Button
                        OutlinedButton(
                            onClick = {
                                TimePickerDialog(
                                    context,
                                    { _, h, m ->
                                        endHour = h
                                        endMinute = m
                                    },
                                    endHour,
                                    endMinute,
                                    true
                                ).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Fin", fontSize = 11.sp, color = Color(0xFFB0B0B0))
                                Text(
                                    String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF81C784)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Días activos:", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        daysList.forEach { (dayCode, dayName) ->
                            val isSelected = selectedDays.contains(dayCode)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedDays = if (isSelected) {
                                        selectedDays - dayCode
                                    } else {
                                        selectedDays + dayCode
                                    }
                                },
                                label = { Text(dayName, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF4CAF50),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF2C2C2C),
                                    labelColor = Color(0xFFB0B0B0)
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalInterval = initial.copy(
                            label = label.ifBlank { "Horario de Trabajo" },
                            startHour = startHour,
                            startMinute = startMinute,
                            endHour = endHour,
                            endMinute = endMinute,
                            daysOfWeek = if (selectedDays.isEmpty()) setOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY) else selectedDays,
                            isEnabled = true
                        )
                        viewModel.addOrUpdateInterval(finalInterval)
                        isCreatingNewInterval = false
                        intervalToEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Guardar", color = Color.White)
                }
            },
            dismissButton = {
                Row {
                    if (intervalToEdit != null) {
                        TextButton(
                            onClick = {
                                intervalToEdit?.let { viewModel.removeInterval(it.id) }
                                isCreatingNewInterval = false
                                intervalToEdit = null
                            }
                        ) {
                            Text("Eliminar", color = Color(0xFFFF8A80))
                        }
                    }
                    TextButton(
                        onClick = {
                            isCreatingNewInterval = false
                            intervalToEdit = null
                        }
                    ) {
                        Text("Cancelar", color = Color(0xFFB0B0B0))
                    }
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }
}
