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
import com.example.enigmafocus.data.AppStrings
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
    val isEng = state.isEnglish

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
                            text = if (!state.hasSecureSettingsPermission) (if (isEng) "ADB Permission required for Grayscale" else "Permiso ADB pendiente para Escala de Grises") else (if (isEng) "Accessibility Service disabled" else "Accesibilidad desactivada"),
                            color = Color(0xFFFF8A80),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isEng) "Tap to open system settings & diagnostics." else "Toca para revisar la configuración de permisos.",
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
                            text = AppStrings.get("active_interval_alert", isEng),
                            color = Color(0xFF81C784),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${getLocalizedIntervalLabel(state.activeScheduledInterval.label, isEng)} (${state.activeScheduledInterval.formattedTimeRange()})",
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
                                "ACTIVE"
                            }
                            Text(
                                text = formattedTime,
                                fontSize = if (state.remainingMillis >= 3600000L) 22.sp else 30.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = AppStrings.get("in_concentration", isEng),
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
                    text = if (state.isFocusActive) AppStrings.get("focus_active_title", isEng) else AppStrings.get("focus_inactive_title", isEng),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = if (state.isFocusActive)
                        AppStrings.get("focus_active_subtitle", isEng)
                    else
                        AppStrings.get("focus_inactive_subtitle", isEng),
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
                            Text(AppStrings.get("btn_stop_session", isEng), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    val buttonText = if (state.selectedDurationMinutes >= 60) {
                        val h = state.selectedDurationMinutes / 60
                        val m = state.selectedDurationMinutes % 60
                        val hrWord = if (isEng) (if (h == 1) "Hour" else "Hours") else (if (h == 1) "Hora" else "Horas")
                        if (m == 0) "${AppStrings.get("btn_start_session", isEng)} ($h $hrWord)" else "${AppStrings.get("btn_start_session", isEng)} ($h h $m m)"
                    } else {
                        "${AppStrings.get("btn_start_session", isEng)} (${state.selectedDurationMinutes} min)"
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
                        25 to AppStrings.get("min_25", isEng),
                        60 to AppStrings.get("hr_1", isEng),
                        240 to AppStrings.get("hr_4", isEng),
                        540 to AppStrings.get("hr_9_workday", isEng)
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

        // Scheduled Focus Intervals Card
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
                            text = AppStrings.get("intervals_card_title", isEng),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = AppStrings.get("intervals_card_subtitle", isEng),
                            color = Color(0xFF8E8E8E),
                            fontSize = 12.sp
                        )
                    }

                    IconButton(onClick = { isCreatingNewInterval = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = AppStrings.get("btn_add_interval", isEng),
                            tint = Color(0xFF81C784)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

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
                                        text = getLocalizedIntervalLabel(interval.label, isEng),
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
                                                text = AppStrings.get("badge_in_progress", isEng),
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
                                        text = "• ${interval.formattedDays(isEng)}",
                                        color = Color(0xFF9E9E9E),
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            IconButton(onClick = { intervalToEdit = interval }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
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

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { isCreatingNewInterval = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF81C784))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(AppStrings.get("btn_add_interval", isEng), color = Color(0xFF81C784))
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
                    text = AppStrings.get("phone_controls_title", isEng),
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
                            text = AppStrings.get("grayscale_mode_title", isEng),
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (state.isGrayscaleActive) AppStrings.get("grayscale_mode_active", isEng) else AppStrings.get("grayscale_mode_inactive", isEng),
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
                            text = AppStrings.get("auto_grayscale_title", isEng),
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Text(
                            text = AppStrings.get("auto_grayscale_desc", isEng),
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
                            text = AppStrings.get("strict_mode_title", isEng),
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Text(
                            text = AppStrings.get("strict_mode_desc", isEng),
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
                            text = AppStrings.get("always_block_title", isEng),
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Text(
                            text = AppStrings.get("always_block_desc", isEng),
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
            label = if (isEng) "Work Schedule" else "Nuevo Horario",
            startHour = 7,
            startMinute = 30,
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
            Calendar.MONDAY to (if (isEng) "Mon" else "Lun"),
            Calendar.TUESDAY to (if (isEng) "Tue" else "Mar"),
            Calendar.WEDNESDAY to (if (isEng) "Wed" else "Mié"),
            Calendar.THURSDAY to (if (isEng) "Thu" else "Jue"),
            Calendar.FRIDAY to (if (isEng) "Fri" else "Vie"),
            Calendar.SATURDAY to (if (isEng) "Sat" else "Sáb"),
            Calendar.SUNDAY to (if (isEng) "Sun" else "Dom")
        )

        AlertDialog(
            onDismissRequest = {
                isCreatingNewInterval = false
                intervalToEdit = null
            },
            title = {
                Text(
                    text = if (intervalToEdit != null) (if (isEng) "Edit Interval" else "Editar Intervalo") else (if (isEng) "New Interval" else "Nuevo Intervalo"),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text(if (isEng) "Label (e.g. Workday)" else "Nombre (ej. Jornada)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),
                            unfocusedBorderColor = Color(0xFF424242),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(if (isEng) "Time Range:" else "Horario de bloqueo:", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
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
                                Text(if (isEng) "Start" else "Inicio", fontSize = 11.sp, color = Color(0xFFB0B0B0))
                                Text(
                                    String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF81C784)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

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
                                Text(if (isEng) "End" else "Fin", fontSize = 11.sp, color = Color(0xFFB0B0B0))
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

                    Text(if (isEng) "Active Days:" else "Días activos:", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
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
                            label = label.ifBlank { if (isEng) "Workday Shift" else "Jornada Laboral" },
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
                    Text(if (isEng) "Save" else "Guardar", color = Color.White)
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
                            Text(if (isEng) "Delete" else "Eliminar", color = Color(0xFFFF8A80))
                        }
                    }
                    TextButton(
                        onClick = {
                            isCreatingNewInterval = false
                            intervalToEdit = null
                        }
                    ) {
                        Text(if (isEng) "Cancel" else "Cancelar", color = Color(0xFFB0B0B0))
                    }
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }
}

fun getLocalizedIntervalLabel(label: String, isEnglish: Boolean): String {
    val l = label.lowercase()
    return when {
        l.contains("workday") || l.contains("jornada") || l.contains("laboral") || l.contains("trabajo") -> {
            AppStrings.get("interval_workday_label", isEnglish)
        }
        l.contains("sleep") || l.contains("dormir") || l.contains("descanso") || l.contains("sueño") || l.contains("noche") || l.contains("rest") -> {
            AppStrings.get("interval_sleep_label", isEnglish)
        }
        l.contains("vespertino") || l.contains("evening") -> {
            if (isEnglish) "Evening Study" else "Estudio Vespertino"
        }
        else -> label
    }
}
