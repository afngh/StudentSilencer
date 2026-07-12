package com.afnan.silencer.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afnan.silencer.data.RingerMode
import com.afnan.silencer.service.RingerModeController
import com.afnan.silencer.ui.components.SectionHeader
import com.afnan.silencer.ui.components.SettingsItem
import com.afnan.silencer.ui.components.StatusText

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
                title = { },
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
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Permissions Revoked", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onErrorContainer)
                            Text("Schedules won't work. Tap to fix.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }

            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // 1. Current Mode Display (Minimalist)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CurrentModeDisplay(uiState.currentMode)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 2. Next Change Info (Minimalist)
            SectionHeader("Schedule Status")
            SettingsItem(
                icon = Icons.Outlined.Timer,
                title = uiState.nextChangeTime,
                subtitle = uiState.nextChangeLabel
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Manual Override Buttons
            SectionHeader("Manual Overrides")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        selectedOverrideMode = RingerMode.SILENT
                        showOverrideDialog = true
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Silent")
                }
                OutlinedButton(
                    onClick = {
                        selectedOverrideMode = RingerMode.NORMAL
                        showOverrideDialog = true
                    },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                    )
                ) {
                    Text("Normal", color = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            // 4. Small Preview of Schedules
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader("Upcoming Schedules")
                TextButton(onClick = onManageSchedules) {
                    Text(
                        "Manage All", 
                        color = MaterialTheme.colorScheme.onSurface, 
                        fontWeight = FontWeight.SemiBold, 
                        fontSize = 12.sp
                    )
                }
            }
            
            if (uiState.activeSchedules.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No schedules active", color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onManageSchedules,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onSurface,
                            contentColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Schedule")
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(uiState.activeSchedules) { schedule ->
                        SettingsItem(
                            icon = Icons.Outlined.Schedule,
                            title = "${schedule.targetMode}",
                            subtitle = "Active on: ${schedule.daysOfWeek}",
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
        RingerMode.NORMAL -> Triple(Icons.Outlined.Notifications, "NORMAL", MaterialTheme.colorScheme.onSurface)
        RingerMode.VIBRATE -> Triple(Icons.Outlined.VolumeUp, "VIBRATE", MaterialTheme.colorScheme.secondary)
        RingerMode.SILENT -> Triple(Icons.Outlined.VolumeOff, "SILENT", Color(0xFFD32F2F))
        RingerMode.DND -> Triple(Icons.Outlined.DoNotDisturbOn, "DND", Color(0xFF5B53D6))
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = color
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun OverrideDurationDialog(onDismiss: () -> Unit, onConfirm: (Int?) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manual Override") },
        text = { Text("How long should this mode stay active?") },
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = { onConfirm(30) }, modifier = Modifier.fillMaxWidth()) { Text("30 Minutes", color = MaterialTheme.colorScheme.onSurface) }
                TextButton(onClick = { onConfirm(60) }, modifier = Modifier.fillMaxWidth()) { Text("1 Hour", color = MaterialTheme.colorScheme.onSurface) }
                TextButton(onClick = { onConfirm(null) }, modifier = Modifier.fillMaxWidth()) { Text("Indefinitely", color = MaterialTheme.colorScheme.onSurface) }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cancel", color = Color(0xFFD32F2F)) }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large
    )
}
