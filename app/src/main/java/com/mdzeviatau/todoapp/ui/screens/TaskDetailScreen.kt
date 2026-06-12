package com.mdzeviatau.todoapp.ui.screens

import android.content.pm.PackageManager
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.mdzeviatau.todoapp.data.models.task.*
import com.mdzeviatau.todoapp.ui.components.*
import com.mdzeviatau.todoapp.ui.notifications.GeofenceHelper
import com.mdzeviatau.todoapp.ui.notifications.NotificationHelper
import com.mdzeviatau.todoapp.ui.viewmodel.TaskViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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
    var attachments by remember { mutableStateOf<List<String>>(emptyList()) }

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
                attachments = task.attachments

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

    DateTimePickerDialogs(
        dueDateMillis = dueDateMillis,
        showDatePicker = showDatePicker,
        showTimePicker = showTimePicker,
        onDatePickerDismiss = { showDatePicker = false },
        onTimePickerDismiss = { showTimePicker = false },
        onDateSelected = {
            dueDateMillis = it
            showDatePicker = false
            showTimePicker = true
        },
        onTimeSelected = {
            dueDateMillis = it
            showTimePicker = false
        })

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
                                            longitude = longitude,
                                            attachments = attachments
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
                                            longitude = longitude,
                                            attachments = attachments
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
                                                timeInMillis = task.dueDate!!
                                            )
                                        }

                                        geofenceHelper.removeGeofence(task.id)
                                        if (task.latitude != null && task.longitude != null && task.status != TaskStatus.COMPLETED) {
                                            geofenceHelper.addGeofence(
                                                task.id, task.latitude!!, task.longitude!!
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

            AttachmentSection(
                attachments = attachments, onAttachmentsChange = { attachments = it })

            LocationSection(
                latitude = latitude,
                longitude = longitude,
                cameraPositionState = cameraPositionState,
                onLocationChange = { lat, lng ->
                    latitude = lat
                    longitude = lng
                })

            RepeatSelector(
                selectedInterval = repeatInterval, onIntervalSelected = { repeatInterval = it })

            PrioritySelector(
                selectedPriority = priority, onPrioritySelected = { priority = it })

            CategorySelector(
                selectedCategory = category, onCategorySelected = { category = it })
        }
    }
}
