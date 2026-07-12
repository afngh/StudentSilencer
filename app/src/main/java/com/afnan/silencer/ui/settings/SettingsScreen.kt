package com.afnan.silencer.ui.settings

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var isDndGranted by remember { mutableStateOf(false) }
    var isAlarmGranted by remember { mutableStateOf(false) }
    var isBatteryUnrestricted by remember { mutableStateOf(false) }

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

    val successGreen = Color(0xFF4CAF50)
    val errorRed = Color(0xFFD32F2F)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.KeyboardArrowLeft, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
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
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SectionHeader("Silent scheduling")
            SettingsItem(
                icon = Icons.Outlined.Info,
                title = "Do not disturb access",
                subtitle = "Required to change ringer mode automatically",
                trailing = {
                    StatusText(
                        text = if (isDndGranted) "Granted" else "Fix",
                        color = if (isDndGranted) successGreen else errorRed,
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
                        color = if (isAlarmGranted) successGreen else errorRed,
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
                        color = if (isBatteryUnrestricted) successGreen else errorRed,
                        onClick = {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader("General")
            
            var darkMode by remember { mutableStateOf(false) }
            var notifyChange by remember { mutableStateOf(true) }
            var restoreRestart by remember { mutableStateOf(true) }

            SettingsItem(
                icon = Icons.Outlined.Info,
                title = "Dark mode",
                trailing = {
                    Switch(
                        checked = darkMode,
                        onCheckedChange = { darkMode = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color.Black,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.LightGray
                        )
                    )
                }
            )
            SettingsItem(
                icon = Icons.Outlined.Notifications,
                title = "Notify on mode change",
                trailing = {
                    Switch(
                        checked = notifyChange,
                        onCheckedChange = { notifyChange = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color.Black
                        )
                    )
                }
            )
            SettingsItem(
                icon = Icons.Outlined.Sync,
                title = "Restore schedules on restart",
                trailing = {
                    Switch(
                        checked = restoreRestart,
                        onCheckedChange = { restoreRestart = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color.Black
                        )
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader("Data")

            SettingsItem(
                icon = Icons.Outlined.FileDownload,
                title = "Export schedules",
                trailing = { Icon(Icons.Outlined.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            SettingsItem(
                icon = Icons.Outlined.FileUpload,
                title = "Import schedules",
                trailing = { Icon(Icons.Outlined.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            SettingsItem(
                icon = Icons.Outlined.Info,
                title = "About",
                trailing = { 
                    Text(
                        text = "v1.0.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
