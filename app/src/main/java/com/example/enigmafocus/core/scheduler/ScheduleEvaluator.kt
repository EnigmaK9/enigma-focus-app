package com.example.enigmafocus.core.scheduler

import com.example.enigmafocus.data.AppPreferences
import com.example.enigmafocus.data.FocusInterval
import java.util.Calendar

object ScheduleEvaluator {

    fun isSleepInterval(interval: FocusInterval): Boolean {
        val label = interval.label.lowercase()
        val isOvernightHours = interval.startHour > interval.endHour && (interval.startHour >= 20 || interval.endHour <= 9)
        return label.contains("dormir") ||
               label.contains("descanso") ||
               label.contains("sueño") ||
               label.contains("noche") ||
               label.contains("sleep") ||
               label.contains("rest") ||
               isOvernightHours
    }

    fun isSleepScheduleActive(calendar: Calendar = Calendar.getInstance()): Boolean {
        val active = AppPreferences.getActiveScheduledInterval(calendar)
        if (active != null && isSleepInterval(active)) return true

        for (interval in AppPreferences.getIntervals()) {
            if (interval.isEnabled && isSleepInterval(interval) && interval.isCurrentlyActive(calendar)) {
                return true
            }
        }

        // Fallback default: 22:30 to 06:30 overnight check
        val currentMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        val isOvernight = currentMinutes >= (22 * 60 + 30) || currentMinutes < (6 * 60 + 30)
        return isOvernight
    }

    fun isFocusActive(calendar: Calendar = Calendar.getInstance()): Boolean {
        return AppPreferences.isFocusActive() || AppPreferences.isAnyScheduledIntervalActive(calendar)
    }
}
