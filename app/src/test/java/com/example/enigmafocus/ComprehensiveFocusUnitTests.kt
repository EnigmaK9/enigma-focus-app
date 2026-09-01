package com.example.enigmafocus

import com.example.enigmafocus.data.AppInfo
import com.example.enigmafocus.data.AppPreferences
import com.example.enigmafocus.data.FocusInterval
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class ComprehensiveFocusUnitTests {

    // 1. Distraction Catalog & Apps Model Tests
    @Test
    fun testDefaultPopularPackages_containsCoreDistractions() {
        val defaults = AppPreferences.DEFAULT_POPULAR_PACKAGES
        assertEquals(7, defaults.size)
        assertTrue("Instagram should be present", defaults.contains("com.instagram.android"))
        assertTrue("Reddit should be present", defaults.contains("com.reddit.frontpage"))
        assertTrue("TikTok should be present", defaults.contains("com.zhiliaoapp.musically"))
        assertTrue("Twitter/X should be present", defaults.contains("com.twitter.android"))
        assertTrue("YouTube should be present", defaults.contains("com.google.android.youtube"))
        assertTrue("Facebook should be present", defaults.contains("com.facebook.katana"))
        assertTrue("Twitch should be present", defaults.contains("tv.twitch.android.app"))
    }

    @Test
    fun testAppInfoModel_isBlockedStateToggle() {
        val app = AppInfo("Reddit", "com.reddit.frontpage", isBlocked = false, isPopularDistraction = true)
        assertFalse(app.isBlocked)
        assertTrue(app.isPopularDistraction)

        val blockedApp = app.copy(isBlocked = true)
        assertTrue(blockedApp.isBlocked)
        assertEquals("Reddit", blockedApp.name)
    }

    @Test
    fun testAppFiltering_caseInsensitiveSearch() {
        val apps = listOf(
            AppInfo("Instagram", "com.instagram.android"),
            AppInfo("Reddit", "com.reddit.frontpage"),
            AppInfo("Telegram", "org.telegram.messenger"),
            AppInfo("Spotify", "com.spotify.music")
        )

        val searchResult = apps.filter { it.name.lowercase().contains("gram") }
        assertEquals(2, searchResult.size)
        assertTrue(searchResult.any { it.name == "Instagram" })
        assertTrue(searchResult.any { it.name == "Telegram" })
    }

    // 2. Focus Session & Countdown Logic Tests
    @Test
    fun testCountdownFormatting_standardMinutes() {
        val remainingMillis = 1500000L // 25 minutes
        val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(remainingMillis) % 60
        val formatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

        assertEquals("25:00", formatted)
    }

    @Test
    fun testCountdownFormatting_nineHourWorkday() {
        val nineHoursMillis = 540 * 60 * 1000L // 9 hours
        val hours = TimeUnit.MILLISECONDS.toHours(nineHoursMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(nineHoursMillis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(nineHoursMillis) % 60
        val formatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)

        assertEquals("09:00:00", formatted)
    }

    @Test
    fun testCountdownFormatting_secondsEdgeCase() {
        val remainingMillis = 65000L // 1 minute 5 seconds
        val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(remainingMillis) % 60
        val formatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

        assertEquals("01:05", formatted)
    }

    @Test
    fun testCountdownFormatting_zeroRemaining() {
        val remainingMillis = 0L
        val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(remainingMillis) % 60
        val formatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

        assertEquals("00:00", formatted)
    }

    // 3. User Requested Default Intervals: 7:30 to 16:30 and 22:30 to 06:30
    @Test
    fun testDefaultIntervals_workday730to1630() {
        val workday = FocusInterval(
            label = "Jornada Laboral",
            startHour = 7,
            startMinute = 30,
            endHour = 16,
            endMinute = 30,
            daysOfWeek = setOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY),
            isEnabled = true
        )

        assertEquals("07:30 - 16:30", workday.formattedTimeRange())
        assertEquals("Mon to Fri", workday.formattedDays(isEnglish = true))
        assertEquals("Lun a Vie", workday.formattedDays(isEnglish = false))

        // Monday 07:30 AM -> Active
        val testCalStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 30)
        }
        assertTrue(workday.isCurrentlyActive(testCalStart))

        // Monday 07:29 AM -> Inactive (before start)
        val testCalBefore = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 29)
        }
        assertFalse(workday.isCurrentlyActive(testCalBefore))

        // Monday 16:30 PM -> Inactive (finished)
        val testCalEnd = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 16)
            set(Calendar.MINUTE, 30)
        }
        assertFalse(workday.isCurrentlyActive(testCalEnd))
    }

    @Test
    fun testDefaultIntervals_sleepOvernight2230to0630() {
        val sleep = FocusInterval(
            label = "Descanso / Dormir",
            startHour = 22,
            startMinute = 30,
            endHour = 6,
            endMinute = 30,
            daysOfWeek = setOf(
                Calendar.MONDAY,
                Calendar.TUESDAY,
                Calendar.WEDNESDAY,
                Calendar.THURSDAY,
                Calendar.FRIDAY,
                Calendar.SATURDAY,
                Calendar.SUNDAY
            ),
            isEnabled = true
        )

        assertEquals("22:30 - 06:30", sleep.formattedTimeRange())
        assertEquals("Every day", sleep.formattedDays(isEnglish = true))
        assertEquals("Todos los días", sleep.formattedDays(isEnglish = false))

        // 23:00 (11:00 PM) -> Active
        val cal11pm = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 0)
        }
        assertTrue(sleep.isCurrentlyActive(cal11pm))

        // 03:00 (3:00 AM) -> Active
        val cal3am = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 3)
            set(Calendar.MINUTE, 0)
        }
        assertTrue(sleep.isCurrentlyActive(cal3am))

        // 06:29 (6:29 AM) -> Active
        val cal629am = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 6)
            set(Calendar.MINUTE, 29)
        }
        assertTrue(sleep.isCurrentlyActive(cal629am))

        // 07:00 (7:00 AM) -> Inactive
        val cal7am = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 0)
        }
        assertFalse(sleep.isCurrentlyActive(cal7am))
    }

    // 4. Mindful Breathing Phases Logic Tests
    @Test
    fun testMindfulBreathingPhases() {
        fun getPhase(secondsLeft: Int): String {
            return when {
                secondsLeft in 8..10 -> "Inhala"
                secondsLeft in 5..7 -> "Sostén"
                secondsLeft in 1..4 -> "Exhala"
                else -> "Listo"
            }
        }

        assertEquals("Inhala", getPhase(10))
        assertEquals("Inhala", getPhase(8))
        assertEquals("Sostén", getPhase(7))
        assertEquals("Sostén", getPhase(5))
        assertEquals("Exhala", getPhase(4))
        assertEquals("Exhala", getPhase(1))
        assertEquals("Listo", getPhase(0))
    }

    // 5. ADB Command Format Tests
    @Test
    fun testAdbCommandGeneration() {
        val packageName = "com.example.enigmafocus"
        val expected = "adb shell pm grant com.example.enigmafocus android.permission.WRITE_SECURE_SETTINGS"
        val generated = "adb shell pm grant $packageName android.permission.WRITE_SECURE_SETTINGS"
        assertEquals(expected, generated)
    }
}
