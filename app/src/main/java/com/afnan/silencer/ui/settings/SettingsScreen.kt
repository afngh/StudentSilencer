package com.afnan.silencer.ui.settings

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afnan.silencer.ui.components.SectionHeader
import com.afnan.silencer.ui.components.SettingsItem
import com.afnan.silencer.ui.components.StatusText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var isDndGranted by remember { mutableStateOf(false) }
    var isAlarmGranted by remember { mutableStateOf(false) }
    var isBatteryUnrestricted by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri -> uri?.let { viewModel.exportSchedules(context, it) } }
    )
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> uri?.let { viewModel.importSchedules(context, it) } }
    )

    LaunchedEffect(Unit) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        isDndGranted = nm.isNotificationPolicyAccessGranted
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            isAlarmGranted = am.canScheduleExactAlarms()
        } else {
            isAlarmGranted = true
        }

        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        isBatteryUnrestricted = pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.KeyboardArrowLeft, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SectionHeader("Silent scheduling")
            SettingsItem(
                icon = Icons.Outlined.Info,
                title = "Do not disturb access",
                subtitle = "Required to change ringer mode",
                trailing = {
                    StatusText(
                        text = if (isDndGranted) "Granted" else "Fix",
                        color = if (isDndGranted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                        }
                    )
                }
            )
            SettingsItem(
                icon = Icons.Outlined.Schedule,
                title = "Exact alarm permission",
                subtitle = "Keeps schedules firing on time",
                trailing = {
                    StatusText(
                        text = if (isAlarmGranted) "Granted" else "Fix",
                        color = if (isAlarmGranted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                            }
                        }
                    )
                }
            )
            SettingsItem(
                icon = Icons.Outlined.Smartphone,
                title = "Battery optimization",
                subtitle = "Unrestricted access recommended",
                trailing = {
                    StatusText(
                        text = if (isBatteryUnrestricted) "Granted" else "Fix",
                        color = if (isBatteryUnrestricted) Color(0xFF4CAF50) else Color(0xFFF44336),
                        onClick = {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            )

            SectionHeader("Appearance")
            SettingsItem(
                icon = Icons.Outlined.Palette,
                title = "Theme",
                subtitle = if (uiState.isDarkMode) "Dark Mode Active" else "Light Mode Active",
                trailing = {
                    Switch(
                        checked = uiState.isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.surface,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            )

            SectionHeader("Preference")
            SettingsItem(
                icon = Icons.Outlined.Notifications,
                title = "Notify on mode change",
                trailing = {
                    Switch(
                        checked = uiState.notifyModeChange,
                        onCheckedChange = { viewModel.toggleNotifyModeChange(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.surface,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            )
            SettingsItem(
                icon = Icons.Outlined.Sync,
                title = "Restore on restart",
                trailing = {
                    Switch(
                        checked = uiState.restoreOnRestart,
                        onCheckedChange = { viewModel.toggleRestoreOnRestart(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.surface,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            )

            SectionHeader("Data")
            SettingsItem(
                icon = Icons.Outlined.FileDownload,
                title = "Export rules",
                trailing = { Icon(Icons.Outlined.ChevronRight, contentDescription = null, modifier = Modifier.size(24.dp)) },
                onClick = { exportLauncher.launch("schedules.json") }
            )
            SettingsItem(
                icon = Icons.Outlined.FileUpload,
                title = "Import rules",
                trailing = { Icon(Icons.Outlined.ChevronRight, contentDescription = null, modifier = Modifier.size(24.dp)) },
                onClick = { importLauncher.launch(arrayOf("application/json")) }
            )
            
            SectionHeader("App Info")
            SettingsItem(
                icon = Icons.Outlined.Info,
                title = "Version",
                trailing = { 
                    Text(
                        text = "1.0.0",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
