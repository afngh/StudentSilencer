package com.afnan.silencer.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afnan.silencer.data.Schedule
import com.afnan.silencer.data.ScheduleRepository
import com.afnan.silencer.service.ScheduleAlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the list of schedules.
 */
class ScheduleListViewModel(
    private val repository: ScheduleRepository,
    private val alarmScheduler: ScheduleAlarmScheduler
) : ViewModel() {

    // Automatically exposes the list from the database to the UI
    val schedules: StateFlow<List<Schedule>> = repository.allSchedules
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleSchedule(schedule: Schedule, isEnabled: Boolean) {
        val updated = schedule.copy(isEnabled = isEnabled)
        viewModelScope.launch {
            repository.update(updated)
            
            // CRITICAL: Update the real Android alarm!
            if (isEnabled) {
                alarmScheduler.schedule(updated)
            } else {
                alarmScheduler.cancel(updated)
            }
        }
    }

    fun deleteSchedule(schedule: Schedule) {
        viewModelScope.launch {
            repository.delete(schedule)
            // CRITICAL: Remove the alarm from Android system
            alarmScheduler.cancel(schedule)
        }
    }
}
