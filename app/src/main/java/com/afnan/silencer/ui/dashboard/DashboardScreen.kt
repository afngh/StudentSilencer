package com.afnan.silencer.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel, onManageSchedules: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val controller = remember { RingerModeController(context) }
    
    var showOverrideDialog by remember { mutableStateOf(false) }
    var selectedOverrideMode by remember { mutableStateOf(RingerMode.NORMAL) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Silencer") },
                actions = {
                    IconButton(onClick = onManageSchedules) {
                        Icon(Icons.Default.Notifications, contentDescription = "Manage Schedules")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 1. Current Mode Display
            CurrentModeCard(uiState.currentMode)

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Next Change Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Next Update", style = MaterialTheme.typography.labelLarge)
                    Text(text = uiState.nextChangeTime, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                    if (uiState.nextChangeLabel.isNotEmpty()) {
                        Text(text = uiState.nextChangeLabel, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 3. Manual Override Buttons
            Text(text = "Manual Overrides", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        selectedOverrideMode = RingerMode.SILENT
                        showOverrideDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Force Silent")
                }
                Button(
                    onClick = {
                        selectedOverrideMode = RingerMode.NORMAL
                        showOverrideDialog = true
                    }
                ) {
                    Text("Force Normal")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            // 4. Small Preview of Schedules
            Text(
                text = "Active Schedules (Preview)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            if (uiState.activeSchedules.isEmpty()) {
                Text("No schedules set up yet.", color = MaterialTheme.colorScheme.outline)
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(uiState.activeSchedules) { schedule ->
                        ListItem(
                            headlineContent = { Text("${schedule.targetMode}") },
                            supportingContent = { Text("Days: ${schedule.daysOfWeek}") }
                        )
                    }
                }
            }
        }

        // Override Duration Dialog
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
fun CurrentModeCard(mode: RingerMode) {
    val icon: ImageVector
    val label: String
    val color: Color

    when (mode) {
        RingerMode.NORMAL -> {
            icon = Icons.Default.Notifications
            label = "NORMAL"
            color = MaterialTheme.colorScheme.primary
        }
        RingerMode.VIBRATE -> {
            icon = Icons.Default.VolumeUp
            label = "VIBRATE"
            color = MaterialTheme.colorScheme.secondary
        }
        RingerMode.SILENT -> {
            icon = Icons.Default.VolumeOff
            label = "SILENT"
            color = MaterialTheme.colorScheme.error
        }
        RingerMode.DND -> {
            icon = Icons.Default.NotificationsOff
            label = "DND"
            color = MaterialTheme.colorScheme.tertiary
        }
    }

    Card(
        modifier = Modifier.size(160.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(48.dp), tint = color)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = label, fontWeight = FontWeight.Bold, color = color, fontSize = 20.sp)
        }
    }
}

@Composable
fun OverrideDurationDialog(onDismiss: () -> Unit, onConfirm: (Int?) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("How long?") },
        text = { Text("Choose how long this manual override should last.") },
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = { onConfirm(30) }, modifier = Modifier.fillMaxWidth()) { Text("30 Minutes") }
                TextButton(onClick = { onConfirm(60) }, modifier = Modifier.fillMaxWidth()) { Text("1 Hour") }
                TextButton(onClick = { onConfirm(null) }, modifier = Modifier.fillMaxWidth()) { Text("Until next change") }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
            }
        }
    )
}
