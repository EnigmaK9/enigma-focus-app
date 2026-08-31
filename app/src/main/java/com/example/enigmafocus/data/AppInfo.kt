package com.example.enigmafocus.data

import android.graphics.drawable.Drawable

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable? = null,
    val isBlocked: Boolean = false,
    val isPopularDistraction: Boolean = false
)
