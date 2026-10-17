package com.example.enigmafocus.core.interceptor

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log

object LauncherDetector {

    private const val TAG = "LauncherDetector"
    private const val CACHE_TTL_MS = 60_000L

    @Volatile
    private var cachedLauncherPackages: Set<String> = emptySet()

    @Volatile
    private var lastCacheUpdateMs: Long = 0L

    private val KNOWN_STATIC_LAUNCHERS = setOf(
        "com.miui.home",
        "com.sec.android.app.launcher",
        "com.google.android.apps.nexuslauncher",
        "com.huawei.android.launcher",
        "com.oppo.launcher",
        "com.vivo.launcher",
        "com.android.launcher",
        "com.android.launcher3",
        "ginlemon.flowerfree",
        "ginlemon.flowerpro",
        "bitpit.launcher",
        "ch.deletescape.lawnchair",
        "ch.deletescape.lawnchair.plah",
        "com.teslacoilsw.launcher",
        "com.microsoft.launcher",
        "net.oneplus.launcher",
        "com.motorola.launcher3",
        "com.transsion.XOSLauncher",
        "com.transsion.hilauncher"
    )

    fun isStaticLauncherPackage(packageName: String): Boolean {
        if (packageName.isBlank()) return false
        return packageName.contains("launcher", ignoreCase = true) ||
               KNOWN_STATIC_LAUNCHERS.contains(packageName) ||
               cachedLauncherPackages.contains(packageName)
    }

    fun isHomeLauncher(context: Context?, packageName: String): Boolean {
        if (packageName.isBlank()) return false
        if (isStaticLauncherPackage(packageName)) return true
        if (context == null) return false

        val now = System.currentTimeMillis()
        if (cachedLauncherPackages.isEmpty() || (now - lastCacheUpdateMs) > CACHE_TTL_MS) {
            refreshHomeLaunchers(context)
            lastCacheUpdateMs = now
        }

        return cachedLauncherPackages.contains(packageName)
    }

    fun refreshHomeLaunchers(context: Context) {
        try {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }
            val resolveList = context.packageManager.queryIntentActivities(
                homeIntent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
            val packages = resolveList
                .mapNotNull { it.activityInfo?.packageName }
                .filter { it.isNotBlank() && it != "android" }
                .toSet()

            if (packages.isNotEmpty()) {
                cachedLauncherPackages = packages
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying dynamic home launcher packages", e)
        }
    }
}
