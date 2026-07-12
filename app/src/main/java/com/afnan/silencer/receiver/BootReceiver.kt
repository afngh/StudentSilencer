package com.afnan.silencer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.afnan.silencer.data.AppDatabase
import com.afnan.silencer.service.ScheduleAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * When the phone restarts, all Alarms are wiped clean by Android.
 * This receiver listens for the phone finishing its boot-up so we 
 * can set all our alarms again.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val scheduler = ScheduleAlarmScheduler(context)
            
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(context)
                val enabledSchedules = db.scheduleDao().getAllEnabledSchedules()
                
                enabledSchedules.forEach {
                    scheduler.schedule(it)
                }
            }
        }
    }
}
