package com.afnan.silencer.ui.schedule

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afnan.silencer.data.RingerMode
import com.afnan.silencer.data.Schedule
import com.afnan.silencer.ui.components.SectionHeader
import com.afnan.silencer.ui.components.SettingsItem
import com.afnan.silencer.ui.components.StatusText
import com.afnan.silencer.ui.components.formatTime

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
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.KeyboardArrowLeft, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddSchedule,
                containerColor = MaterialTheme.colorScheme.onSurface,
                contentColor = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Add Schedule")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "My Schedules",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (schedules.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.NotificationsOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No schedules active",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(schedules) { schedule ->
                        var showDeleteDialog by remember { mutableStateOf(false) }

                        SettingsItem(
                            icon = getModeIcon(schedule.targetMode),
                            title = "${formatTime(schedule.startTimeMinutes)} - ${formatTime(schedule.endTimeMinutes)}",
                            subtitle = "${schedule.targetMode} • ${formatDays(schedule.daysOfWeek)}",
                            trailing = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(
                                        checked = schedule.isEnabled,
                                        onCheckedChange = { viewModel.toggleSchedule(schedule, it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = MaterialTheme.colorScheme.surface,
                                            checkedTrackColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    IconButton(onClick = { showDeleteDialog = true }) {
                                        Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color(0xFFD32F2F))
                                    }
                                }
                            },
                            onClick = { onEditSchedule(schedule.id) }
                        )

                        if (showDeleteDialog) {
                            AlertDialog(
                                onDismissRequest = { showDeleteDialog = false },
                                title = { Text("Delete Schedule?") },
                                text = { Text("This will permanently remove this rule.") },
                                confirmButton = {
                                    TextButton(onClick = {
                                        viewModel.deleteSchedule(schedule)
                                        showDeleteDialog = false
                                    }) {
                                        Text("Delete", color = Color(0xFFD32F2F))
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteDialog = false }) {
                                        Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                                    }
                                },
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        }
                    }
                }
            }
        }
    }
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
        RingerMode.NORMAL -> Icons.Outlined.Notifications
        RingerMode.VIBRATE -> Icons.Outlined.VolumeUp
        RingerMode.SILENT -> Icons.Outlined.VolumeOff
        RingerMode.DND -> Icons.Outlined.DoNotDisturbOn
    }
}
