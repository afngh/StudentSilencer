package com.afnan.silencer.ui.settings

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afnan.silencer.data.PreferenceManager
import com.afnan.silencer.data.Schedule
import com.afnan.silencer.data.ScheduleRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader

data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val notifyModeChange: Boolean = true,
    val restoreOnRestart: Boolean = true,
    val exportStatus: String? = null,
    val importStatus: String? = null
)

class SettingsViewModel(
    private val preferenceManager: PreferenceManager,
    private val repository: ScheduleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = combine(
        preferenceManager.isDarkMode,
        preferenceManager.notifyModeChange,
        preferenceManager.restoreOnRestart
    ) { darkMode, notify, restore ->
        SettingsUiState(
            isDarkMode = darkMode,
            notifyModeChange = notify,
            restoreOnRestart = restore
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.setDarkMode(enabled)
        }
    }

    fun toggleNotifyModeChange(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.setNotifyModeChange(enabled)
        }
    }

    fun toggleRestoreOnRestart(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.setRestoreOnRestart(enabled)
        }
    }

    fun exportSchedules(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val schedules = repository.allSchedules.first()
                val json = Json.encodeToString(schedules)
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(json.toByteArray())
                }
                Log.d("SilentScheduler", "Exported ${schedules.size} schedules")
            } catch (e: Exception) {
                Log.e("SilentScheduler", "Export failed", e)
            }
        }
    }

    fun importSchedules(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val json = reader.readText()
                    val schedules = Json.decodeFromString<List<Schedule>>(json)
                    
                    schedules.forEach { schedule ->
                        // Reset ID to 0 so Room treats it as a new insert
                        repository.insert(schedule.copy(id = 0))
                    }
                    Log.d("SilentScheduler", "Imported ${schedules.size} schedules")
                }
            } catch (e: Exception) {
                Log.e("SilentScheduler", "Import failed", e)
            }
        }
    }
}
