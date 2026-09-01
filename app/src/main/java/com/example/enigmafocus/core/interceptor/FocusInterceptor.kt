package com.example.enigmafocus.core.interceptor

import android.content.Context
import android.view.accessibility.AccessibilityNodeInfo
import com.example.enigmafocus.core.scheduler.ScheduleEvaluator
import com.example.enigmafocus.data.AppPreferences
import java.util.Calendar

object FocusInterceptor {

    val BROWSER_PACKAGES = setOf(
        "com.android.chrome",
        "com.brave.browser",
        "com.microsoft.emmx",
        "org.mozilla.firefox",
        "com.opera.browser",
        "com.sec.android.app.sbrowser",
        "com.duckduckgo.mobile.android"
    )

    val BLOCKED_WEB_DOMAINS = listOf(
        "instagram.com",
        "reddit.com",
        "tiktok.com",
        "twitter.com",
        "x.com",
        "youtube.com",
        "facebook.com",
        "twitch.tv",
        "web.whatsapp.com",
        "whatsapp.com"
    )

    fun isEmergencyPackage(ownPackageName: String, packageName: String): Boolean {
        return packageName == ownPackageName ||
               packageName == "com.android.phone" ||
               packageName == "com.google.android.dialer" ||
               packageName == "com.android.dialer" ||
               packageName == "com.samsung.android.dialer" ||
               packageName == "com.android.server.telecom" ||
               packageName == "com.android.incallui" ||
               packageName.contains("dialer") ||
               packageName.contains("telecom") ||
               packageName.contains("incallui") ||
               packageName.contains("deskclock") ||
               packageName.contains("clockpackage")
    }

    fun isLauncherPackage(packageName: String): Boolean {
        return packageName.contains("launcher") ||
               packageName == "com.miui.home" ||
               packageName == "com.sec.android.app.launcher" ||
               packageName == "com.huawei.android.launcher" ||
               packageName == "com.oppo.launcher" ||
               packageName == "com.vivo.launcher"
    }

    fun shouldBlockPackage(ownPackageName: String, packageName: String, calendar: Calendar = Calendar.getInstance()): Boolean {
        if (packageName.isBlank() ||
            packageName == ownPackageName ||
            packageName == "android" ||
            packageName == "com.android.systemui" ||
            packageName.contains("systemui") ||
            packageName.contains("inputmethod") ||
            isEmergencyPackage(ownPackageName, packageName) ||
            isLauncherPackage(packageName)
        ) {
            return false
        }

        val isFocusActive = ScheduleEvaluator.isFocusActive(calendar)
        val isAlwaysBlock = AppPreferences.isAlwaysBlockEnabled()

        if (!isFocusActive && !isAlwaysBlock) {
            return false
        }

        if (AppPreferences.isTemporarilyWhitelisted(packageName)) {
            return false
        }

        return AppPreferences.isAppBlocked(packageName)
    }

    fun findBlockedDomainInNode(node: AccessibilityNodeInfo?, depth: Int = 0): String? {
        if (node == null || depth > 6) return null
        val text = node.text?.toString()
        if (text != null && (text.contains(".") || text.startsWith("http"))) {
            for (domain in BLOCKED_WEB_DOMAINS) {
                if (text.contains(domain, ignoreCase = true)) {
                    return domain
                }
            }
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            val found = findBlockedDomainInNode(child, depth + 1)
            if (found != null) return found
        }
        return null
    }
}
