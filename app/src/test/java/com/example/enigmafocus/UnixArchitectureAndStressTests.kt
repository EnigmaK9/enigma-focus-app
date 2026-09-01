package com.example.enigmafocus

import com.example.enigmafocus.core.interceptor.FocusInterceptor
import com.example.enigmafocus.core.scheduler.ScheduleEvaluator
import com.example.enigmafocus.data.AppPreferences
import com.example.enigmafocus.data.DeclarativeConfigManager
import com.example.enigmafocus.data.FocusInterval
import com.example.enigmafocus.data.JsonConfigManager
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class UnixArchitectureAndStressTests {

    @Test
    fun testScheduleEvaluator_comprehensiveOvernightTimeGrid() {
        val sleepInterval = FocusInterval(
            label = "Bedtime Strict",
            startHour = 22,
            startMinute = 30,
            endHour = 6,
            endMinute = 30,
            daysOfWeek = setOf(
                Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
                Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
            ),
            isEnabled = true
        )

        assertTrue(ScheduleEvaluator.isSleepInterval(sleepInterval))

        // Check 24 hours in 15 minute increments
        val activeMinutes = mutableListOf<String>()
        for (h in 0..23) {
            for (m in listOf(0, 15, 30, 45)) {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, h)
                    set(Calendar.MINUTE, m)
                }
                if (sleepInterval.isCurrentlyActive(cal)) {
                    activeMinutes.add(String.format("%02d:%02d", h, m))
                }
            }
        }

        // Must be active from 22:30 -> 23:45, and 00:00 -> 06:15
        assertTrue(activeMinutes.contains("22:30"))
        assertTrue(activeMinutes.contains("23:45"))
        assertTrue(activeMinutes.contains("00:00"))
        assertTrue(activeMinutes.contains("03:30"))
        assertTrue(activeMinutes.contains("06:15"))
        assertFalse(activeMinutes.contains("06:30"))
        assertFalse(activeMinutes.contains("12:00"))
        assertFalse(activeMinutes.contains("22:15"))
    }

    @Test
    fun testScheduleEvaluator_daytimeNonSleepIntervals() {
        val morningWorkout = FocusInterval(
            label = "Morning Gym",
            startHour = 6,
            startMinute = 0,
            endHour = 7,
            endMinute = 30,
            isEnabled = true
        )
        assertFalse("Daytime morning interval must NEVER be marked as sleep interval", ScheduleEvaluator.isSleepInterval(morningWorkout))

        val afternoonStudy = FocusInterval(
            label = "Deep Study",
            startHour = 14,
            startMinute = 0,
            endHour = 18,
            endMinute = 0,
            isEnabled = true
        )
        assertFalse("Afternoon study must not be a sleep interval", ScheduleEvaluator.isSleepInterval(afternoonStudy))
    }

    @Test
    fun testFocusInterceptor_emergencyWhitelistsNeverBlocked() {
        val emergencyPkgs = listOf(
            "com.android.phone",
            "com.google.android.dialer",
            "com.android.dialer",
            "com.samsung.android.dialer",
            "com.android.server.telecom",
            "com.android.incallui",
            "com.google.android.deskclock",
            "com.sec.android.app.clockpackage",
            "com.android.deskclock"
        )

        for (pkg in emergencyPkgs) {
            assertTrue("Expected emergency package $pkg to be recognized", FocusInterceptor.isEmergencyPackage("com.example.enigmafocus", pkg))
            assertFalse("Emergency package $pkg must never be blocked", FocusInterceptor.shouldBlockPackage("com.example.enigmafocus", pkg))
        }
    }

    @Test
    fun testFocusInterceptor_launchersNeverBlocked() {
        val launcherPkgs = listOf(
            "com.miui.home",
            "com.sec.android.app.launcher",
            "com.google.android.apps.nexuslauncher",
            "com.huawei.android.launcher",
            "com.oppo.launcher",
            "com.vivo.launcher"
        )

        for (pkg in launcherPkgs) {
            assertTrue("Expected launcher $pkg to be recognized", FocusInterceptor.isLauncherPackage(pkg))
            assertFalse("Launcher $pkg must never be blocked", FocusInterceptor.shouldBlockPackage("com.example.enigmafocus", pkg))
        }
    }

    @Test
    fun testJsonConfigManager_corruptedAndEdgeCaseJsonHandling() {
        // Valid JSON import
        val validJson = """
            {
              "always_block": true,
              "strict_mode": true,
              "blocked_packages": ["com.instagram.android", "com.zhiliaoapp.musically"],
              "scheduled_intervals": [
                {
                  "id": "11111111-2222-3333-4444-555555555555",
                  "label": "Focus Work",
                  "start_hour": 9,
                  "start_minute": 0,
                  "end_hour": 17,
                  "end_minute": 0,
                  "enabled": true,
                  "days_of_week": [2, 3, 4, 5, 6]
                }
              ]
            }
        """.trimIndent()

        assertTrue(JsonConfigManager.importConfigJson(validJson))
        assertTrue(AppPreferences.isAlwaysBlockEnabled())
        assertTrue(AppPreferences.isStrictModeEnabled())
        assertTrue(AppPreferences.isAppBlocked("com.instagram.android"))

        // Corrupted JSON (Missing closing braces)
        assertFalse(JsonConfigManager.importConfigJson("{ \"always_block\": true, "))

        // Empty string
        assertFalse(JsonConfigManager.importConfigJson(""))

        // Random garbage text
        assertFalse(JsonConfigManager.importConfigJson("<<<not a json>>>"))

        // Malformed types
        val malformedTypesJson = """{ "blocked_packages": "not an array" }"""
        assertFalse(JsonConfigManager.importConfigJson(malformedTypesJson))
    }

    @Test
    fun testDeclarativeConfig_roundtripExportAndImport() {
        AppPreferences.setAlwaysBlockEnabled(true)
        AppPreferences.setStrictModeEnabled(true)
        AppPreferences.setBlockedPackages(setOf("com.tiktok.app", "com.facebook.katana"))

        val exported = JsonConfigManager.exportConfigJson()
        assertNotNull(exported)
        val json = JSONObject(exported)
        assertEquals(true, json.getBoolean("always_block"))
        assertEquals(true, json.getBoolean("strict_mode"))

        // Re-import exported json
        assertTrue(JsonConfigManager.importConfigJson(exported))
    }

    @Test
    fun testConcurrency_stressIntervalEvaluation() {
        val executor = Executors.newFixedThreadPool(16)
        val latch = CountDownLatch(1000)
        val successCount = AtomicInteger(0)

        val interval = FocusInterval(
            label = "Work Session",
            startHour = 8,
            startMinute = 0,
            endHour = 18,
            endMinute = 0,
            isEnabled = true
        )

        for (i in 0 until 1000) {
            executor.submit {
                try {
                    val cal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, (i % 24))
                        set(Calendar.MINUTE, (i % 60))
                    }
                    val isActive = interval.isCurrentlyActive(cal)
                    val daysStr = interval.formattedDays(isEnglish = (i % 2 == 0))
                    assertNotNull(daysStr)
                    successCount.incrementAndGet()
                } finally {
                    latch.countDown()
                }
            }
        }

        val completed = latch.await(5, TimeUnit.SECONDS)
        executor.shutdown()

        assertTrue(completed)
        assertEquals(1000, successCount.get())
    }

    @Test
    fun testFocusInterval_customDelimiterSanitization() {
        val intervalWithSemicolons = FocusInterval(
            label = "My Custom ;;; Tricky ;;; Label",
            startHour = 10,
            startMinute = 0,
            endHour = 12,
            endMinute = 0,
            isEnabled = true
        )

        AppPreferences.saveIntervals(listOf(intervalWithSemicolons))
        val loaded = AppPreferences.getIntervals()
        assertEquals(1, loaded.size)
        assertEquals(10, loaded[0].startHour)
        assertEquals(12, loaded[0].endHour)
        assertFalse(loaded[0].label.contains(";;;"))
    }
}
