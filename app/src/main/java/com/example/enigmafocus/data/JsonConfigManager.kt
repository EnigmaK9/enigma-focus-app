package com.example.enigmafocus.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object JsonConfigManager {

    fun exportConfigJson(): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("always_block", AppPreferences.isAlwaysBlockEnabled())
        root.put("auto_grayscale", AppPreferences.isAutoGrayscaleEnabled())
        root.put("strict_mode", AppPreferences.isStrictModeEnabled())
        root.put("selected_duration_minutes", AppPreferences.getFocusDurationMinutes())

        // Blocked Packages
        val pkgsArray = JSONArray()
        AppPreferences.getBlockedPackages().forEach { pkgsArray.put(it) }
        root.put("blocked_packages", pkgsArray)

        // Scheduled Intervals
        val intervalsArray = JSONArray()
        AppPreferences.getIntervals().forEach { interval ->
            val obj = JSONObject()
            obj.put("id", interval.id)
            obj.put("label", interval.label)
            obj.put("start_hour", interval.startHour)
            obj.put("start_minute", interval.startMinute)
            obj.put("end_hour", interval.endHour)
            obj.put("end_minute", interval.endMinute)
            obj.put("enabled", interval.isEnabled)

            val daysArray = JSONArray()
            interval.daysOfWeek.forEach { daysArray.put(it) }
            obj.put("days_of_week", daysArray)

            intervalsArray.put(obj)
        }
        root.put("scheduled_intervals", intervalsArray)

        return root.toString(2)
    }

    fun importConfigJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)

            if (root.has("always_block")) {
                AppPreferences.setAlwaysBlockEnabled(root.getBoolean("always_block"))
            }
            if (root.has("strict_mode")) {
                AppPreferences.setStrictModeEnabled(root.getBoolean("strict_mode"))
            }

            if (root.has("blocked_packages")) {
                val array = root.getJSONArray("blocked_packages")
                val set = mutableSetOf<String>()
                for (i in 0 until array.length()) {
                    set.add(array.getString(i))
                }
                AppPreferences.setBlockedPackages(set)
            }

            if (root.has("scheduled_intervals")) {
                val array = root.getJSONArray("scheduled_intervals")
                val list = mutableListOf<FocusInterval>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val id = obj.optString("id", java.util.UUID.randomUUID().toString())
                    val label = obj.getString("label")
                    val startH = obj.getInt("start_hour")
                    val startM = obj.getInt("start_minute")
                    val endH = obj.getInt("end_hour")
                    val endM = obj.getInt("end_minute")
                    val enabled = obj.optBoolean("enabled", true)

                    val daysSet = mutableSetOf<Int>()
                    if (obj.has("days_of_week")) {
                        val daysArr = obj.getJSONArray("days_of_week")
                        for (d in 0 until daysArr.length()) {
                            daysSet.add(daysArr.getInt(d))
                        }
                    }

                    list.add(FocusInterval(id, label, startH, startM, endH, endM, daysSet, enabled))
                }
                if (list.isNotEmpty()) {
                    AppPreferences.saveIntervals(list)
                }
            }

            true
        } catch (e: Exception) {
            false
        }
    }
}
