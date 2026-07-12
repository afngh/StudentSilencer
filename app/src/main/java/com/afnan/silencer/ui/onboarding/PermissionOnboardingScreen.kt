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
import androidx.compose.foundation.verticalScroll
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

@Composable
fun PermissionOnboardingScreen(onContinue: () -> Unit) {
    val context = LocalContext.current
    
    // These states keep track of whether permissions are granted
    var isDndGranted by remember { mutableStateOf(false) }
    var isExactAlarmGranted by remember { mutableStateOf(false) }
    var isBatteryExempt by remember { mutableStateOf(false) }

    // This function checks all permissions at once
    val checkPermissions = {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        isDndGranted = notificationManager.isNotificationPolicyAccessGranted

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            isExactAlarmGranted = alarmManager.canScheduleExactAlarms()
        } else {
            isExactAlarmGranted = true // Not needed on older Android versions
        }

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        isBatteryExempt = powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    // Lifecycle logic: Re-check permissions every time the user comes back to the app
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Permissions Needed",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "To schedule silence automatically, we need a few permissions from your phone.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // 1. Do Not Disturb Access
        PermissionCard(
            title = "Do Not Disturb Access",
            description = "Allows the app to turn on Silent or Vibrate mode.",
            isGranted = isDndGranted,
            onClick = {
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                context.startActivity(intent)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Exact Alarms
        PermissionCard(
            title = "Exact Alarms",
            description = "Needed to trigger your schedules at the precise time.",
            isGranted = isExactAlarmGranted,
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val intent = Intent().apply {
                        action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Battery Optimization (Optional)
        PermissionCard(
            title = "Battery Optimization",
            description = "Prevents the system from killing the app in the background.",
            isGranted = isBatteryExempt,
            onClick = {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                context.startActivity(intent)
            }
        )

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
            enabled = isDndGranted && isExactAlarmGranted, // Required permissions
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Continue")
        }
    }
}

@Composable
fun PermissionCard(
    title: String,
    description: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    text = if (isGranted) "Granted" else "Not Granted",
                    color = if (isGranted) Color(0xFF2E7D32) else Color(0xFFC62828),
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = description,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            if (!isGranted) {
                Button(onClick = onClick) {
                    Text("Grant Permission")
                }
            }
        }
    }
}
