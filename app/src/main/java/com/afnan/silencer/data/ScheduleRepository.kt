package com.afnan.silencer.data

import kotlinx.coroutines.flow.Flow

/**
 * The Repository is a clean way to manage data. 
 * Instead of the UI (ViewModel) talking to the Database directly, 
 * it talks to this Repository. 
 */
class ScheduleRepository(private val scheduleDao: ScheduleDao) {

    // A list of all schedules that updates itself automatically
    val allSchedules: Flow<List<Schedule>> = scheduleDao.getAllSchedules()

    suspend fun getScheduleById(id: Int): Schedule? {
        return scheduleDao.getScheduleById(id)
    }

    suspend fun insert(schedule: Schedule) {
        scheduleDao.insertSchedule(schedule)
    }


    suspend fun update(schedule: Schedule) {
        scheduleDao.updateSchedule(schedule)
    }

    suspend fun delete(schedule: Schedule) {
        scheduleDao.deleteSchedule(schedule)
    }
}
