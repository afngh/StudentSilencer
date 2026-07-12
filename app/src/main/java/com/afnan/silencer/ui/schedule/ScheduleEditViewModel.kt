package com.afnan.silencer.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afnan.silencer.data.RingerMode
import com.afnan.silencer.data.Schedule
import com.afnan.silencer.data.ScheduleRepository
import com.afnan.silencer.service.ScheduleAlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for the Edit screen.
 */
data class ScheduleEditUiState(
    val id: Int = 0,
    val startTimeMinutes: Int = 540, // 9:00 AM
    val endTimeMinutes: Int = 1020,  // 5:00 PM
    val daysOfWeek: Set<Int> = setOf(1, 2, 3, 4, 5), // Mon-Fri
    val targetMode: RingerMode = RingerMode.SILENT,
    val isEnabled: Boolean = true,
    val isNew: Boolean = true,
    val isSaved: Boolean = false,
    val error: String? = null
)

class ScheduleEditViewModel(
    private val repository: ScheduleRepository,
    private val alarmScheduler: ScheduleAlarmScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleEditUiState())
    val uiState: StateFlow<ScheduleEditUiState> = _uiState.asStateFlow()

    fun loadSchedule(id: Int) {
        if (id == 0) return // It's a new schedule
        
        viewModelScope.launch {
            val schedule = repository.getScheduleById(id)
            schedule?.let {
                val daysSet = it.daysOfWeek.split(",")
                    .mapNotNull { d -> d.trim().toIntOrNull() }
                    .toSet()
                
                _uiState.value = ScheduleEditUiState(
                    id = it.id,
                    startTimeMinutes = it.startTimeMinutes,
                    endTimeMinutes = it.endTimeMinutes,
                    daysOfWeek = daysSet,
                    targetMode = it.targetMode,
                    isEnabled = it.isEnabled,
                    isNew = false
                )
            }
        }
    }

    fun updateStartTime(minutes: Int) {
        _uiState.value = _uiState.value.copy(startTimeMinutes = minutes)
    }

    fun updateEndTime(minutes: Int) {
        _uiState.value = _uiState.value.copy(endTimeMinutes = minutes)
    }

    fun toggleDay(day: Int) {
        val currentDays = _uiState.value.daysOfWeek.toMutableSet()
        if (currentDays.contains(day)) {
            currentDays.remove(day)
        } else {
            currentDays.add(day)
        }
        _uiState.value = _uiState.value.copy(daysOfWeek = currentDays)
    }

    fun updateMode(mode: RingerMode) {
        _uiState.value = _uiState.value.copy(targetMode = mode)
    }

    fun saveSchedule() {
        val state = _uiState.value
        
        // --- VALIDATION ---
        if (state.daysOfWeek.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Please select at least one day.")
            return
        }
        if (state.startTimeMinutes == state.endTimeMinutes) {
            _uiState.value = _uiState.value.copy(error = "Start and End time cannot be the same.")
            return
        }
        // Clear error if validation passes
        _uiState.value = _uiState.value.copy(error = null)

        val schedule = Schedule(
            id = state.id,
            startTimeMinutes = state.startTimeMinutes,
            endTimeMinutes = state.endTimeMinutes,
            daysOfWeek = state.daysOfWeek.sorted().joinToString(","),
            targetMode = state.targetMode,
            isEnabled = state.isEnabled
        )

        viewModelScope.launch {
            val finalSchedule = if (state.isNew) {
                val newId = repository.insert(schedule)
                android.util.Log.d("SilentScheduler", "Inserted new schedule with ID: $newId")
                schedule.copy(id = newId.toInt())
            } else {
                repository.update(schedule)
                android.util.Log.d("SilentScheduler", "Updated existing schedule with ID: ${schedule.id}")
                schedule
            }
            
            // Register/Update the actual Android Alarm with the CORRECT ID
            alarmScheduler.schedule(finalSchedule)
            
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }
}
