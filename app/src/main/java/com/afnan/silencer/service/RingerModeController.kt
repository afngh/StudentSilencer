package com.afnan.silencer.service

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import com.afnan.silencer.data.RingerMode

/**
 * This class is the "hand" that actually reaches into the phone settings 
 * and flips the switch for Silent, Vibrate, or DND.
 */
class RingerModeController(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /**
     * Why DND is different:
     * - AudioManager.setRingerMode() handles basic stuff: Sound ON, Vibrate ONLY, or Sound OFF.
     * - NotificationManager.setInterruptionFilter() handles Do Not Disturb (DND).
     *   DND is "smarter" than Silent—it can let favorite contacts call through while keeping 
     *   everything else quiet. On modern Android, "Silent" is often just a specific DND state.
     */
    fun setMode(mode: RingerMode) {
        // If we don't have permission, we can't do anything
        if (!notificationManager.isNotificationPolicyAccessGranted) return

        when (mode) {
            RingerMode.NORMAL -> {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            }
            RingerMode.VIBRATE -> {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
            }
            RingerMode.SILENT -> {
                // On most modern phones, this sets the phone to totally silent
                audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
            }
            RingerMode.DND -> {
                // This activates the actual "Do Not Disturb" mode icon in your status bar
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
            }
        }
    }
}
