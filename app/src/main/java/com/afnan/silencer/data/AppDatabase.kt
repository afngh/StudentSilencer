package com.afnan.silencer.data

import android.content.Context
import androidx.room.*

/**
 * TypeConverters tell Room how to save custom types (like our Enum) 
 * into the database (which only understands simple things like Strings and Ints).
 */
class Converters {
    @TypeConverter
    fun fromRingerMode(mode: RingerMode): String = mode.name

    @TypeConverter
    fun toRingerMode(name: String): RingerMode = RingerMode.valueOf(name)
}

@Database(entities = [Schedule::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun scheduleDao(): ScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Singleton pattern: This ensures only one instance of the database 
         * exists across the entire app.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "silent_scheduler_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
