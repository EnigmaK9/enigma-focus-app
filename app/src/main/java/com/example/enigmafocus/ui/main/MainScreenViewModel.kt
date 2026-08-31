package com.example.enigmafocus.ui.main

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.enigmafocus.data.AppInfo
import com.example.enigmafocus.data.AppPreferences
import com.example.enigmafocus.data.FocusInterval
import com.example.enigmafocus.data.InstalledAppsRepository
import com.example.enigmafocus.manager.FocusSessionManager
import com.example.enigmafocus.manager.GrayscaleManager
import com.example.enigmafocus.service.FocusAccessibilityService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class MainUiState(
    val isFocusActive: Boolean = false,
    val remainingMillis: Long = 0L,
    val selectedDurationMinutes: Int = 25,
    val isGrayscaleActive: Boolean = false,
    val hasSecureSettingsPermission: Boolean = false,
    val isAccessibilityEnabled: Boolean = false,
    val isAutoGrayscaleEnabled: Boolean = true,
    val isAlwaysBlockEnabled: Boolean = true,
    val isStrictModeEnabled: Boolean = false,
    val scheduledIntervals: List<FocusInterval> = emptyList(),
    val activeScheduledInterval: FocusInterval? = null,
    val installedApps: List<AppInfo> = emptyList(),
    val filteredApps: List<AppInfo> = emptyList(),
    val searchQuery: String = "",
    val isLoadingApps: Boolean = false
)

class MainScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InstalledAppsRepository(application)
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        refreshState()
        loadInstalledApps()
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            AppPreferences.focusActiveFlow.collect { active ->
                _uiState.update { it.copy(isFocusActive = active) }
                if (active) {
                    startLocalTimerLoop()
                } else {
                    timerJob?.cancel()
                    _uiState.update { it.copy(remainingMillis = 0L) }
                }
            }
        }

        viewModelScope.launch {
            AppPreferences.autoGrayscaleFlow.collect { auto ->
                val isGray = GrayscaleManager.isGrayscaleActive(getApplication())
                _uiState.update { it.copy(isAutoGrayscaleEnabled = auto, isGrayscaleActive = isGray) }
            }
        }

        viewModelScope.launch {
            AppPreferences.alwaysBlockFlow.collect { always ->
                _uiState.update { it.copy(isAlwaysBlockEnabled = always) }
            }
        }

        viewModelScope.launch {
            AppPreferences.strictModeFlow.collect { strict ->
                _uiState.update { it.copy(isStrictModeEnabled = strict) }
            }
        }

        viewModelScope.launch {
            AppPreferences.intervalsFlow.collect { intervals ->
                val active = AppPreferences.getActiveScheduledInterval()
                _uiState.update { it.copy(scheduledIntervals = intervals, activeScheduledInterval = active) }
            }
        }

        viewModelScope.launch {
            AppPreferences.blockedPackagesFlow.collect { blockedSet ->
                _uiState.update { state ->
                    val updatedApps = state.installedApps.map { app ->
                        app.copy(isBlocked = blockedSet.contains(app.packageName))
                    }
                    val filtered = filterApps(updatedApps, state.searchQuery)
                    state.copy(installedApps = updatedApps, filteredApps = filtered)
                }
            }
        }
    }

    fun refreshState() {
        val context = getApplication<Application>()
        val hasSecure = GrayscaleManager.hasSecureSettingsPermission(context)
        val isGray = GrayscaleManager.isGrayscaleActive(context)
        val isAccess = FocusAccessibilityService.isAccessibilityServiceEnabled(context)
        val isFocus = AppPreferences.isFocusActive()
        val duration = AppPreferences.getFocusDurationMinutes()
        val autoGray = AppPreferences.isAutoGrayscaleEnabled()
        val alwaysBlock = AppPreferences.isAlwaysBlockEnabled()
        val strict = AppPreferences.isStrictModeEnabled()
        val intervals = AppPreferences.getIntervals()
        val activeInterval = AppPreferences.getActiveScheduledInterval()

        _uiState.update {
            it.copy(
                hasSecureSettingsPermission = hasSecure,
                isGrayscaleActive = isGray,
                isAccessibilityEnabled = isAccess,
                isFocusActive = isFocus,
                selectedDurationMinutes = duration,
                isAutoGrayscaleEnabled = autoGray,
                isAlwaysBlockEnabled = alwaysBlock,
                isStrictModeEnabled = strict,
                scheduledIntervals = intervals,
                activeScheduledInterval = activeInterval
            )
        }

        if (isFocus) {
            startLocalTimerLoop()
        }
    }

    private fun startLocalTimerLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                val endTimestamp = AppPreferences.getFocusEndTimestamp()
                if (endTimestamp > 0) {
                    val remaining = endTimestamp - System.currentTimeMillis()
                    if (remaining <= 0) {
                        _uiState.update { it.copy(remainingMillis = 0L) }
                        break
                    }
                    _uiState.update { it.copy(remainingMillis = remaining) }
                } else {
                    _uiState.update { it.copy(remainingMillis = 0L) }
                }
                delay(1000)
            }
        }
    }

    fun setDuration(minutes: Int) {
        _uiState.update { it.copy(selectedDurationMinutes = minutes) }
    }

    fun startFocusSession(minutes: Int? = null) {
        val duration = minutes ?: _uiState.value.selectedDurationMinutes
        FocusSessionManager.startSession(getApplication(), duration)
        refreshState()
    }

    fun stopFocusSession() {
        FocusSessionManager.stopSession(getApplication())
        refreshState()
    }

    fun toggleGrayscale() {
        val context = getApplication<Application>()
        GrayscaleManager.toggleGrayscale(context)
        val isNowActive = GrayscaleManager.isGrayscaleActive(context)
        _uiState.update { it.copy(isGrayscaleActive = isNowActive) }
    }

    fun setAutoGrayscale(enabled: Boolean) {
        AppPreferences.setAutoGrayscaleEnabled(getApplication(), enabled)
        refreshState()
    }

    fun setAlwaysBlock(enabled: Boolean) {
        AppPreferences.setAlwaysBlockEnabled(enabled)
    }

    fun setStrictMode(enabled: Boolean) {
        AppPreferences.setStrictModeEnabled(enabled)
    }

    fun toggleInterval(id: String) {
        AppPreferences.toggleIntervalEnabled(id)
    }

    fun addOrUpdateInterval(interval: FocusInterval) {
        AppPreferences.addOrUpdateInterval(interval)
    }

    fun removeInterval(id: String) {
        AppPreferences.removeInterval(id)
    }

    fun toggleAppBlock(packageName: String) {
        AppPreferences.toggleBlockedPackage(packageName)
    }

    fun blockAllPopular() {
        val current = AppPreferences.getBlockedPackages().toMutableSet()
        current.addAll(AppPreferences.DEFAULT_POPULAR_PACKAGES)
        AppPreferences.setBlockedPackages(current)
    }

    fun unblockAllPopular() {
        val current = AppPreferences.getBlockedPackages().toMutableSet()
        current.removeAll(AppPreferences.DEFAULT_POPULAR_PACKAGES)
        AppPreferences.setBlockedPackages(current)
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingApps = true) }
            val apps = repository.getInstalledApps()
            val filtered = filterApps(apps, _uiState.value.searchQuery)
            _uiState.update {
                it.copy(
                    installedApps = apps,
                    filteredApps = filtered,
                    isLoadingApps = false
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update {
            val filtered = filterApps(it.installedApps, query)
            it.copy(searchQuery = query, filteredApps = filtered)
        }
    }

    private fun filterApps(apps: List<AppInfo>, query: String): List<AppInfo> {
        if (query.isBlank()) return apps
        val lower = query.lowercase().trim()
        return apps.filter { it.name.lowercase().contains(lower) || it.packageName.lowercase().contains(lower) }
    }
}
