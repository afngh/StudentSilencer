package com.afnan.silencer.ui.schedule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afnan.silencer.data.RingerMode
import com.afnan.silencer.data.Schedule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleListScreen(
    viewModel: ScheduleListViewModel,
    onAddSchedule: () -> Unit,
    onEditSchedule: (Int) -> Unit,
    onBack: () -> Unit
) {
    val schedules by viewModel.schedules.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Schedules") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←") // Simple back button for now
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddSchedule) {
                Icon(Icons.Default.Add, contentDescription = "Add Schedule")
            }
        }
    ) { padding ->
        if (schedules.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.NotificationsOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No schedules set up yet.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        "Tap the + button to create your first rule!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(schedules) { schedule ->
                    var showDeleteDialog by remember { mutableStateOf(false) }
                    var scheduleToDelete by remember { mutableStateOf<Schedule?>(null) }

                    ScheduleCard(
                        schedule = schedule,
                        onToggle = { enabled -> viewModel.toggleSchedule(schedule, enabled) },
                        onDelete = { 
                            scheduleToDelete = schedule
                            showDeleteDialog = true
                        },
                        onClick = { onEditSchedule(schedule.id) }
                    )

                    if (showDeleteDialog && scheduleToDelete != null) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            title = { Text("Delete Schedule?") },
                            text = { Text("Are you sure you want to remove this schedule? This cannot be undone.") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        scheduleToDelete?.let { viewModel.deleteSchedule(it) }
                                        showDeleteDialog = false
                                    },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Delete")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleCard(
    schedule: Schedule,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val startTime = formatTime(schedule.startTimeMinutes)
    val endTime = formatTime(schedule.endTimeMinutes)
    
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mode Icon
            Icon(
                imageVector = getModeIcon(schedule.targetMode),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "$startTime - $endTime", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "Days: ${formatDays(schedule.daysOfWeek)}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Mode: ${schedule.targetMode}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
            
            Switch(
                checked = schedule.isEnabled,
                onCheckedChange = onToggle
            )
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

fun formatTime(minutes: Int): String {
    val hour = minutes / 60
    val minute = minutes % 60
    val amPm = if (hour < 12) "AM" else "PM"
    val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    return String.format("%02d:%02d %s", displayHour, minute, amPm)
}

fun formatDays(days: String): String {
    val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    return days.split(",")
        .mapNotNull { it.trim().toIntOrNull() }
        .filter { it in 1..7 }
        .joinToString(", ") { dayNames[it - 1] }
}

fun getModeIcon(mode: RingerMode): ImageVector {
    return when (mode) {
        RingerMode.NORMAL -> Icons.Default.Notifications
        RingerMode.VIBRATE -> Icons.Default.VolumeUp
        RingerMode.SILENT -> Icons.Default.VolumeOff
        RingerMode.DND -> Icons.Default.NotificationsOff
    }
}
