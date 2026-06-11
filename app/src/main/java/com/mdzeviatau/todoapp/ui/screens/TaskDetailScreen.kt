package com.mdzeviatau.todoapp.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdzeviatau.todoapp.data.models.task.*
import com.mdzeviatau.todoapp.ui.viewmodel.TaskViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String?, viewModel: TaskViewModel, onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(TaskPriority.LOW) }
    var category by remember { mutableStateOf(TaskCategory.OTHER) }
    var dueDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(taskId) {
        if (taskId != null) {
            val task = viewModel.getTaskById(taskId)
            if (task != null) {
                title = task.title
                description = task.description
                priority = task.priority
                category = task.category
                dueDateMillis = task.dueDate
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDateMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = {
            TextButton(onClick = {
                dueDateMillis = datePickerState.selectedDateMillis
                showDatePicker = false
                showTimePicker = true
            }) { Text("Ok") }
        }, dismissButton = {
            TextButton(onClick = { showDatePicker = false }) { Text("Anuluj") }
        }) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val calendar = Calendar.getInstance().apply {
            dueDateMillis?.let { timeInMillis = it }
        }
        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE),
            is24Hour = true
        )

        AlertDialog(onDismissRequest = { showTimePicker = false }, confirmButton = {
            TextButton(onClick = {
                val date = Instant.ofEpochMilli(dueDateMillis ?: System.currentTimeMillis())
                    .atZone(ZoneId.systemDefault()).toLocalDate()
                val combinedDateTime = LocalDateTime.of(
                    date, LocalTime.of(timePickerState.hour, timePickerState.minute)
                )

                dueDateMillis =
                    combinedDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                showTimePicker = false
            }) { Text("Ok") }
        }, dismissButton = {
            TextButton(onClick = { showTimePicker = false }) { Text("Anuluj") }
        }, text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TimePicker(state = timePickerState)
            }
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (taskId == null) "New Task" else "Edit Task") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Return")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (title.isNotBlank()) {
                                scope.launch {
                                    val task = if (taskId == null) {
                                        Task(
                                            title = title,
                                            description = description,
                                            createdAt = LocalDateTime.now()
                                                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                            priority = priority,
                                            category = category,
                                            dueDate = dueDateMillis
                                        )
                                    } else {
                                        viewModel.getTaskById(taskId)?.copy(
                                            title = title,
                                            description = description,
                                            priority = priority,
                                            category = category,
                                            dueDate = dueDateMillis
                                        )
                                    }

                                    if (task != null) {
                                        if (taskId == null) viewModel.addTask(task) else viewModel.updateTask(
                                            task
                                        )
                                        onNavigateBack()
                                    }
                                }
                            }
                        }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                })
        }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Opis") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Text("Deadline", style = MaterialTheme.typography.labelLarge)
            OutlinedCard(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (dueDateMillis != null) {
                            val dt =
                                Instant.ofEpochMilli(dueDateMillis!!).atZone(ZoneId.systemDefault())
                            dt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                        } else "Choose date and time", style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Text("Priority", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskPriority.entries.forEach { p ->
                    FilterChip(
                        selected = priority == p,
                        onClick = { priority = p },
                        label = { Text(p.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        leadingIcon = {
                            Icon(
                                imageVector = getPriorityIcon(p),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (priority == p) getPriorityColor(p) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        })
                }
            }

            Text("Category", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskCategory.entries.forEach { c ->
                    FilterChip(
                        selected = category == c,
                        onClick = { category = c },
                        label = { Text(c.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        leadingIcon = {
                            Icon(
                                imageVector = getCategoryIcon(c),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        })
                }
            }
        }
    }
}
