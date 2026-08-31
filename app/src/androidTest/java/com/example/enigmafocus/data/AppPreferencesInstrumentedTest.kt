package com.example.enigmafocus.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppPreferencesInstrumentedTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        AppPreferences.init(context)
    }

    @Test
    fun testDefaultBlockedPackages() {
        val blocked = AppPreferences.getBlockedPackages()
        assertTrue(blocked.contains("com.instagram.android"))
        assertTrue(blocked.contains("com.reddit.frontpage"))
        assertTrue(blocked.contains("com.zhiliaoapp.musically"))
        assertTrue(blocked.contains("com.twitter.android"))
        assertTrue(blocked.contains("com.google.android.youtube"))
    }

    @Test
    fun testToggleBlockedPackage() {
        val testPkg = "com.test.distraction"
        assertFalse(AppPreferences.isAppBlocked(testPkg))

        val isNowBlocked = AppPreferences.toggleBlockedPackage(testPkg)
        assertTrue(isNowBlocked)
        assertTrue(AppPreferences.isAppBlocked(testPkg))

        val isNowUnblocked = AppPreferences.toggleBlockedPackage(testPkg)
        assertFalse(isNowUnblocked)
        assertFalse(AppPreferences.isAppBlocked(testPkg))
    }

    @Test
    fun testFocusActiveState() {
        AppPreferences.setFocusActive(false, 0L, 0)
        assertFalse(AppPreferences.isFocusActive())

        val futureTimestamp = System.currentTimeMillis() + 60000L
        AppPreferences.setFocusActive(true, futureTimestamp, 25)

        assertTrue(AppPreferences.isFocusActive())
        assertEquals(25, AppPreferences.getFocusDurationMinutes())
        assertEquals(futureTimestamp, AppPreferences.getFocusEndTimestamp())

        // Test expired timestamp
        val pastTimestamp = System.currentTimeMillis() - 1000L
        AppPreferences.setFocusActive(true, pastTimestamp, 25)
        assertFalse(AppPreferences.isFocusActive())
    }

    @Test
    fun testAutoGrayscalePreference() {
        AppPreferences.setAutoGrayscaleEnabled(context, true)
        assertTrue(AppPreferences.isAutoGrayscaleEnabled())

        AppPreferences.setAutoGrayscaleEnabled(context, false)
        assertFalse(AppPreferences.isAutoGrayscaleEnabled())
    }

    @Test
    fun testAlwaysBlockPreference() {
        AppPreferences.setAlwaysBlockEnabled(true)
        assertTrue(AppPreferences.isAlwaysBlockEnabled())

        AppPreferences.setAlwaysBlockEnabled(false)
        assertFalse(AppPreferences.isAlwaysBlockEnabled())
    }

    @Test
    fun testStrictModePreference() {
        AppPreferences.setStrictModeEnabled(true)
        assertTrue(AppPreferences.isStrictModeEnabled())

        AppPreferences.setStrictModeEnabled(false)
        assertFalse(AppPreferences.isStrictModeEnabled())
    }

    @Test
    fun testTemporaryWhitelist() {
        val pkg = "com.instagram.android"
        AppPreferences.setStrictModeEnabled(false)
        AppPreferences.clearTemporaryWhitelist()
        assertFalse(AppPreferences.isTemporarilyWhitelisted(pkg))

        AppPreferences.setTemporaryWhitelist(pkg, 1)
        assertTrue(AppPreferences.isTemporarilyWhitelisted(pkg))

        // When strict mode is enabled, whitelist is ignored
        AppPreferences.setStrictModeEnabled(true)
        assertFalse(AppPreferences.isTemporarilyWhitelisted(pkg))

        AppPreferences.setStrictModeEnabled(false)
        AppPreferences.clearTemporaryWhitelist()
        assertFalse(AppPreferences.isTemporarilyWhitelisted(pkg))
    }
}
