package com.example.enigmafocus.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InstalledAppsRepository(private val context: Context) {

    suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        val blockedPackages = AppPreferences.getBlockedPackages()
        val ownPackage = context.packageName

        val apps = resolveInfos
            .mapNotNull { resolveInfo ->
                val pkgName = resolveInfo.activityInfo.packageName
                if (pkgName == ownPackage) return@mapNotNull null

                val appName = resolveInfo.loadLabel(pm).toString()
                val appIcon = try {
                    resolveInfo.loadIcon(pm)
                } catch (e: Exception) {
                    null
                }

                AppInfo(
                    name = appName,
                    packageName = pkgName,
                    icon = appIcon,
                    isBlocked = blockedPackages.contains(pkgName),
                    isPopularDistraction = AppPreferences.DEFAULT_POPULAR_PACKAGES.contains(pkgName)
                )
            }
            .distinctBy { it.packageName }
            .sortedWith(compareByDescending<AppInfo> { it.isPopularDistraction }
                .thenByDescending { it.isBlocked }
                .thenBy { it.name.lowercase() })

        apps
    }
}
