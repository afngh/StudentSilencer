package com.afnan.silencer.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.afnan.silencer.data.Schedule
import com.afnan.silencer.receiver.ScheduleTriggerReceiver
import java.util.*

/**
 * This class tells the Android System: "Hey, at 9:00 AM on Monday, 
 * please wake up my app so I can silence the phone."
 */
class ScheduleAlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(schedule: Schedule) {
        if (!schedule.isEnabled) return

        // We need an alarm for the START time and an alarm for the END time
        scheduleAlarm(schedule, isStart = true)
        scheduleAlarm(schedule, isStart = false)
    }

    private fun scheduleAlarm(schedule: Schedule, isStart: Boolean) {
        val intent = Intent(context, ScheduleTriggerReceiver::class.java).apply {
            putExtra("SCHEDULE_ID", schedule.id)
            putExtra("IS_START", isStart)
        }

        // Each alarm needs a unique ID so they don't overwrite each other
        // Start alarms use ID, End alarms use ID + 100000 (just a simple trick)
        val requestCode = if (isStart) schedule.id else schedule.id + 100000

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

        // "AllowWhileIdle" means the alarm will fire even if the phone is "sleeping" to save battery
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    /**
     * Simple Math for "Next Occurrence":
     * 1. Get current time.
     * 2. Set the target hour/minute.
     * 3. If that time has already passed today, or today isn't in 'daysOfWeek', 
     *    keep adding 1 day until we find a match.
     */
    private fun calculateNextOccurrence(minutesFromStartOfDay: Int, daysOfWeek: String): Long {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        
        val targetHour = minutesFromStartOfDay / 60
        val targetMinute = minutesFromStartOfDay % 60
        
        calendar.set(Calendar.HOUR_OF_DAY, targetHour)
        calendar.set(Calendar.MINUTE, targetMinute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val daysList = daysOfWeek.split(",").map { it.trim().toInt() }
        
        // Find the first matching day starting from today
        for (i in 0..7) {
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            // Calendar.MONDAY = 2, but your app uses 1=Mon, 2=Tue...
            // Let's adjust to your 1=Mon logic:
            val adjustedDay = if (dayOfWeek == Calendar.SUNDAY) 7 else dayOfWeek - 1
            
            if (daysList.contains(adjustedDay) && calendar.timeInMillis > now) {
                return calendar.timeInMillis
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        return calendar.timeInMillis
    }
    
    fun cancel(schedule: Schedule) {
        val intent = Intent(context, ScheduleTriggerReceiver::class.java)
        val startPI = PendingIntent.getBroadcast(context, schedule.id, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        val endPI = PendingIntent.getBroadcast(context, schedule.id + 100000, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        
        startPI?.let { alarmManager.cancel(it) }
        endPI?.let { alarmManager.cancel(it) }
    }
}
