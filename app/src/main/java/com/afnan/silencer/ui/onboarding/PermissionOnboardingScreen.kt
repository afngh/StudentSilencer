package com.afnan.silencer.ui.onboarding

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Adjust
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.afnan.silencer.ui.components.SectionHeader
import com.afnan.silencer.ui.components.SettingsItem
import com.afnan.silencer.ui.components.StatusText

@Composable
fun PermissionOnboardingScreen(onContinue: () -> Unit) {
    val context = LocalContext.current
    
    var isDndGranted by remember { mutableStateOf(false) }
    var isExactAlarmGranted by remember { mutableStateOf(false) }
    var isBatteryExempt by remember { mutableStateOf(false) }

    val checkPermissions = {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        isDndGranted = notificationManager.isNotificationPolicyAccessGranted

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            isExactAlarmGranted = alarmManager.canScheduleExactAlarms()
        } else {
            isExactAlarmGranted = true
        }

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        isBatteryExempt = powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                checkPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val successGreen = Color(0xFF4CAF50)
    val errorRed = Color(0xFFD32F2F)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(64.dp))
            
            Text(
                text = "Permissions Needed",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "We need a few permissions to automate your ringer modes.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            SectionHeader("Required Permissions")
            SettingsItem(
                icon = Icons.Outlined.Adjust,
                title = "Do Not Disturb Access",
                subtitle = "Required to change ringer mode",
                trailing = {
                    StatusText(
                        text = if (isDndGranted) "Granted" else "Grant",
                        color = if (isDndGranted) successGreen else errorRed,
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                        }
                    )
                }
            )
            SettingsItem(
                icon = Icons.Outlined.Timer,
                title = "Exact Alarms",
                subtitle = "Needed for precise scheduling",
                trailing = {
                    StatusText(
                        text = if (isExactAlarmGranted) "Granted" else "Grant",
                        color = if (isExactAlarmGranted) successGreen else errorRed,
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            }
                        }
                    )
                }
            )

            SectionHeader("Recommended")
            SettingsItem(
                icon = Icons.Outlined.BatteryChargingFull,
                title = "Battery Optimization",
                subtitle = "Prevents system from killing the app",
                trailing = {
                    StatusText(
                        text = if (isBatteryExempt) "Granted" else "Fix",
                        color = if (isBatteryExempt) successGreen else errorRed,
                        onClick = {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isDndGranted && isExactAlarmGranted,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Text("Continue", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
