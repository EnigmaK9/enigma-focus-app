package com.example.enigmafocus.manager

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.enigmafocus.data.AppPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FocusSessionManagerInstrumentedTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        AppPreferences.init(context)
    }

    @Test
    fun testStartAndStopSession() {
        FocusSessionManager.stopSession(context)
        assertFalse(AppPreferences.isFocusActive())

        FocusSessionManager.startSession(context, 45)
        assertTrue(AppPreferences.isFocusActive())
        assertEquals(45, AppPreferences.getFocusDurationMinutes())
        assertTrue(AppPreferences.getFocusEndTimestamp() > System.currentTimeMillis())

        FocusSessionManager.stopSession(context)
        assertFalse(AppPreferences.isFocusActive())
    }

    @Test
    fun testToggleSession() {
        FocusSessionManager.stopSession(context)
        assertFalse(AppPreferences.isFocusActive())

        FocusSessionManager.toggleSession(context, 15)
        assertTrue(AppPreferences.isFocusActive())
        assertEquals(15, AppPreferences.getFocusDurationMinutes())

        FocusSessionManager.toggleSession(context, 15)
        assertFalse(AppPreferences.isFocusActive())
    }
}
