package com.mdzeviatau.todoapp.ui.screens

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.mdzeviatau.todoapp.data.models.task.*
import com.mdzeviatau.todoapp.ui.notifications.GeofenceHelper
import com.mdzeviatau.todoapp.ui.notifications.NotificationHelper
import com.mdzeviatau.todoapp.ui.viewmodel.TaskViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String?, viewModel: TaskViewModel, onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val geofenceHelper = remember { GeofenceHelper(context) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(TaskPriority.LOW) }
    var category by remember { mutableStateOf(TaskCategory.OTHER) }
    var repeatInterval by remember { mutableStateOf(RepeatInterval.NONE) }
    var dueDateMillis by remember { mutableStateOf<Long?>(null) }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(52.2297, 21.0122), 10f)
    }

    LaunchedEffect(taskId) {
        if (taskId != null) {
            val task = viewModel.getTaskById(taskId)
            if (task != null) {
                title = task.title
                description = task.description
                priority = task.priority
                category = task.category
                repeatInterval = task.repeatInterval
                dueDateMillis = task.dueDate
                latitude = task.latitude
                longitude = task.longitude

                if (task.latitude != null && task.longitude != null) {
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(
                        LatLng(task.latitude, task.longitude), 15f
                    )
                }
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(
                            LatLng(it.latitude, it.longitude), 15f
                        )
                    }
                }
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
            TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
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
            TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
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
                                            repeatInterval = repeatInterval,
                                            dueDate = dueDateMillis,
                                            latitude = latitude,
                                            longitude = longitude
                                        )
                                    } else {
                                        viewModel.getTaskById(taskId)?.copy(
                                            title = title,
                                            description = description,
                                            priority = priority,
                                            category = category,
                                            repeatInterval = repeatInterval,
                                            dueDate = dueDateMillis,
                                            latitude = latitude,
                                            longitude = longitude
                                        )
                                    }

                                    if (task != null) {
                                        if (taskId == null) {
                                            viewModel.addTask(task)
                                        } else {
                                            viewModel.updateTask(task)
                                        }

                                        NotificationHelper.cancelNotification(context, task.id)
                                        if (task.dueDate != null && task.status != TaskStatus.COMPLETED) {
                                            NotificationHelper.scheduleNotification(
                                                context = context,
                                                taskId = task.id,
                                                title = task.title,
                                                description = task.description,
                                                timeInMillis = task.dueDate
                                            )
                                        }

                                        geofenceHelper.removeGeofence(task.id)
                                        if (task.latitude != null && task.longitude != null && task.status != TaskStatus.COMPLETED) {
                                            geofenceHelper.addGeofence(
                                                task.id, task.latitude, task.longitude
                                            )
                                        }

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
                label = { Text("Description") },
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

            Text("Location", style = MaterialTheme.typography.labelLarge)
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    GoogleMap(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        cameraPositionState = cameraPositionState,
                        onMapClick = { latLng ->
                            latitude = latLng.latitude
                            longitude = latLng.longitude
                        },
                        uiSettings = remember { MapUiSettings(zoomControlsEnabled = false) }) {
                        if (latitude != null && longitude != null) {
                            Marker(
                                state = remember(latitude, longitude) {
                                    MarkerState(position = LatLng(latitude!!, longitude!!))
                                }, title = "Task Location"
                            )
                        }
                    }
                    if (latitude != null && longitude != null) {
                        Row(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = String.format(
                                        Locale.ENGLISH, "%.4f, %.4f", latitude, longitude
                                    ), style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Row {
                                TextButton(onClick = {
                                    val gmmIntentUri =
                                        Uri.parse("google.navigation:q=$latitude,$longitude")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                    mapIntent.setPackage("com.google.android.apps.maps")
                                    context.startActivity(mapIntent)
                                }) {
                                    Text("Navigate")
                                }
                                TextButton(onClick = {
                                    latitude = null
                                    longitude = null
                                }) {
                                    Text("Clear")
                                }
                            }
                        }

                    } else {
                        Box(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Tap map to set location",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Text("Repeat", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RepeatInterval.entries.forEach { interval ->
                    FilterChip(
                        selected = repeatInterval == interval,
                        onClick = { repeatInterval = interval },
                        label = {
                            Text(
                                interval.name.lowercase().replaceFirstChar { it.uppercase() })
                        })
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
