package com.example.enigmafocus.data

import android.content.Context
import android.content.SharedPreferences
import com.example.enigmafocus.manager.GrayscaleManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

object AppPreferences {

    private const val PREFS_NAME = "enigma_focus_prefs"
    private const val KEY_BLOCKED_PACKAGES = "blocked_packages"
    private const val KEY_FOCUS_ACTIVE = "focus_active"
    private const val KEY_FOCUS_END_TIMESTAMP = "focus_end_timestamp"
    private const val KEY_FOCUS_DURATION_MINUTES = "focus_duration_minutes"
    private const val KEY_AUTO_GRAYSCALE = "auto_grayscale_on_focus"
    private const val KEY_ALWAYS_BLOCK = "always_block_selected_apps"
    private const val KEY_STRICT_MODE = "strict_mode_no_breaks"
    private const val KEY_TEMP_WHITELIST_EXPIRY = "temp_whitelist_expiry"
    private const val KEY_TEMP_WHITELIST_PKG = "temp_whitelist_pkg"
    private const val KEY_FOCUS_INTERVALS = "focus_intervals"
    private const val KEY_APP_LANGUAGE = "app_language"
    private const val KEY_ANTI_IMPULSE = "anti_impulse_cooldown"

    // Default distraction packages
    val DEFAULT_POPULAR_PACKAGES = setOf(
        "com.instagram.android",      // Instagram
        "com.reddit.frontpage",       // Reddit
        "com.zhiliaoapp.musically",   // TikTok
        "com.twitter.android",        // X / Twitter
        "com.google.android.youtube", // YouTube
        "com.facebook.katana",        // Facebook
        "tv.twitch.android.app"       // Twitch
    )

    val DEFAULT_SCHEDULED_INTERVALS = listOf(
        FocusInterval(
            id = "default_workday_1",
            label = "Workday Shift",
            startHour = 7,
            startMinute = 30,
            endHour = 16,
            endMinute = 30,
            daysOfWeek = setOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY),
            isEnabled = true
        ),
        FocusInterval(
            id = "default_sleep_2",
            label = "Rest / Sleep",
            startHour = 22,
            startMinute = 30,
            endHour = 6,
            endMinute = 30,
            daysOfWeek = setOf(
                Calendar.MONDAY,
                Calendar.TUESDAY,
                Calendar.WEDNESDAY,
                Calendar.THURSDAY,
                Calendar.FRIDAY,
                Calendar.SATURDAY,
                Calendar.SUNDAY
            ),
            isEnabled = true
        )
    )

    private lateinit var prefs: SharedPreferences

    private val _blockedPackagesFlow = MutableStateFlow<Set<String>>(emptySet())
    val blockedPackagesFlow: StateFlow<Set<String>> = _blockedPackagesFlow.asStateFlow()

    private val _focusActiveFlow = MutableStateFlow(false)
    val focusActiveFlow: StateFlow<Boolean> = _focusActiveFlow.asStateFlow()

    private val _autoGrayscaleFlow = MutableStateFlow(true)
    val autoGrayscaleFlow: StateFlow<Boolean> = _autoGrayscaleFlow.asStateFlow()

    private val _alwaysBlockFlow = MutableStateFlow(true)
    val alwaysBlockFlow: StateFlow<Boolean> = _alwaysBlockFlow.asStateFlow()

    private val _strictModeFlow = MutableStateFlow(false)
    val strictModeFlow: StateFlow<Boolean> = _strictModeFlow.asStateFlow()

    private val _languageFlow = MutableStateFlow("en")
    val languageFlow: StateFlow<String> = _languageFlow.asStateFlow()

    private val _antiImpulseFlow = MutableStateFlow(false)
    val antiImpulseFlow: StateFlow<Boolean> = _antiImpulseFlow.asStateFlow()

    private val _intervalsFlow = MutableStateFlow<List<FocusInterval>>(emptyList())
    val intervalsFlow: StateFlow<List<FocusInterval>> = _intervalsFlow.asStateFlow()

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Initialize defaults if not set
        if (!prefs.contains(KEY_BLOCKED_PACKAGES)) {
            prefs.edit().putStringSet(KEY_BLOCKED_PACKAGES, DEFAULT_POPULAR_PACKAGES).apply()
        }
        if (!prefs.contains(KEY_ALWAYS_BLOCK)) {
            prefs.edit().putBoolean(KEY_ALWAYS_BLOCK, true).apply()
        }
        if (!prefs.contains(KEY_AUTO_GRAYSCALE)) {
            prefs.edit().putBoolean(KEY_AUTO_GRAYSCALE, true).apply()
        }
        if (!prefs.contains(KEY_APP_LANGUAGE)) {
            prefs.edit().putString(KEY_APP_LANGUAGE, "en").apply() // English by default
        }
        if (!prefs.contains(KEY_ANTI_IMPULSE)) {
            prefs.edit().putBoolean(KEY_ANTI_IMPULSE, false).apply()
        }
        if (!prefs.contains("initial_grayscale_applied")) {
            prefs.edit().putBoolean("initial_grayscale_applied", true).apply()
            GrayscaleManager.setGrayscaleEnabled(context, true)
        }

        _blockedPackagesFlow.value = prefs.getStringSet(KEY_BLOCKED_PACKAGES, DEFAULT_POPULAR_PACKAGES) ?: DEFAULT_POPULAR_PACKAGES
        _focusActiveFlow.value = prefs.getBoolean(KEY_FOCUS_ACTIVE, false)
        _autoGrayscaleFlow.value = prefs.getBoolean(KEY_AUTO_GRAYSCALE, true)
        _alwaysBlockFlow.value = prefs.getBoolean(KEY_ALWAYS_BLOCK, true)
        _strictModeFlow.value = prefs.getBoolean(KEY_STRICT_MODE, false)
        _languageFlow.value = prefs.getString(KEY_APP_LANGUAGE, "en") ?: "en"
        _antiImpulseFlow.value = prefs.getBoolean(KEY_ANTI_IMPULSE, false)

        loadIntervals()
    }

    fun getAppLanguage(): String {
        return prefs.getString(KEY_APP_LANGUAGE, "en") ?: "en"
    }

    fun isEnglish(): Boolean {
        return getAppLanguage() == "en"
    }

    fun setAppLanguage(language: String) {
        prefs.edit().putString(KEY_APP_LANGUAGE, language).apply()
        _languageFlow.value = language
    }

    fun isAntiImpulseEnabled(): Boolean {
        return prefs.getBoolean(KEY_ANTI_IMPULSE, false)
    }

    fun setAntiImpulseEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ANTI_IMPULSE, enabled).apply()
        _antiImpulseFlow.value = enabled
    }

    fun getBlockedPackages(): Set<String> {
        return prefs.getStringSet(KEY_BLOCKED_PACKAGES, DEFAULT_POPULAR_PACKAGES) ?: DEFAULT_POPULAR_PACKAGES
    }

    fun setBlockedPackages(packages: Set<String>) {
        prefs.edit().putStringSet(KEY_BLOCKED_PACKAGES, packages).apply()
        _blockedPackagesFlow.value = packages
    }

    fun toggleBlockedPackage(packageName: String): Boolean {
        val current = getBlockedPackages().toMutableSet()
        val isNowBlocked: Boolean
        if (current.contains(packageName)) {
            current.remove(packageName)
            isNowBlocked = false
        } else {
            current.add(packageName)
            isNowBlocked = true
        }
        setBlockedPackages(current)
        return isNowBlocked
    }

    fun isAppBlocked(packageName: String): Boolean {
        return getBlockedPackages().contains(packageName)
    }

    fun isFocusActive(): Boolean {
        val active = prefs.getBoolean(KEY_FOCUS_ACTIVE, false)
        if (active) {
            val end = prefs.getLong(KEY_FOCUS_END_TIMESTAMP, 0L)
            if (end > 0 && System.currentTimeMillis() >= end) {
                // Session expired
                setFocusActive(false, 0L, 0)
                return false
            }
        }
        return active
    }

    fun setFocusActive(active: Boolean, endTimestamp: Long = 0L, durationMinutes: Int = 0) {
        prefs.edit()
            .putBoolean(KEY_FOCUS_ACTIVE, active)
            .putLong(KEY_FOCUS_END_TIMESTAMP, endTimestamp)
            .putInt(KEY_FOCUS_DURATION_MINUTES, durationMinutes)
            .apply()
        _focusActiveFlow.value = active
    }

    fun getFocusEndTimestamp(): Long {
        return prefs.getLong(KEY_FOCUS_END_TIMESTAMP, 0L)
    }

    fun getFocusDurationMinutes(): Int {
        return prefs.getInt(KEY_FOCUS_DURATION_MINUTES, 25)
    }

    fun isAutoGrayscaleEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_GRAYSCALE, true)
    }

    fun setAutoGrayscaleEnabled(context: Context, enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_GRAYSCALE, enabled).apply()
        _autoGrayscaleFlow.value = enabled
        if (!enabled) {
            GrayscaleManager.setGrayscaleEnabled(context, false)
        }
    }

    fun isAlwaysBlockEnabled(): Boolean {
        return prefs.getBoolean(KEY_ALWAYS_BLOCK, true)
    }

    fun setAlwaysBlockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ALWAYS_BLOCK, enabled).apply()
        _alwaysBlockFlow.value = enabled
    }

    fun isStrictModeEnabled(): Boolean {
        return prefs.getBoolean(KEY_STRICT_MODE, false)
    }

    fun setStrictModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_STRICT_MODE, enabled).apply()
        _strictModeFlow.value = enabled
    }

    fun setTemporaryWhitelist(packageName: String, durationMinutes: Int) {
        val expiry = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
        prefs.edit()
            .putString(KEY_TEMP_WHITELIST_PKG, packageName)
            .putLong(KEY_TEMP_WHITELIST_EXPIRY, expiry)
            .apply()
    }

    fun clearTemporaryWhitelist() {
        prefs.edit()
            .remove(KEY_TEMP_WHITELIST_PKG)
            .remove(KEY_TEMP_WHITELIST_EXPIRY)
            .apply()
    }

    fun isTemporarilyWhitelisted(packageName: String): Boolean {
        if (isStrictModeEnabled()) return false
        val whitelistedPkg = prefs.getString(KEY_TEMP_WHITELIST_PKG, null) ?: return false
        if (whitelistedPkg != packageName) return false
        val expiry = prefs.getLong(KEY_TEMP_WHITELIST_EXPIRY, 0L)
        val valid = System.currentTimeMillis() < expiry
        if (!valid) {
            clearTemporaryWhitelist()
        }
        return valid
    }

    // --- Scheduled Intervals Logic ---

    fun getIntervals(): List<FocusInterval> {
        return _intervalsFlow.value
    }

    private fun loadIntervals() {
        val set = prefs.getStringSet(KEY_FOCUS_INTERVALS, null)
        if (set == null) {
            saveIntervals(DEFAULT_SCHEDULED_INTERVALS)
            _intervalsFlow.value = DEFAULT_SCHEDULED_INTERVALS
        } else {
            val list = set.mapNotNull { deserializeInterval(it) }
            _intervalsFlow.value = list.ifEmpty { DEFAULT_SCHEDULED_INTERVALS }
        }
    }

    fun saveIntervals(intervals: List<FocusInterval>) {
        val serializedSet = intervals.map { serializeInterval(it) }.toSet()
        prefs.edit().putStringSet(KEY_FOCUS_INTERVALS, serializedSet).apply()
        _intervalsFlow.value = intervals
    }

    fun addOrUpdateInterval(interval: FocusInterval) {
        val current = getIntervals().toMutableList()
        val index = current.indexOfFirst { it.id == interval.id }
        if (index >= 0) {
            current[index] = interval
        } else {
            current.add(interval)
        }
        saveIntervals(current)
    }

    fun removeInterval(id: String) {
        val current = getIntervals().filter { it.id != id }
        saveIntervals(current)
    }

    fun toggleIntervalEnabled(id: String): Boolean {
        var isNowEnabled = false
        val current = getIntervals().map {
            if (it.id == id) {
                isNowEnabled = !it.isEnabled
                it.copy(isEnabled = isNowEnabled)
            } else {
                it
            }
        }
        saveIntervals(current)
        return isNowEnabled
    }

    fun getActiveScheduledInterval(calendar: Calendar = Calendar.getInstance()): FocusInterval? {
        return getIntervals().firstOrNull { it.isCurrentlyActive(calendar) }
    }

    fun isAnyScheduledIntervalActive(calendar: Calendar = Calendar.getInstance()): Boolean {
        return getActiveScheduledInterval(calendar) != null
    }

    private fun serializeInterval(interval: FocusInterval): String {
        val daysString = interval.daysOfWeek.joinToString(",")
        return "${interval.id};;;${interval.label};;;${interval.startHour};;;${interval.startMinute};;;${interval.endHour};;;${interval.endMinute};;;${daysString};;;${interval.isEnabled}"
    }

    private fun deserializeInterval(raw: String): FocusInterval? {
        return try {
            val parts = raw.split(";;;")
            if (parts.size < 8) return null
            val id = parts[0]
            val label = parts[1]
            val startH = parts[2].toInt()
            val startM = parts[3].toInt()
            val endH = parts[4].toInt()
            val endM = parts[5].toInt()
            val days = if (parts[6].isBlank()) emptySet() else parts[6].split(",").map { it.toInt() }.toSet()
            val enabled = parts[7].toBoolean()
            FocusInterval(id, label, startH, startM, endH, endM, days, enabled)
        } catch (e: Exception) {
            null
        }
    }
}
