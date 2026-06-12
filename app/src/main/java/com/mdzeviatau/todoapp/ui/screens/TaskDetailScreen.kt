package com.mdzeviatau.todoapp.ui.screens

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil3.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
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

    var attachments by remember { mutableStateOf<List<String>>(emptyList()) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(), onResult = { uri ->
            if (uri != null) {
                try {
                    val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(uri, flag)
                } catch (e: Exception) {
                    Log.e("TaskDetailScreen", "Error persisting URI permission for photo: $uri", e)
                }
                attachments = attachments + uri.toString()
            }
        })

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(), onResult = { uri ->
            if (uri != null) {
                try {
                    val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(uri, flag)
                } catch (e: Exception) {
                    Log.e("TaskDetailScreen", "Error persisting URI permission for file: $uri", e)
                }
                attachments = attachments + uri.toString()
            }
        })

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
    var fullScreenImageUri by remember { mutableStateOf<String?>(null) }

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

    if (fullScreenImageUri != null) {
        Dialog(
            onDismissRequest = { fullScreenImageUri = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { fullScreenImageUri = null }) {
                AsyncImage(
                    model = fullScreenImageUri,
                    contentDescription = "Full Screen Attachment",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { fullScreenImageUri = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
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

            Text("Attachments", style = MaterialTheme.typography.labelLarge)
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (attachments.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(attachments) { uriString ->
                                val uri = Uri.parse(uriString)
                                AttachmentItem(uri = uri, onDelete = {
                                    attachments = attachments.filter { it != uriString }
                                }, onClick = {
                                    if (isImage(context, uri)) {
                                        fullScreenImageUri = uriString
                                    } else {
                                        openFile(context, uri)
                                    }
                                })
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }) {
                            Text("Add Photo")
                        }
                        Button(onClick = {
                            filePickerLauncher.launch("*/*")
                        }) {
                            Text("Add File")
                        }
                    }
                }
            }

            Text("Location", style = MaterialTheme.typography.labelLarge)
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    GoogleMap(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        cameraPositionState = cameraPositionState,
                        onMapClick = { latLng ->
                            latitude = latLng.latitude
                            longitude = latLng.longitude
                            scope.launch {
                                cameraPositionState.animate(CameraUpdateFactory.newLatLng(latLng))
                            }
                        },
                        onMapLongClick = { latLng ->
                            latitude = latLng.latitude
                            longitude = latLng.longitude
                            scope.launch {
                                cameraPositionState.animate(CameraUpdateFactory.newLatLng(latLng))
                            }
                        },
                        properties = MapProperties(
                            isMyLocationEnabled = ContextCompat.checkSelfPermission(
                                context, android.Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                        ),
                        uiSettings = remember {
                            MapUiSettings(
                                zoomControlsEnabled = false, scrollGesturesEnabled = true
                            )
                        }) {
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

@Composable
fun AttachmentItem(uri: Uri, onDelete: () -> Unit, onClick: () -> Unit) {
    val context = LocalContext.current
    val isImage = remember(uri) { isImage(context, uri) }
    val fileName = remember(uri) { getFileName(context, uri) }

    Box(modifier = Modifier.size(80.dp)) {
        if (isImage) {
            AsyncImage(
                model = uri,
                contentDescription = "Attachment preview",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(0.dp))
                    .clickable { onClick() },
                contentScale = ContentScale.Crop
            )
        } else {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onClick() },
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(0.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null)
                    Text(
                        text = fileName,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Delete attachment",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}
