package com.example.enigmafocus.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InstalledAppsRepositoryTest {

    private lateinit var context: Context
    private lateinit var repository: InstalledAppsRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        AppPreferences.init(context)
        repository = InstalledAppsRepository(context)
    }

    @Test
    fun testGetInstalledApps_returnsNonEmptyList() = runTest {
        val apps = repository.getInstalledApps()
        assertNotNull(apps)
        assertTrue(apps.isNotEmpty())

        // Ensure our own app is excluded from blocking list
        val ownApp = apps.find { it.packageName == context.packageName }
        assertFalse(ownApp != null)
    }

    @Test
    fun testInstalledApps_containPackageNames() = runTest {
        val apps = repository.getInstalledApps()
        apps.forEach { app ->
            assertTrue(app.name.isNotBlank())
            assertTrue(app.packageName.isNotBlank())
        }
    }
}
