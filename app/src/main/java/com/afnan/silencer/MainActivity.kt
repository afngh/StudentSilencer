package com.afnan.silencer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.afnan.silencer.data.AppDatabase
import com.afnan.silencer.data.ScheduleRepository
import com.afnan.silencer.service.ScheduleAlarmScheduler
import com.afnan.silencer.ui.dashboard.DashboardScreen
import com.afnan.silencer.ui.dashboard.DashboardViewModel
import com.afnan.silencer.ui.onboarding.PermissionOnboardingScreen
import com.afnan.silencer.ui.schedule.ScheduleEditScreen
import com.afnan.silencer.ui.schedule.ScheduleEditViewModel
import com.afnan.silencer.ui.schedule.ScheduleListScreen
import com.afnan.silencer.ui.schedule.ScheduleListViewModel
import com.afnan.silencer.ui.settings.SettingsScreen
import com.afnan.silencer.ui.theme.SilencerTheme

/**
 * --- APP ARCHITECTURE SUMMARY ---
 * 
 * 1. UI LAYER (Screens & ViewModels):
 *    - MainActivity & MainNavigation: The entry point using Compose NavHost.
 *    - Dashboard: The main hub. ViewModel handles real-time UI state.
 *    - ScheduleList/Edit: Managing user rules.
 * 
 * 2. SERVICE LAYER (The "Engine"):
 *    - RingerModeController: Directly interacts with AudioManager/NotificationManager.
 *    - ScheduleAlarmScheduler: Schedules the exact timing via Android's AlarmManager.
 * 
 * 3. DATA LAYER (Persistence):
 *    - Room Database (AppDatabase): Saves schedules so they survive reboots.
 *    - ScheduleRepository: The middleman between the DB and the UI.
 * 
 * 4. SYSTEM LAYER (Receivers):
 *    - ScheduleTriggerReceiver: Wakes up when an alarm fires to flip the ringer switch.
 *    - BootReceiver: Reschedules all alarms when the phone is turned on.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SilencerTheme {
                MainNavigation()
            }
        }
    }
}

/**
 * Navigation using NavHost. 
 * Why NavHost? As our app grows (Dashboard -> List -> Edit), NavHost manages 
 * the "Back Stack" automatically. This means when a user presses the back button, 
 * Android knows exactly which screen to go back to.
 */
@Composable
fun MainNavigation() {
    val context = LocalContext.current
    val navController = rememberNavController()
    
    // Dependencies
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { ScheduleRepository(database.scheduleDao()) }
    val alarmScheduler = remember { ScheduleAlarmScheduler(context) }

    val notificationManager = remember { context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager }
    val alarmManager = remember { context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager }
    val initialRoute = if (notificationManager.isNotificationPolicyAccessGranted && 
        (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms())) {
        "dashboard"
    } else {
        "onboarding"
    }

    NavHost(navController = navController, startDestination = initialRoute) {
        
        // 1. Onboarding Screen
        composable("onboarding") {
            PermissionOnboardingScreen(
                onContinue = { 
                    navController.navigate("dashboard") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        // 2. Dashboard Screen
        composable("dashboard") {
            val dashboardViewModel: DashboardViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return DashboardViewModel(repository) as T
                    }
                }
            )
            DashboardScreen(
                viewModel = dashboardViewModel,
                onManageSchedules = { navController.navigate("schedule_list") },
                onFixPermissions = { navController.navigate("onboarding") },
                onSettings = { navController.navigate("settings") },
                onEditSchedule = { id -> navController.navigate("schedule_edit/$id") }
            )
        }

        // 3. Schedule List Screen
        composable("schedule_list") {
            val listViewModel: ScheduleListViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return ScheduleListViewModel(repository, alarmScheduler) as T
                    }
                }
            )
            ScheduleListScreen(
                viewModel = listViewModel,
                onAddSchedule = { navController.navigate("schedule_edit/0") },
                onEditSchedule = { id -> navController.navigate("schedule_edit/$id") },
                onBack = { navController.popBackStack() }
            )
        }

        // 4. Schedule Edit Screen
        composable(
            route = "schedule_edit/{scheduleId}",
            arguments = listOf(navArgument("scheduleId") { type = NavType.IntType })
        ) { backStackEntry ->
            val scheduleId = backStackEntry.arguments?.getInt("scheduleId") ?: 0
            val editViewModel: ScheduleEditViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return ScheduleEditViewModel(repository, alarmScheduler) as T
                    }
                }
            )
            ScheduleEditScreen(
                viewModel = editViewModel,
                scheduleId = scheduleId,
                onSaveSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        // 5. Settings Screen
        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
