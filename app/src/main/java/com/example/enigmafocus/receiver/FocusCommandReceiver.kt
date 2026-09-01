package com.example.enigmafocus.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.enigmafocus.core.scheduler.ScheduleEvaluator
import com.example.enigmafocus.data.AppPreferences
import com.example.enigmafocus.data.DeclarativeConfigManager
import com.example.enigmafocus.data.FocusEventLogger
import com.example.enigmafocus.data.JsonConfigManager
import com.example.enigmafocus.manager.FocusSessionManager
import com.example.enigmafocus.manager.GrayscaleManager
import org.json.JSONObject

class FocusCommandReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "FocusCommandReceiver"

        const val ACTION_START_SESSION = "com.example.enigmafocus.action.START_SESSION"
        const val ACTION_STOP_SESSION = "com.example.enigmafocus.action.STOP_SESSION"
        const val ACTION_TOGGLE_GRAYSCALE = "com.example.enigmafocus.action.TOGGLE_GRAYSCALE"
        const val ACTION_SET_GRAYSCALE = "com.example.enigmafocus.action.SET_GRAYSCALE"
        const val ACTION_SET_ALWAYS_BLOCK = "com.example.enigmafocus.action.SET_ALWAYS_BLOCK"
        const val ACTION_SET_STRICT_MODE = "com.example.enigmafocus.action.SET_STRICT_MODE"
        const val ACTION_BLOCK_PACKAGE = "com.example.enigmafocus.action.BLOCK_PACKAGE"
        const val ACTION_UNBLOCK_PACKAGE = "com.example.enigmafocus.action.UNBLOCK_PACKAGE"
        const val ACTION_SET_LANGUAGE = "com.example.enigmafocus.action.SET_LANGUAGE"
        const val ACTION_APPLY_TEMPLATE = "com.example.enigmafocus.action.APPLY_TEMPLATE"
        const val ACTION_RELOAD_CONFIG = "com.example.enigmafocus.action.RELOAD_CONFIG"
        const val ACTION_EXPORT_CONFIG = "com.example.enigmafocus.action.EXPORT_CONFIG"
        const val ACTION_GET_STATUS = "com.example.enigmafocus.action.GET_STATUS"
        const val ACTION_CLEAR_LOGS = "com.example.enigmafocus.action.CLEAR_LOGS"

        const val EXTRA_DURATION_MINUTES = "duration_minutes"
        const val EXTRA_ENABLED = "enabled"
        const val EXTRA_PACKAGE = "package"
        const val EXTRA_LANG = "lang"
        const val EXTRA_TEMPLATE = "template"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.i(TAG, "⚡ Received CLI broadcast intent action: $action")

        try {
            AppPreferences.init(context)

            when (action) {
                ACTION_START_SESSION -> {
                    val duration = intent.getIntExtra(EXTRA_DURATION_MINUTES, 25)
                    FocusSessionManager.startSession(context, duration)
                    FocusEventLogger.logEvent(context, "cli_start_session", mapOf("duration" to duration))
                    Log.i(TAG, "✅ Started focus session for $duration minutes via CLI")
                }
                ACTION_STOP_SESSION -> {
                    FocusSessionManager.stopSession(context)
                    FocusEventLogger.logEvent(context, "cli_stop_session")
                    Log.i(TAG, "✅ Stopped focus session via CLI")
                }
                ACTION_TOGGLE_GRAYSCALE -> {
                    GrayscaleManager.toggleGrayscale(context)
                    Log.i(TAG, "✅ Toggled grayscale via CLI")
                }
                ACTION_SET_GRAYSCALE -> {
                    val enabled = intent.getBooleanExtra(EXTRA_ENABLED, true)
                    GrayscaleManager.setGrayscaleEnabled(context, enabled)
                    Log.i(TAG, "✅ Set grayscale to $enabled via CLI")
                }
                ACTION_SET_ALWAYS_BLOCK -> {
                    val enabled = intent.getBooleanExtra(EXTRA_ENABLED, true)
                    AppPreferences.setAlwaysBlockEnabled(enabled)
                    DeclarativeConfigManager.syncFromPreferences(context)
                    Log.i(TAG, "✅ Set always block to $enabled via CLI")
                }
                ACTION_SET_STRICT_MODE -> {
                    val enabled = intent.getBooleanExtra(EXTRA_ENABLED, true)
                    AppPreferences.setStrictModeEnabled(enabled)
                    DeclarativeConfigManager.syncFromPreferences(context)
                    Log.i(TAG, "✅ Set strict mode to $enabled via CLI")
                }
                ACTION_BLOCK_PACKAGE -> {
                    val pkg = intent.getStringExtra(EXTRA_PACKAGE)
                    if (!pkg.isNullOrBlank()) {
                        val current = AppPreferences.getBlockedPackages().toMutableSet()
                        current.add(pkg)
                        AppPreferences.setBlockedPackages(current)
                        DeclarativeConfigManager.syncFromPreferences(context)
                        FocusEventLogger.logEvent(context, "cli_block_package", mapOf("package" to pkg))
                        Log.i(TAG, "✅ Blocked package: $pkg")
                    }
                }
                ACTION_UNBLOCK_PACKAGE -> {
                    val pkg = intent.getStringExtra(EXTRA_PACKAGE)
                    if (!pkg.isNullOrBlank()) {
                        val current = AppPreferences.getBlockedPackages().toMutableSet()
                        current.remove(pkg)
                        AppPreferences.setBlockedPackages(current)
                        DeclarativeConfigManager.syncFromPreferences(context)
                        FocusEventLogger.logEvent(context, "cli_unblock_package", mapOf("package" to pkg))
                        Log.i(TAG, "✅ Unblocked package: $pkg")
                    }
                }
                ACTION_SET_LANGUAGE -> {
                    val lang = intent.getStringExtra(EXTRA_LANG) ?: "system"
                    AppPreferences.setAppLanguage(lang)
                    Log.i(TAG, "✅ Set app language to $lang via CLI")
                }
                ACTION_APPLY_TEMPLATE -> {
                    val templateStr = intent.getStringExtra(EXTRA_TEMPLATE)?.uppercase() ?: "DEFAULT"
                    val template = try {
                        DeclarativeConfigManager.Template.valueOf(templateStr)
                    } catch (e: Exception) {
                        DeclarativeConfigManager.Template.DEFAULT
                    }
                    DeclarativeConfigManager.applyTemplate(context, template)
                    Log.i(TAG, "✅ Applied template $template via CLI")
                }
                ACTION_RELOAD_CONFIG -> {
                    DeclarativeConfigManager.loadIntoPreferences(context)
                    Log.i(TAG, "✅ Reloaded declarative config from disk")
                }
                ACTION_EXPORT_CONFIG -> {
                    val json = DeclarativeConfigManager.syncFromPreferences(context)
                    resultData = json
                    Log.i(TAG, "📄 Exported JSON Config: $json")
                }
                ACTION_GET_STATUS -> {
                    val status = JSONObject().apply {
                        put("is_focus_active", AppPreferences.isFocusActive())
                        put("is_always_block", AppPreferences.isAlwaysBlockEnabled())
                        put("is_strict_mode", AppPreferences.isStrictModeEnabled())
                        put("is_grayscale_active", GrayscaleManager.isGrayscaleActive(context))
                        put("is_sleep_active", ScheduleEvaluator.isSleepScheduleActive())
                        put("blocked_apps_count", AppPreferences.getBlockedPackages().size)
                        put("language", AppPreferences.getSelectedLanguagePreference())
                    }
                    resultData = status.toString()
                    Log.i(TAG, "📊 Status: $status")
                }
                ACTION_CLEAR_LOGS -> {
                    FocusEventLogger.clearLogs(context)
                    Log.i(TAG, "🗑️ Cleared JSONL event logs")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling command intent", e)
        }
    }
}
