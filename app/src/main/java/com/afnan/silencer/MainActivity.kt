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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.afnan.silencer.data.AppDatabase
import com.afnan.silencer.data.ScheduleRepository
import com.afnan.silencer.ui.dashboard.DashboardScreen
import com.afnan.silencer.ui.dashboard.DashboardViewModel
import com.afnan.silencer.ui.onboarding.PermissionOnboardingScreen
import com.afnan.silencer.ui.theme.SilencerTheme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

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
 * Simple navigation logic for a beginner.
 * We use a simple 'state' variable to decide which screen to show.
 * This is easier than a full NavHost for a small app.
 */
@Composable
fun MainNavigation() {
    val context = LocalContext.current
    
    // Check if we should show the dashboard or onboarding
    // For now, we'll use a simple state that the onboarding screen can trigger.
    var showDashboard by remember { mutableStateOf(false) }

    if (!showDashboard) {
        PermissionOnboardingScreen(
            onContinue = { showDashboard = true }
        )
    } else {
        // Initialize our ViewModel with the Repository
        val database = AppDatabase.getDatabase(context)
        val repository = ScheduleRepository(database.scheduleDao())
        
        val dashboardViewModel: DashboardViewModel = viewModel(
            factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DashboardViewModel(repository) as T
                }
            }
        )
        
        DashboardScreen(viewModel = dashboardViewModel)
    }
}
