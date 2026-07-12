package com.afnan.silencer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * This is a "Target Mode" for the phone's ringer.
 */
enum class RingerMode {
    SILENT, VIBRATE, NORMAL, DND
}

/**
 * This class represents a single time schedule in the database.
 * Each row in the "schedules" table will look like this.
 */
@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true) 
    val id: Int = 0,
    
    val startTimeMinutes: Int, // Minutes from start of day (0 to 1439)
    val endTimeMinutes: Int,   // Minutes from start of day (0 to 1439)
    
    /**
     * Stored as a String like "1,2,3" where 1=Monday, 2=Tuesday, etc.
     * This is the simplest way for a beginner to store a list of days.
     */
    val daysOfWeek: String, 
    
    val targetMode: RingerMode,
    val isEnabled: Boolean = true
)
