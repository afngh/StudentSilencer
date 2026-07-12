package com.afnan.silencer.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) defines the "queries" we can run on our database.
 * It's the interface between our Kotlin code and the SQL database.
 */
@Dao
interface ScheduleDao {

    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getScheduleById(id: Int): Schedule?

    @Query("SELECT * FROM schedules WHERE isEnabled = 1")
    suspend fun getAllEnabledSchedules(): List<Schedule>

    @Query("SELECT * FROM schedules ORDER BY startTimeMinutes ASC")
    fun getAllSchedules(): Flow<List<Schedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: Schedule): Long

    @Update
    suspend fun updateSchedule(schedule: Schedule)

    @Delete
    suspend fun deleteSchedule(schedule: Schedule)
}
