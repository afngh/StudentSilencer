package com.afnan.silencer.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afnan.silencer.data.RingerMode
import com.afnan.silencer.data.Schedule
import com.afnan.silencer.data.ScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * UI State for the Dashboard.
 * We group everything the UI needs into one "data class" so it's easy to manage.
 */
data class DashboardUiState(
    val currentMode: RingerMode = RingerMode.NORMAL,
    val nextChangeTime: String = "No upcoming changes",
    val nextChangeLabel: String = "",
    val activeSchedules: List<Schedule> = emptyList()
)

/**
 * The ViewModel is like the "brain" of the Dashboard.
 * 
 * Why use StateFlow?
 * Imagine a variable is like a piece of paper with a number written on it. To see if it changed, 
 * you have to keep looking at it.
 * A StateFlow is like a TV broadcast. The ViewModel "broadcasts" the latest UI state, 
 * and the Screen (UI) just "tunes in" (collects). Whenever the data changes, the UI 
 * automatically updates itself. It's safe, efficient, and handles screen rotations perfectly.
 */
class DashboardViewModel(private val repository: ScheduleRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        // Start listening to the database as soon as the ViewModel is created
        viewModelScope.launch {
            repository.allSchedules.collectLatest { schedules ->
                updateDashboard(schedules)
            }
        }
    }

    private fun updateDashboard(schedules: List<Schedule>) {
        val enabledSchedules = schedules.filter { it.isEnabled }
        
        // Simple logic to find the "next" change. 
        // For a beginner, we'll just show a preview of the list for now.
        // We will improve this logic in later steps!
        
        val nextLabel = if (enabledSchedules.isNotEmpty()) {
            "Total active schedules: ${enabledSchedules.size}"
        } else {
            "No schedules active"
        }

        _uiState.value = _uiState.value.copy(
            activeSchedules = enabledSchedules.take(3), // Just a preview
            nextChangeLabel = nextLabel
        )
    }

    // We can manually update the UI state's current mode when an override happens
    fun updateCurrentMode(mode: RingerMode) {
        _uiState.value = _uiState.value.copy(currentMode = mode)
    }
}
