package com.example.enigmafocus.data

import java.util.Calendar
import java.util.Locale
import java.util.UUID

data class FocusInterval(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val daysOfWeek: Set<Int> = setOf(
        Calendar.MONDAY,
        Calendar.TUESDAY,
        Calendar.WEDNESDAY,
        Calendar.THURSDAY,
        Calendar.FRIDAY,
        Calendar.SATURDAY,
        Calendar.SUNDAY
    ),
    val isEnabled: Boolean = true
) {
    fun isCurrentlyActive(calendar: Calendar = Calendar.getInstance()): Boolean {
        if (!isEnabled) return false

        val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
        val currentMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        val startMins = startHour * 60 + startMinute
        val endMins = endHour * 60 + endMinute

        return if (startMins <= endMins) {
            if (!daysOfWeek.contains(currentDay)) return false
            currentMinutes in startMins until endMins
        } else {
            // Spans across midnight (e.g. 22:30 to 06:30)
            val isBeforeMidnight = currentMinutes >= startMins
            val isAfterMidnight = currentMinutes < endMins

            if (isBeforeMidnight) {
                daysOfWeek.contains(currentDay)
            } else if (isAfterMidnight) {
                val prevDay = if (currentDay == Calendar.SUNDAY) Calendar.SATURDAY else currentDay - 1
                daysOfWeek.contains(prevDay)
            } else {
                false
            }
        }
    }

    fun formattedTimeRange(): String {
        return String.format(Locale.getDefault(), "%02d:%02d - %02d:%02d", startHour, startMinute, endHour, endMinute)
    }

    fun formattedDays(): String {
        val daysMap = mapOf(
            Calendar.MONDAY to "L",
            Calendar.TUESDAY to "M",
            Calendar.WEDNESDAY to "X",
            Calendar.THURSDAY to "J",
            Calendar.FRIDAY to "V",
            Calendar.SATURDAY to "S",
            Calendar.SUNDAY to "D"
        )
        if (daysOfWeek.size == 7) return "Todos los días"
        if (daysOfWeek == setOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY)) return "Lun a Vie"
        if (daysOfWeek == setOf(Calendar.SATURDAY, Calendar.SUNDAY)) return "Fines de semana"
        return daysOfWeek.sorted().mapNotNull { daysMap[it] }.joinToString(" ")
    }
}
