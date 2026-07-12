package com.afnan.silencer.ui.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.afnan.silencer.data.RingerMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleEditScreen(
    viewModel: ScheduleEditViewModel,
    scheduleId: Int,
    onSaveSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Load data once when screen opens
    LaunchedEffect(scheduleId) {
        viewModel.loadSchedule(scheduleId)
    }

    // Go back once saved
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaveSuccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isNew) "New Schedule" else "Edit Schedule") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Time Selection
            TimeSelectionRow("Start Time", uiState.startTimeMinutes) { h, m ->
                viewModel.updateStartTime(h * 60 + m)
            }
            TimeSelectionRow("End Time", uiState.endTimeMinutes) { h, m ->
                viewModel.updateEndTime(h * 60 + m)
            }

            // 2. Day Selection
            Text("Active Days", style = MaterialTheme.typography.titleMedium)
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
                        label = { Text(day) }
                    )
                }
            }

            // 3. Mode Selection
            Text("Target Mode", style = MaterialTheme.typography.titleMedium)
            RingerModeSelector(
                selectedMode = uiState.targetMode,
                onModeSelected = { viewModel.updateMode(it) }
            )

            // 4. Error Message (if any)
            uiState.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.saveSchedule() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Schedule")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSelectionRow(label: String, totalMinutes: Int, onTimeSelected: (Int, Int) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    val hour = totalMinutes / 60
    val minute = totalMinutes % 60
    
    val timePickerState = rememberTimePickerState(
        initialHour = hour,
        initialMinute = minute,
        is24Hour = false
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium)
        Button(onClick = { showPicker = true }) {
            Text(formatTime(totalMinutes))
        }
    }

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onTimeSelected(timePickerState.hour, timePickerState.minute)
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RingerModeSelector(selectedMode: RingerMode, onModeSelected: (RingerMode) -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RingerMode.entries.forEach { mode ->
            ElevatedFilterChip(
                selected = selectedMode == mode,
                onClick = { onModeSelected(mode) },
                label = { Text(mode.name) }
            )
        }
    }
}
