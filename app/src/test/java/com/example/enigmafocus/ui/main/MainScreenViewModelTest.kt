package com.example.enigmafocus.ui.main

import com.example.enigmafocus.data.AppInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MainScreenViewModelLogicTest {

    @Test
    fun uiState_defaultState_hasCorrectInitialValues() {
        val state = MainUiState()
        assertFalse(state.isFocusActive)
        assertEquals(0L, state.remainingMillis)
        assertEquals(25, state.selectedDurationMinutes)
        assertFalse(state.isGrayscaleActive)
        assertTrue(state.isAutoGrayscaleEnabled)
        assertTrue(state.isAlwaysBlockEnabled)
        assertFalse(state.isStrictModeEnabled)
        assertTrue(state.scheduledIntervals.isEmpty())
        assertTrue(state.installedApps.isEmpty())
        assertTrue(state.filteredApps.isEmpty())
        assertEquals("", state.searchQuery)
    }

    @Test
    fun filterApps_matchesByNameAndPackage() {
        val apps = listOf(
            AppInfo("Instagram", "com.instagram.android"),
            AppInfo("Reddit", "com.reddit.frontpage"),
            AppInfo("Calculator", "com.android.calculator2")
        )

        val query = "gram"
        val filtered = apps.filter { it.name.lowercase().contains(query) || it.packageName.lowercase().contains(query) }

        assertEquals(1, filtered.size)
        assertEquals("Instagram", filtered.first().name)
    }

    @Test
    fun filterApps_emptyQuery_returnsAll() {
        val apps = listOf(
            AppInfo("Instagram", "com.instagram.android"),
            AppInfo("Reddit", "com.reddit.frontpage")
        )

        val query = ""
        val filtered = if (query.isBlank()) apps else apps.filter { it.name.lowercase().contains(query) }

        assertEquals(2, filtered.size)
    }
}
