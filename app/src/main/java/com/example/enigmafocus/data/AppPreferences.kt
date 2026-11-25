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
    private const val KEY_FOCUS_INTERVALS = "focus_intervals"
    private const val KEY_APP_LANGUAGE = "app_language"
    private const val KEY_ANTI_IMPULSE = "anti_impulse_cooldown"
    private const val KEY_HOURLY_LIMIT = "hourly_limit_1m_per_app"
    private const val KEY_TEMP_WHITELIST_PKGS = "temp_whitelist_pkgs"
    private const val KEY_HOURLY_COOLDOWN_PKGS = "hourly_cooldown_pkgs"
    private const val KEY_TEMP_WHITELIST_EXPIRY_PREFIX = "temp_whitelist_expiry_"
    private const val KEY_HOURLY_COOLDOWN_UNTIL_PREFIX = "hourly_cooldown_until_"
    private const val KEY_DAILY_UNLOCK_MINUTES_USED = "daily_unlock_minutes_used"
    private const val KEY_DAILY_UNLOCK_DAY_KEY = "daily_unlock_day_key"
    const val MAX_DAILY_UNLOCK_MINUTES = 15

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
    private val inMemoryMap = mutableMapOf<String, Any>()

    fun isInitialized(): Boolean = ::prefs.isInitialized

    private fun getBooleanPref(key: String, defValue: Boolean): Boolean {
        return if (::prefs.isInitialized) prefs.getBoolean(key, defValue) else (inMemoryMap[key] as? Boolean ?: defValue)
    }

    private fun putBooleanPref(key: String, value: Boolean) {
        if (::prefs.isInitialized) {
            prefs.edit().putBoolean(key, value).apply()
        } else {
            inMemoryMap[key] = value
        }
    }

    private fun getStringPref(key: String, defValue: String?): String? {
        return if (::prefs.isInitialized) prefs.getString(key, defValue) else (inMemoryMap[key] as? String ?: defValue)
    }

    private fun putStringPref(key: String, value: String?) {
        if (::prefs.isInitialized) {
            prefs.edit().putString(key, value).apply()
        } else {
            if (value != null) inMemoryMap[key] = value else inMemoryMap.remove(key)
        }
    }

    private fun getLongPref(key: String, defValue: Long): Long {
        return if (::prefs.isInitialized) prefs.getLong(key, defValue) else (inMemoryMap[key] as? Long ?: defValue)
    }

    private fun putLongPref(key: String, value: Long) {
        if (::prefs.isInitialized) {
            prefs.edit().putLong(key, value).apply()
        } else {
            inMemoryMap[key] = value
        }
    }

    private fun getIntPref(key: String, defValue: Int): Int {
        return if (::prefs.isInitialized) prefs.getInt(key, defValue) else (inMemoryMap[key] as? Int ?: defValue)
    }

    private fun putIntPref(key: String, value: Int) {
        if (::prefs.isInitialized) {
            prefs.edit().putInt(key, value).apply()
        } else {
            inMemoryMap[key] = value
        }
    }

    private fun getStringSetPref(key: String, defValue: Set<String>): Set<String> {
        @Suppress("UNCHECKED_CAST")
        return if (::prefs.isInitialized) (prefs.getStringSet(key, defValue) ?: defValue) else (inMemoryMap[key] as? Set<String> ?: defValue)
    }

    private fun putStringSetPref(key: String, value: Set<String>) {
        if (::prefs.isInitialized) {
            prefs.edit().putStringSet(key, value).apply()
        } else {
            inMemoryMap[key] = value
        }
    }

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

    private val _hourlyLimitFlow = MutableStateFlow(true)
    val hourlyLimitFlow: StateFlow<Boolean> = _hourlyLimitFlow.asStateFlow()

    private val _dailyUsedMinutesFlow = MutableStateFlow(0)
    val dailyUsedMinutesFlow: StateFlow<Int> = _dailyUsedMinutesFlow.asStateFlow()

    private val _intervalsFlow = MutableStateFlow<List<FocusInterval>>(emptyList())
    val intervalsFlow: StateFlow<List<FocusInterval>> = _intervalsFlow.asStateFlow()

    fun init(context: Context) {
        val appContext = context.applicationContext ?: context
        prefs = try {
            appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        } catch (e: IllegalStateException) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                appContext.createDeviceProtectedStorageContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            } else {
                throw e
            }
        }

        // Initialize defaults if not set in a single batched transaction
        val needsInitialGrayscale = !prefs.contains("initial_grayscale_applied")
        prefs.edit().apply {
            if (!prefs.contains(KEY_BLOCKED_PACKAGES)) putStringSet(KEY_BLOCKED_PACKAGES, DEFAULT_POPULAR_PACKAGES)
            if (!prefs.contains(KEY_ALWAYS_BLOCK)) putBoolean(KEY_ALWAYS_BLOCK, true)
            if (!prefs.contains(KEY_AUTO_GRAYSCALE)) putBoolean(KEY_AUTO_GRAYSCALE, true)
            if (!prefs.contains(KEY_APP_LANGUAGE)) putString(KEY_APP_LANGUAGE, "system")
            if (!prefs.contains(KEY_ANTI_IMPULSE)) putBoolean(KEY_ANTI_IMPULSE, false)
            if (!prefs.contains(KEY_HOURLY_LIMIT)) putBoolean(KEY_HOURLY_LIMIT, true)
            if (needsInitialGrayscale) putBoolean("initial_grayscale_applied", true)
        }.apply()
        if (needsInitialGrayscale) {
            GrayscaleManager.setGrayscaleEnabled(context, true)
        }

        _blockedPackagesFlow.value = prefs.getStringSet(KEY_BLOCKED_PACKAGES, DEFAULT_POPULAR_PACKAGES) ?: DEFAULT_POPULAR_PACKAGES
        _focusActiveFlow.value = prefs.getBoolean(KEY_FOCUS_ACTIVE, false)
        _autoGrayscaleFlow.value = prefs.getBoolean(KEY_AUTO_GRAYSCALE, true)
        _alwaysBlockFlow.value = prefs.getBoolean(KEY_ALWAYS_BLOCK, true)
        _strictModeFlow.value = prefs.getBoolean(KEY_STRICT_MODE, false)
        _languageFlow.value = getAppLanguage()
        _antiImpulseFlow.value = prefs.getBoolean(KEY_ANTI_IMPULSE, false)
        _hourlyLimitFlow.value = prefs.getBoolean(KEY_HOURLY_LIMIT, true)
        _dailyUsedMinutesFlow.value = getDailyUsedMinutes()

        loadIntervals()
    }

    fun getSystemLanguage(): String {
        val sysLang = java.util.Locale.getDefault().language.lowercase()
        return if (sysLang.startsWith("es")) "es" else "en"
    }

    fun getSelectedLanguagePreference(): String {
        return getStringPref(KEY_APP_LANGUAGE, "system") ?: "system"
    }

    fun getAppLanguage(): String {
        val pref = getStringPref(KEY_APP_LANGUAGE, "system") ?: "system"
        return if (pref == "system") getSystemLanguage() else pref
    }

    fun isEnglish(): Boolean {
        return getAppLanguage() == "en"
    }

    fun setAppLanguage(language: String) {
        putStringPref(KEY_APP_LANGUAGE, language)
        _languageFlow.value = if (language == "system") getSystemLanguage() else language
    }

    fun isAntiImpulseEnabled(): Boolean {
        return getBooleanPref(KEY_ANTI_IMPULSE, false)
    }

    fun setAntiImpulseEnabled(enabled: Boolean) {
        putBooleanPref(KEY_ANTI_IMPULSE, enabled)
        _antiImpulseFlow.value = enabled
    }

    fun getBlockedPackages(): Set<String> {
        return getStringSetPref(KEY_BLOCKED_PACKAGES, DEFAULT_POPULAR_PACKAGES)
    }

    fun setBlockedPackages(packages: Set<String>) {
        putStringSetPref(KEY_BLOCKED_PACKAGES, packages)
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
        val active = getBooleanPref(KEY_FOCUS_ACTIVE, false)
        if (active) {
            val end = getLongPref(KEY_FOCUS_END_TIMESTAMP, 0L)
            if (end > 0 && System.currentTimeMillis() >= end) {
                // Session expired
                setFocusActive(false, 0L, 0)
                return false
            }
        }
        return active
    }

    fun setFocusActive(active: Boolean, endTimestamp: Long = 0L, durationMinutes: Int = 0) {
        putBooleanPref(KEY_FOCUS_ACTIVE, active)
        putLongPref(KEY_FOCUS_END_TIMESTAMP, endTimestamp)
        putIntPref(KEY_FOCUS_DURATION_MINUTES, durationMinutes)
        _focusActiveFlow.value = active
    }

    fun getFocusEndTimestamp(): Long {
        return getLongPref(KEY_FOCUS_END_TIMESTAMP, 0L)
    }

    fun getFocusDurationMinutes(): Int {
        return getIntPref(KEY_FOCUS_DURATION_MINUTES, 25)
    }

    fun isAutoGrayscaleEnabled(): Boolean {
        return getBooleanPref(KEY_AUTO_GRAYSCALE, true)
    }

    fun setAutoGrayscaleEnabled(context: Context, enabled: Boolean) {
        putBooleanPref(KEY_AUTO_GRAYSCALE, enabled)
        _autoGrayscaleFlow.value = enabled
        if (!enabled) {
            GrayscaleManager.setGrayscaleEnabled(context, false)
        }
    }

    fun isAlwaysBlockEnabled(): Boolean {
        return getBooleanPref(KEY_ALWAYS_BLOCK, true)
    }

    fun setAlwaysBlockEnabled(enabled: Boolean) {
        putBooleanPref(KEY_ALWAYS_BLOCK, enabled)
        _alwaysBlockFlow.value = enabled
    }

    fun isStrictModeEnabled(): Boolean {
        return getBooleanPref(KEY_STRICT_MODE, false)
    }

    fun setStrictModeEnabled(enabled: Boolean) {
        putBooleanPref(KEY_STRICT_MODE, enabled)
        _strictModeFlow.value = enabled
    }

    fun isHourlyLimitEnabled(): Boolean {
        return getBooleanPref(KEY_HOURLY_LIMIT, true)
    }

    fun setHourlyLimitEnabled(enabled: Boolean) {
        putBooleanPref(KEY_HOURLY_LIMIT, enabled)
        _hourlyLimitFlow.value = enabled
    }

    fun setTemporaryWhitelist(
        packageName: String,
        durationMinutes: Int,
        nowMs: Long = System.currentTimeMillis()
    ) {
        val expiry = nowMs + (durationMinutes * 60 * 1000L)
        val currentPkgs = getStringSetPref(KEY_TEMP_WHITELIST_PKGS, emptySet()).toMutableSet()
        currentPkgs.add(packageName)

        val applyHourlyCooldown = isHourlyLimitEnabled() && durationMinutes > 0
        val cooldownUntil = if (applyHourlyCooldown) getNextHourBoundaryMillis(nowMs) else 0L
        val cooldownPkgs = if (applyHourlyCooldown) {
            getStringSetPref(KEY_HOURLY_COOLDOWN_PKGS, emptySet()).toMutableSet().apply { add(packageName) }
        } else {
            null
        }
        val todayKey = if (applyHourlyCooldown) getDayKey(nowMs) else null
        val newDailyUsed = if (applyHourlyCooldown) getDailyUsedMinutes(nowMs) + durationMinutes else 0

        if (::prefs.isInitialized) {
            prefs.edit().apply {
                putLong("$KEY_TEMP_WHITELIST_EXPIRY_PREFIX$packageName", expiry)
                putStringSet(KEY_TEMP_WHITELIST_PKGS, currentPkgs)
                if (applyHourlyCooldown && cooldownPkgs != null && todayKey != null) {
                    putLong("$KEY_HOURLY_COOLDOWN_UNTIL_PREFIX$packageName", cooldownUntil)
                    putStringSet(KEY_HOURLY_COOLDOWN_PKGS, cooldownPkgs)
                    putString(KEY_DAILY_UNLOCK_DAY_KEY, todayKey)
                    putInt(KEY_DAILY_UNLOCK_MINUTES_USED, newDailyUsed)
                }
            }.apply()
        } else {
            inMemoryMap["$KEY_TEMP_WHITELIST_EXPIRY_PREFIX$packageName"] = expiry
            inMemoryMap[KEY_TEMP_WHITELIST_PKGS] = currentPkgs
            if (applyHourlyCooldown && cooldownPkgs != null && todayKey != null) {
                inMemoryMap["$KEY_HOURLY_COOLDOWN_UNTIL_PREFIX$packageName"] = cooldownUntil
                inMemoryMap[KEY_HOURLY_COOLDOWN_PKGS] = cooldownPkgs
                inMemoryMap[KEY_DAILY_UNLOCK_DAY_KEY] = todayKey
                inMemoryMap[KEY_DAILY_UNLOCK_MINUTES_USED] = newDailyUsed
            }
        }
        if (applyHourlyCooldown) {
            _dailyUsedMinutesFlow.value = newDailyUsed
        }
    }

    fun clearTemporaryWhitelist() {
        val currentPkgs = getStringSetPref(KEY_TEMP_WHITELIST_PKGS, emptySet())
        if (::prefs.isInitialized) {
            prefs.edit().apply {
                for (pkg in currentPkgs) {
                    remove("$KEY_TEMP_WHITELIST_EXPIRY_PREFIX$pkg")
                }
                putStringSet(KEY_TEMP_WHITELIST_PKGS, emptySet())
            }.apply()
        } else {
            for (pkg in currentPkgs) {
                inMemoryMap.remove("$KEY_TEMP_WHITELIST_EXPIRY_PREFIX$pkg")
            }
            inMemoryMap[KEY_TEMP_WHITELIST_PKGS] = emptySet<String>()
        }
    }

    fun isTemporarilyWhitelisted(
        packageName: String,
        nowMs: Long = System.currentTimeMillis()
    ): Boolean {
        val perPkgExpiry = getLongPref("$KEY_TEMP_WHITELIST_EXPIRY_PREFIX$packageName", 0L)
        return perPkgExpiry > 0L && nowMs < perPkgExpiry
    }

    fun hasActiveTemporaryWhitelist(nowMs: Long = System.currentTimeMillis()): Boolean {
        val currentPkgs = getStringSetPref(KEY_TEMP_WHITELIST_PKGS, emptySet())
        return currentPkgs.any { pkg ->
            val expiry = getLongPref("$KEY_TEMP_WHITELIST_EXPIRY_PREFIX$pkg", 0L)
            expiry > 0L && nowMs < expiry
        }
    }

    fun getNextHourBoundaryMillis(nowMs: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = nowMs
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.HOUR_OF_DAY, 1)
        }
        return calendar.timeInMillis
    }

    fun getHourlyCooldownUntil(packageName: String): Long {
        return getLongPref("$KEY_HOURLY_COOLDOWN_UNTIL_PREFIX$packageName", 0L)
    }

    fun getRemainingHourlyCooldownMillis(
        packageName: String,
        nowMs: Long = System.currentTimeMillis()
    ): Long {
        if (!isHourlyLimitEnabled()) return 0L
        val cooldownUntil = getHourlyCooldownUntil(packageName)
        return (cooldownUntil - nowMs).coerceAtLeast(0L)
    }

    fun getDayKey(nowMs: Long = System.currentTimeMillis()): String {
        val calendar = Calendar.getInstance().apply { timeInMillis = nowMs }
        return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.DAY_OF_YEAR)}"
    }

    fun getNextDayBoundaryMillis(nowMs: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = nowMs
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis
    }

    fun getDailyUsedMinutes(nowMs: Long = System.currentTimeMillis()): Int {
        val storedDay = getStringPref(KEY_DAILY_UNLOCK_DAY_KEY, null)
        if (storedDay != getDayKey(nowMs)) {
            return 0
        }
        return getIntPref(KEY_DAILY_UNLOCK_MINUTES_USED, 0).coerceAtLeast(0)
    }

    fun isDailyCapReached(nowMs: Long = System.currentTimeMillis()): Boolean {
        if (!isHourlyLimitEnabled()) return false
        return getDailyUsedMinutes(nowMs) >= MAX_DAILY_UNLOCK_MINUTES
    }

    fun getRemainingDailyCapResetMillis(nowMs: Long = System.currentTimeMillis()): Long {
        if (!isDailyCapReached(nowMs)) return 0L
        return (getNextDayBoundaryMillis(nowMs) - nowMs).coerceAtLeast(0L)
    }

    fun clearDailyUsage() {
        if (::prefs.isInitialized) {
            prefs.edit().apply {
                remove(KEY_DAILY_UNLOCK_DAY_KEY)
                putInt(KEY_DAILY_UNLOCK_MINUTES_USED, 0)
            }.apply()
        } else {
            inMemoryMap.remove(KEY_DAILY_UNLOCK_DAY_KEY)
            inMemoryMap[KEY_DAILY_UNLOCK_MINUTES_USED] = 0
        }
        _dailyUsedMinutesFlow.value = 0
    }

    fun canUseTemporaryUnlock(
        packageName: String,
        nowMs: Long = System.currentTimeMillis()
    ): Boolean {
        if (isStrictModeEnabled()) return false
        if (isDailyCapReached(nowMs)) return false
        return getRemainingHourlyCooldownMillis(packageName, nowMs) == 0L
    }

    fun clearHourlyCooldowns() {
        val cooldownPkgs = getStringSetPref(KEY_HOURLY_COOLDOWN_PKGS, emptySet())
        if (::prefs.isInitialized) {
            prefs.edit().apply {
                for (pkg in cooldownPkgs) {
                    remove("$KEY_HOURLY_COOLDOWN_UNTIL_PREFIX$pkg")
                }
                putStringSet(KEY_HOURLY_COOLDOWN_PKGS, emptySet())
            }.apply()
        } else {
            for (pkg in cooldownPkgs) {
                inMemoryMap.remove("$KEY_HOURLY_COOLDOWN_UNTIL_PREFIX$pkg")
            }
            inMemoryMap[KEY_HOURLY_COOLDOWN_PKGS] = emptySet<String>()
        }
    }

    // --- Scheduled Intervals Logic ---

    fun getIntervals(): List<FocusInterval> {
        val rawSet = getStringSetPref(KEY_FOCUS_INTERVALS, emptySet())
        if (rawSet.isEmpty()) {
            return DEFAULT_SCHEDULED_INTERVALS
        }
        val list = rawSet.mapNotNull { deserializeInterval(it) }
        return if (list.isEmpty()) DEFAULT_SCHEDULED_INTERVALS else list.sortedWith(compareBy({ it.startHour }, { it.startMinute }))
    }

    private fun loadIntervals() {
        _intervalsFlow.value = getIntervals()
    }

    fun saveIntervals(intervals: List<FocusInterval>) {
        val rawSet = intervals.map { serializeInterval(it) }.toSet()
        putStringSetPref(KEY_FOCUS_INTERVALS, rawSet)
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
        val cleanLabel = interval.label.replace(";;;", " ")
        val daysString = interval.daysOfWeek.joinToString(",")
        return "${interval.id};;;${cleanLabel};;;${interval.startHour};;;${interval.startMinute};;;${interval.endHour};;;${interval.endMinute};;;${daysString};;;${interval.isEnabled}"
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
