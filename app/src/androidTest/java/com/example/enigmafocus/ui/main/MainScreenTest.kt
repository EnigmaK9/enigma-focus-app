package com.example.enigmafocus.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.example.enigmafocus.data.AppPreferences
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setup() {
        AppPreferences.init(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun defaultBlockedPackages_contains_popular_apps() {
        val blocked = AppPreferences.getBlockedPackages()
        assert(blocked.contains("com.instagram.android"))
        assert(blocked.contains("com.reddit.frontpage"))
    }
}
