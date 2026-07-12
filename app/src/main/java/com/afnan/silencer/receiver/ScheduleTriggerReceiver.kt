package com.afnan.silencer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.afnan.silencer.data.AppDatabase
import com.afnan.silencer.data.RingerMode
import com.afnan.silencer.service.RingerModeController
import com.afnan.silencer.service.ScheduleAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScheduleTriggerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getIntExtra("SCHEDULE_ID", -1)
        val isStart = intent.getBooleanExtra("IS_START", true)

        Log.d("SilentScheduler", "Alarm Received! ID: $scheduleId, IsStart: $isStart")

        if (scheduleId == -1) return

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            val schedule = db.scheduleDao().getScheduleById(scheduleId)

            if (schedule != null && schedule.isEnabled) {
                val controller = RingerModeController(context)
                
                if (isStart) {
                    Log.d("SilentScheduler", "Applying Target Mode: ${schedule.targetMode}")
                    controller.setMode(schedule.targetMode)
                } else {
                    Log.d("SilentScheduler", "Schedule Ended. Returning to NORMAL")
                    controller.setMode(RingerMode.NORMAL)
                }

                // IMPORTANT: Re-schedule for the next occurrence (next week)
                val scheduler = ScheduleAlarmScheduler(context)
                scheduler.schedule(schedule)
            } else {
                Log.d("SilentScheduler", "Schedule $scheduleId not found or disabled. Skipping.")
            }
        }
    }
}
