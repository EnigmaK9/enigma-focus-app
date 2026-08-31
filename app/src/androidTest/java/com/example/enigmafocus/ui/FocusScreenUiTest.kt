package com.example.enigmafocus.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.enigmafocus.data.AppPreferences
import com.example.enigmafocus.ui.main.FocusScreen
import com.example.enigmafocus.ui.main.MainScreenViewModel
import com.example.enigmafocus.ui.main.MainUiState
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FocusScreenUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setup() {
        AppPreferences.init(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun focusScreen_displaysSessionTitleAndDurations() {
        val viewModel = MainScreenViewModel(ApplicationProvider.getApplicationContext())
        val state = MainUiState(isFocusActive = false, selectedDurationMinutes = 25)

        composeTestRule.setContent {
            FocusScreen(
                state = state,
                viewModel = viewModel,
                onNavigateToSettings = {}
            )
        }

        composeTestRule.onNodeWithText("Sesión de Concentración").assertIsDisplayed()
        composeTestRule.onNodeWithText("15 min").assertIsDisplayed()
        composeTestRule.onNodeWithText("25 min").assertIsDisplayed()
        composeTestRule.onNodeWithText("45 min").assertIsDisplayed()
        composeTestRule.onNodeWithText("60 min").assertIsDisplayed()
        composeTestRule.onNodeWithText("Iniciar Sesión (25 min)").assertIsDisplayed()
    }

    @Test
    fun focusScreen_whenActive_displaysStopButton() {
        val viewModel = MainScreenViewModel(ApplicationProvider.getApplicationContext())
        val state = MainUiState(isFocusActive = true, remainingMillis = 1500000L)

        composeTestRule.setContent {
            FocusScreen(
                state = state,
                viewModel = viewModel,
                onNavigateToSettings = {}
            )
        }

        composeTestRule.onNodeWithText("Sesión de Enfoque Activa").assertIsDisplayed()
        composeTestRule.onNodeWithText("Detener Sesión").assertIsDisplayed()
    }
}
