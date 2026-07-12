package com.afnan.silencer.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afnan.silencer.data.RingerMode
import com.afnan.silencer.service.RingerModeController
import com.afnan.silencer.ui.components.SectionHeader
import com.afnan.silencer.ui.components.SettingsItem
import com.afnan.silencer.ui.components.formatTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel, 
    onManageSchedules: () -> Unit,
    onFixPermissions: () -> Unit,
    onSettings: () -> Unit,
    onEditSchedule: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val controller = remember { RingerModeController(context) }
    
    val notificationManager = remember { context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager }
    val alarmManager = remember { context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager }
    
    var permissionsGranted by remember { mutableStateOf(true) }
    
    DisposableEffect(Unit) {
        permissionsGranted = notificationManager.isNotificationPolicyAccessGranted && 
                             (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms())
        onDispose { }
    }

    var showOverrideDialog by remember { mutableStateOf(false) }
    var selectedOverrideMode by remember { mutableStateOf(RingerMode.NORMAL) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Silencer", fontWeight = FontWeight.Black) },
                actions = {
                    IconButton(onClick = onManageSchedules) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Manage Schedules")
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings")
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
                .padding(horizontal = 24.dp)
        ) {
            if (!permissionsGranted) {
                Surface(
                    onClick = onFixPermissions,
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .shadow(4.dp, RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Permissions Revoked", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            Text("Schedules won't work. Tap to fix.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // 1. Current Mode Display (Maximalist)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                CurrentModeDisplay(uiState.currentMode)
            }

            // 2. Schedule Status
            SectionHeader("Quick Status")
            SettingsItem(
                icon = Icons.Outlined.Timer,
                title = uiState.nextChangeTime,
                subtitle = uiState.nextChangeLabel
            )

            // 3. Manual Override Buttons
            SectionHeader("Manual Control")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        selectedOverrideMode = RingerMode.SILENT
                        showOverrideDialog = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .shadow(8.dp, RoundedCornerShape(20.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Silent", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
                Button(
                    onClick = {
                        selectedOverrideMode = RingerMode.NORMAL
                        showOverrideDialog = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .shadow(8.dp, RoundedCornerShape(20.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Normal", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }

            // 4. Preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader("Your Rules")
                TextButton(onClick = onManageSchedules) {
                    Text("VIEW ALL", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
            
            if (uiState.activeSchedules.isEmpty()) {
                Button(
                    onClick = onManageSchedules,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .shadow(4.dp, RoundedCornerShape(20.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create Your First Schedule", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(uiState.activeSchedules) { schedule ->
                        SettingsItem(
                            icon = Icons.Outlined.Schedule,
                            title = "${schedule.targetMode}",
                            subtitle = "Starts at ${formatTime(schedule.startTimeMinutes)}",
                            onClick = { onEditSchedule(schedule.id) }
                        )
                    }
                }
            }
        }

        if (showOverrideDialog) {
            OverrideDurationDialog(
                onDismiss = { showOverrideDialog = false },
                onConfirm = { durationMinutes ->
                    controller.setMode(selectedOverrideMode)
                    viewModel.updateCurrentMode(selectedOverrideMode)
                    showOverrideDialog = false
                }
            )
        }
    }
}

@Composable
fun CurrentModeDisplay(mode: RingerMode) {
    val (icon, label, color) = when (mode) {
        RingerMode.NORMAL -> Triple(Icons.Outlined.Notifications, "NORMAL", MaterialTheme.colorScheme.primary)
        RingerMode.VIBRATE -> Triple(Icons.Outlined.VolumeUp, "VIBRATE", MaterialTheme.colorScheme.secondary)
        RingerMode.SILENT -> Triple(Icons.Outlined.VolumeOff, "SILENT", Color(0xFFF44336))
        RingerMode.DND -> Triple(Icons.Outlined.DoNotDisturbOn, "DND", Color(0xFF5B53D6))
    }

    Surface(
        modifier = Modifier
            .size(160.dp)
            .shadow(16.dp, RoundedCornerShape(40.dp)),
        shape = RoundedCornerShape(40.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = color
            )
        }
    }
}

@Composable
fun OverrideDurationDialog(onDismiss: () -> Unit, onConfirm: (Int?) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manual Switch", fontWeight = FontWeight.Black) },
        text = { Text("How long should we stay in this mode?") },
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { onConfirm(30) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("30 Minutes", fontWeight = FontWeight.Bold) }
                
                Button(
                    onClick = { onConfirm(60) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("1 Hour", fontWeight = FontWeight.Bold) }
                
                Button(
                    onClick = { onConfirm(null) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Indefinitely", fontWeight = FontWeight.Bold) }
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("CANCEL", color = Color(0xFFF44336), fontWeight = FontWeight.Black) }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp)
    )
}
