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

/**
 * This is the "Ear" of the app. It waits for the AlarmManager to shout: 
 * "TIME'S UP!" and then it acts.
 */
class ScheduleTriggerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getIntExtra("SCHEDULE_ID", -1)
        val isStart = intent.getBooleanExtra("IS_START", true)

        if (scheduleId == -1) return

        // Since we need to talk to the database, we use a Coroutine
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            val schedule = db.scheduleDao().getScheduleById(scheduleId)

            schedule?.let {
                val controller = RingerModeController(context)
                
                if (isStart) {
                    Log.d("Silencer", "Starting schedule: ${it.id}")
                    controller.setMode(it.targetMode)
                } else {
                    Log.d("Silencer", "Ending schedule: ${it.id}")
                    controller.setMode(RingerMode.NORMAL)
                }

                // Re-schedule for next week
                val scheduler = ScheduleAlarmScheduler(context)
                scheduler.schedule(it)
            }
        }
    }
}
