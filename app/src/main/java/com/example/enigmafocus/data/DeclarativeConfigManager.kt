package com.example.enigmafocus.data

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Calendar
import java.util.UUID

object DeclarativeConfigManager {

    private const val TAG = "DeclarativeConfig"
    private const val CONFIG_FILE_NAME = "enigma_config.json"

    enum class Template {
        DEFAULT,
        WORKDAY_ONLY,
        BEDTIME_STRICT,
        DIGITAL_DETOX
    }

    fun getConfigFile(context: Context): File {
        return File(context.filesDir, CONFIG_FILE_NAME)
    }

    fun syncFromPreferences(context: Context): String {
        val jsonString = JsonConfigManager.exportConfigJson()
        try {
            getConfigFile(context).writeText(jsonString)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving declarative config", e)
        }
        return jsonString
    }

    fun loadIntoPreferences(context: Context): Boolean {
        val file = getConfigFile(context)
        if (!file.exists()) {
            syncFromPreferences(context)
            return true
        }

        return try {
            val content = file.readText()
            JsonConfigManager.importConfigJson(content)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading declarative config into preferences", e)
            false
        }
    }

    fun applyTemplate(context: Context, template: Template): Boolean {
        return try {
            when (template) {
                Template.DEFAULT -> {
                    AppPreferences.setAlwaysBlockEnabled(false)
                    AppPreferences.setStrictModeEnabled(false)
                    AppPreferences.setBlockedPackages(AppPreferences.DEFAULT_POPULAR_PACKAGES)
                    AppPreferences.saveIntervals(listOf(
                        FocusInterval(
                            label = "Workday Shift",
                            startHour = 7,
                            startMinute = 30,
                            endHour = 16,
                            endMinute = 30,
                            daysOfWeek = setOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY),
                            isEnabled = true
                        ),
                        FocusInterval(
                            label = "Rest / Sleep",
                            startHour = 22,
                            startMinute = 30,
                            endHour = 6,
                            endMinute = 30,
                            isEnabled = true
                        )
                    ))
                }
                Template.WORKDAY_ONLY -> {
                    AppPreferences.setAlwaysBlockEnabled(false)
                    AppPreferences.setStrictModeEnabled(false)
                    AppPreferences.saveIntervals(listOf(
                        FocusInterval(
                            label = "Deep Work",
                            startHour = 8,
                            startMinute = 0,
                            endHour = 17,
                            endMinute = 0,
                            daysOfWeek = setOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY),
                            isEnabled = true
                        )
                    ))
                }
                Template.BEDTIME_STRICT -> {
                    AppPreferences.setAlwaysBlockEnabled(false)
                    AppPreferences.setStrictModeEnabled(true)
                    AppPreferences.saveIntervals(listOf(
                        FocusInterval(
                            label = "Sleep Hours",
                            startHour = 22,
                            startMinute = 0,
                            endHour = 7,
                            endMinute = 0,
                            isEnabled = true
                        )
                    ))
                }
                Template.DIGITAL_DETOX -> {
                    AppPreferences.setAlwaysBlockEnabled(true)
                    AppPreferences.setStrictModeEnabled(true)
                    AppPreferences.setAutoGrayscaleEnabled(context, true)
                }
            }
            syncFromPreferences(context)
            FocusEventLogger.logEvent(context, "template_applied", mapOf("template" to template.name))
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error applying template", e)
            false
        }
    }
}
