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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.afnan.silencer.ui.components.AppLogo
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            AppLogo()

            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Let's Get Set Up",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Silencer needs a few tools to keep your phone quiet at the right times.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )

            SectionHeader("Permissions")
            SettingsItem(
                icon = Icons.Outlined.Adjust,
                title = "Do Not Disturb",
                subtitle = "To control your ringer mode",
                trailing = {
                    StatusText(
                        text = if (isDndGranted) "Granted" else "Grant",
                        color = if (isDndGranted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                        }
                    )
                }
            )
            SettingsItem(
                icon = Icons.Outlined.Timer,
                title = "Exact Alarms",
                subtitle = "For precise schedule timing",
                trailing = {
                    StatusText(
                        text = if (isExactAlarmGranted) "Granted" else "Grant",
                        color = if (isExactAlarmGranted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
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

            SectionHeader("System")
            SettingsItem(
                icon = Icons.Outlined.BatteryChargingFull,
                title = "Battery Unrestricted",
                subtitle = "Keep schedules alive in background",
                trailing = {
                    StatusText(
                        text = if (isBatteryExempt) "Optimized" else "Fix",
                        color = if (isBatteryExempt) Color(0xFF4CAF50) else Color(0xFFF44336),
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
                    .height(64.dp)
                    .padding(bottom = 8.dp),
                enabled = isDndGranted && isExactAlarmGranted,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text("Start Silencing", fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
        }
    }
}
