package com.afnan.silencer.ui.schedule

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afnan.silencer.data.RingerMode
import com.afnan.silencer.ui.components.SectionHeader
import com.afnan.silencer.ui.components.SettingsItem
import com.afnan.silencer.ui.components.StatusText
import com.afnan.silencer.ui.components.formatTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleEditScreen(
    viewModel: ScheduleEditViewModel,
    scheduleId: Int,
    onSaveSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(scheduleId) {
        viewModel.loadSchedule(scheduleId)
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            Toast.makeText(context, "Schedule saved!", Toast.LENGTH_SHORT).show()
            onSaveSuccess()
        }
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = if (uiState.isNew) "New Schedule" else "Edit Schedule",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SectionHeader("Time range")
            // Simplified for the demo, in a real app these would open pickers
            TimeSelectionRow("Start Time", uiState.startTimeMinutes) { h, m ->
                viewModel.updateStartTime(h * 60 + m)
            }
            TimeSelectionRow("End Time", uiState.endTimeMinutes) { h, m ->
                viewModel.updateEndTime(h * 60 + m)
            }

            SectionHeader("Active days")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                days.forEachIndexed { index, day ->
                    val dayNum = index + 1
                    FilterChip(
                        selected = uiState.daysOfWeek.contains(dayNum),
                        onClick = { viewModel.toggleDay(dayNum) },
                        label = { Text(day) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.Black,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            SectionHeader("Target mode")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RingerMode.entries.forEach { mode ->
                    FilterChip(
                        selected = uiState.targetMode == mode,
                        onClick = { viewModel.updateMode(mode) },
                        label = { Text(mode.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.Black,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            uiState.error?.let {
                Text(
                    text = it,
                    color = Color(0xFFD32F2F),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { viewModel.saveSchedule() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Save Schedule", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSelectionRow(label: String, totalMinutes: Int, onTimeSelected: (Int, Int) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    
    val timePickerState = rememberTimePickerState(
        initialHour = totalMinutes / 60,
        initialMinute = totalMinutes % 60,
        is24Hour = false
    )

    SettingsItem(
        icon = Icons.Outlined.AccessTime,
        title = label,
        trailing = {
            StatusText(
                text = formatTime(totalMinutes),
                color = Color.Black,
                onClick = { showPicker = true }
            )
        },
        onClick = { showPicker = true }
    )

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onTimeSelected(timePickerState.hour, timePickerState.minute)
                    showPicker = false
                }) { Text("OK", color = Color.Black) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel", color = Color.Gray) }
            },
            text = {
                TimePicker(state = timePickerState)
            },
            containerColor = Color.White
        )
    }
}
