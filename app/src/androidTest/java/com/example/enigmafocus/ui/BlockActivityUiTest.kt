package com.example.enigmafocus.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.enigmafocus.data.AppPreferences
import com.example.enigmafocus.ui.block.DirectBreathingBlockScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BlockActivityUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setup() {
        AppPreferences.init(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun directBreathingBlockScreen_displaysHeaderAndButtons() {
        composeTestRule.setContent {
            DirectBreathingBlockScreen(
                appName = "Instagram",
                packageName = "com.instagram.android",
                onGoHome = {},
                onUnlockTemporary = {}
            )
        }

        composeTestRule.onNodeWithText("Acceso bloqueado: Instagram").assertIsDisplayed()
        composeTestRule.onNodeWithText("Toma una pausa consciente").assertIsDisplayed()
        composeTestRule.onNodeWithText("Volver a mi enfoque").assertIsDisplayed()
    }
}
