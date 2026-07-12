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
import com.afnan.silencer.ui.theme.SilencerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SilencerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        MainNavigation()
                    }
                }
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

    NavHost(navController = navController, startDestination = "onboarding") {
        
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
                onManageSchedules = { navController.navigate("schedule_list") }
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
    }
}
