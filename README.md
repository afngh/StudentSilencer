# AutoRingScheduler

Automatically switch your phone's ringer mode — Silent, Vibrate, Normal, or Do Not Disturb — based on schedules you set. No more manually flipping it at bedtime or forgetting during meetings.

<!-- Add a screenshot or two here once you have them, e.g.:
<p align="center">
  <img src="screenshots/dashboard.png" width="250" />
  <img src="screenshots/schedule_list.png" width="250" />
  <img src="screenshots/settings.png" width="250" />
</p>
-->

## Features

- Create unlimited time-based schedules, each with its own start/end time, active days of the week, and target mode (Silent, Vibrate, Normal, DND)
- Manual override with configurable auto-resume duration
- Schedules survive device restarts (re-registered via a boot receiver)
- Dashboard showing current mode and the next scheduled change
- Full schedule management — create, edit, enable/disable, delete
- Clean permission onboarding flow for Android's special permissions (DND access, exact alarms, battery optimization)

## Tech stack

- **Language**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **Architecture**: MVVM (`ViewModel` + `StateFlow`)
- **Persistence**: Room
- **Scheduling**: `AlarmManager` (`setExactAndAllowWhileIdle`) for precise, time-exact triggers
- **System integration**: `AudioManager`, `NotificationManager`, `BroadcastReceiver` (schedule triggers + `BOOT_COMPLETED` recovery)
- **Min SDK**: 26 (Android 8.0)

## Why AlarmManager over WorkManager

This app needs mode changes to fire at an exact minute (e.g. 9:00 PM sharp), not "sometime in the next 15 minutes," which is what `WorkManager`'s deferred/periodic scheduling would give you. `AlarmManager.setExactAndAllowWhileIdle()` guarantees exact timing even in Doze mode, at the cost of needing the `SCHEDULE_EXACT_ALARM` permission on Android 12+.

## Permissions required

| Permission | Why |
|---|---|
| `ACCESS_NOTIFICATION_POLICY` | Required to change ringer mode / toggle DND. Special permission — granted via a system settings deep link, not a runtime dialog. |
| `SCHEDULE_EXACT_ALARM` | Required on Android 12+ to schedule alarms that fire at an exact time. |
| `RECEIVE_BOOT_COMPLETED` | Lets the app re-register all enabled schedules after a device restart, since `AlarmManager` alarms don't survive a reboot otherwise. |
| Battery optimization exemption | Optional but recommended — prevents the OS from killing background triggers on aggressive OEM battery management (notably an issue on MIUI/Xiaomi devices). |

## Project structure

```
app/src/main/java/com/afnan/autoringscheduler/
├── ui/
│   ├── onboarding/      # Permission onboarding screen
│   ├── dashboard/       # Current mode + next change + manual override
│   └── schedule/        # Schedule list, create/edit screens
├── data/
│   ├── Schedule.kt              # Room entity
│   ├── ScheduleDao.kt           # Room DAO
│   ├── AppDatabase.kt           # Room database singleton
│   └── ScheduleRepository.kt    # Repository wrapping the DAO
├── service/
│   ├── RingerModeController.kt      # Wraps AudioManager / NotificationManager
│   └── ScheduleAlarmScheduler.kt    # Computes next trigger time, registers alarms
└── receiver/
    ├── ScheduleTriggerReceiver.kt   # Fires on schedule alarm, applies mode, reschedules
    └── BootReceiver.kt             # Re-registers all alarms after BOOT_COMPLETED
```

## Building the project

1. Clone the repo and open it in Android Studio (Giraffe or newer recommended)
2. Let Gradle sync — no additional setup needed, all dependencies resolve via Gradle
3. Run on a physical device (recommended over emulator) since DND/exact-alarm permission behavior is more reliable to test on real hardware, and OEM-specific quirks (e.g. MIUI autostart/battery restrictions) only show up on real devices

## Generating a signed release APK

1. **Build → Generate Signed Bundle / APK → APK**
2. Create a new keystore (first time only) — back up the `.jks` file and password somewhere safe, since you'll need the same one for any future signed update
3. Select `release` build variant → Finish
4. Output APK: `app/release/app-release.apk`

## Known limitations / things to keep testing

- OEM background restrictions (MIUI in particular) can silently kill the boot receiver or alarm triggers unless "Autostart" and unrestricted battery access are manually enabled for the app
- No location-based or contact-exception rules yet (planned as future enhancements)
- No cloud backup — schedules live only in local Room storage on-device

## Roadmap / possible next features

- Preset schedules (Work Hours, Sleep, Class Hours)
- Starred-contact call exceptions during DND
- History log of mode changes
- Location-based schedule triggers

## License

<!-- Add your preferred license, e.g. MIT -->
