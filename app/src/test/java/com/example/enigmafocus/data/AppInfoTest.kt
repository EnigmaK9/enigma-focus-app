package com.example.enigmafocus.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppInfoTest {

    @Test
    fun appInfo_instantiation_hasExpectedValues() {
        val app = AppInfo(
            name = "Instagram",
            packageName = "com.instagram.android",
            icon = null,
            isBlocked = true,
            isPopularDistraction = true
        )

        assertEquals("Instagram", app.name)
        assertEquals("com.instagram.android", app.packageName)
        assertNull(app.icon)
        assertTrue(app.isBlocked)
        assertTrue(app.isPopularDistraction)
    }

    @Test
    fun appInfo_defaultValues_areCorrect() {
        val app = AppInfo(
            name = "Calculator",
            packageName = "com.android.calculator2"
        )

        assertEquals("Calculator", app.name)
        assertEquals("com.android.calculator2", app.packageName)
        assertNull(app.icon)
        assertFalse(app.isBlocked)
        assertFalse(app.isPopularDistraction)
    }

    @Test
    fun appInfo_copy_updatesStateProperly() {
        val original = AppInfo(
            name = "Reddit",
            packageName = "com.reddit.frontpage",
            isBlocked = false
        )

        val updated = original.copy(isBlocked = true)

        assertFalse(original.isBlocked)
        assertTrue(updated.isBlocked)
        assertEquals(original.packageName, updated.packageName)
    }
}
