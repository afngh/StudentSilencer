package com.afnan.silencer.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.afnan.silencer.data.Schedule
import com.afnan.silencer.receiver.ScheduleTriggerReceiver
import java.util.*

/**
 * Why unique request codes?
 * If you give two alarms the same "Request Code", Android thinks the second one is an update 
 * to the first and overwrites it. To have 10 schedules running independently, we need 20 
 * unique codes (10 for Start, 10 for End).
 */
class ScheduleAlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(schedule: Schedule) {
        if (!schedule.isEnabled) {
            cancel(schedule)
            return
        }

        // We use a math trick to guarantee unique codes:
        // Schedule #5: Start = 10 (5*2), End = 11 (5*2 + 1)
        // Schedule #6: Start = 12 (6*2), End = 13 (6*2 + 1)
        // They will never overlap!
        scheduleAlarm(schedule, isStart = true, requestCode = schedule.id * 2)
        scheduleAlarm(schedule, isStart = false, requestCode = schedule.id * 2 + 1)
    }

    private fun scheduleAlarm(schedule: Schedule, isStart: Boolean, requestCode: Int) {
        val intent = Intent(context, ScheduleTriggerReceiver::class.java).apply {
            putExtra("SCHEDULE_ID", schedule.id)
            putExtra("IS_START", isStart)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = calculateNextOccurrence(
            if (isStart) schedule.startTimeMinutes else schedule.endTimeMinutes,
            schedule.daysOfWeek
        )

        Log.d("SilentScheduler", "Scheduling Alarm ID $requestCode for Schedule ${schedule.id} at $triggerTime")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    private fun calculateNextOccurrence(minutesFromStartOfDay: Int, daysOfWeek: String): Long {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        
        val targetHour = minutesFromStartOfDay / 60
        val targetMinute = minutesFromStartOfDay % 60
        
        calendar.set(Calendar.HOUR_OF_DAY, targetHour)
        calendar.set(Calendar.MINUTE, targetMinute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val daysList = daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
        
        for (i in 0..7) {
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val adjustedDay = if (dayOfWeek == Calendar.SUNDAY) 7 else dayOfWeek - 1
            
            if (daysList.contains(adjustedDay) && calendar.timeInMillis > now) {
                return calendar.timeInMillis
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis
    }
    
    fun cancel(schedule: Schedule) {
        Log.d("SilentScheduler", "Canceling Alarms for Schedule ${schedule.id}")
        val intent = Intent(context, ScheduleTriggerReceiver::class.java)
        
        val startPI = PendingIntent.getBroadcast(context, schedule.id * 2, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        val endPI = PendingIntent.getBroadcast(context, schedule.id * 2 + 1, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        
        startPI?.let { alarmManager.cancel(it); it.cancel() }
        endPI?.let { alarmManager.cancel(it); it.cancel() }
    }
}
