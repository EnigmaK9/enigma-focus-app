package com.example.enigmafocus.data

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object FocusEventLogger {

    private const val TAG = "FocusEventLogger"
    private const val LOG_FILE_NAME = "focus_events.jsonl"
    private const val MAX_LOG_LINES = 5000

    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun logEvent(
        context: Context,
        eventType: String,
        details: Map<String, Any> = emptyMap()
    ) {
        try {
            val file = File(context.filesDir, LOG_FILE_NAME)
            val now = System.currentTimeMillis()

            val json = JSONObject().apply {
                put("timestamp", now)
                put("iso", isoDateFormat.format(Date(now)))
                put("event", eventType)
                details.forEach { (k, v) -> put(k, v) }
            }

            FileWriter(file, true).use { writer ->
                writer.append(json.toString()).append("\n")
            }

            // Simple line-count rotation check if file exceeds MAX_LOG_LINES
            if (file.length() > 500_000) { // ~500 KB
                rotateLogIfNeeded(file)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error writing event to log", e)
        }
    }

    private fun rotateLogIfNeeded(file: File) {
        try {
            val lines = file.readLines()
            if (lines.size > MAX_LOG_LINES) {
                val preserved = lines.takeLast(MAX_LOG_LINES / 2)
                file.writeText(preserved.joinToString("\n") + "\n")
                Log.i(TAG, "Rotated focus_events.jsonl to last ${preserved.size} lines")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error rotating log", e)
        }
    }

    fun getRecentEvents(context: Context, limit: Int = 100): List<JSONObject> {
        return try {
            val file = File(context.filesDir, LOG_FILE_NAME)
            if (!file.exists()) return emptyList()
            file.readLines()
                .takeLast(limit)
                .mapNotNull { line ->
                    try { JSONObject(line) } catch (e: Exception) { null }
                }
                .reversed()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearLogs(context: Context): Boolean {
        return try {
            val file = File(context.filesDir, LOG_FILE_NAME)
            if (file.exists()) file.delete() else true
        } catch (e: Exception) {
            false
        }
    }

    fun getLogContent(context: Context): String {
        return try {
            val file = File(context.filesDir, LOG_FILE_NAME)
            if (file.exists()) file.readText() else ""
        } catch (e: Exception) {
            ""
        }
    }
}
