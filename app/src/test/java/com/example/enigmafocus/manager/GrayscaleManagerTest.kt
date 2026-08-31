package com.example.enigmafocus.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GrayscaleManagerTest {

    @Test
    fun defaultPopularPackages_containsMajorDistractions() {
        val popular = com.example.enigmafocus.data.AppPreferences.DEFAULT_POPULAR_PACKAGES
        assertTrue(popular.contains("com.instagram.android"))
        assertTrue(popular.contains("com.reddit.frontpage"))
        assertTrue(popular.contains("com.zhiliaoapp.musically"))
        assertTrue(popular.contains("com.twitter.android"))
        assertTrue(popular.contains("com.google.android.youtube"))
    }
}
