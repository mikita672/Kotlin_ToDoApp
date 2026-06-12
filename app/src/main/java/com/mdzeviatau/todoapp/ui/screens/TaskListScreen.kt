package com.mdzeviatau.todoapp.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mdzeviatau.todoapp.data.models.task.RepeatInterval
import com.mdzeviatau.todoapp.data.models.task.Task
import com.mdzeviatau.todoapp.data.models.task.TaskStatus
import com.mdzeviatau.todoapp.ui.components.TaskItem
import com.mdzeviatau.todoapp.ui.notifications.NotificationHelper
import com.mdzeviatau.todoapp.ui.viewmodel.TaskViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel,
    onAddTaskClick: () -> Unit,
    onTaskClick: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var sortOrder by remember { mutableStateOf(SortOrder.DATE_DESC) }
    var showSortMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val tabs = listOf("To Do", "Today", "Overdue", "Completed")

    val filteredAndSortedTasks = remember(tasks, selectedTabIndex, sortOrder, searchQuery) {
        val baseFiltered = when (selectedTabIndex) {
            0 -> tasks.filter { it.status != TaskStatus.COMPLETED }
            1 -> {
                val today = LocalDate.now()
                tasks.filter { task ->
                    task.dueDate?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault())
                            .toLocalDate() == today
                    } == true && task.status != TaskStatus.COMPLETED
                }
            }

            2 -> {
                val today = LocalDate.now()
                tasks.filter { task ->
                    task.dueDate?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                            .isBefore(today)
                    } == true && task.status != TaskStatus.COMPLETED
                }
            }

            3 -> tasks.filter { it.status == TaskStatus.COMPLETED }
            else -> tasks
        }

        val searchFiltered = if (searchQuery.isBlank()) {
            baseFiltered
        } else {
            val query = searchQuery.lowercase()
            baseFiltered.filter { task ->
                task.title.lowercase().contains(query) ||
                        task.description.lowercase().contains(query)
            }
        }

        when (sortOrder) {
            SortOrder.DATE_DESC -> searchFiltered.sortedByDescending { it.createdAt }
            SortOrder.DATE_ASC -> searchFiltered.sortedBy { it.createdAt }
            SortOrder.PRIORITY -> searchFiltered.sortedBy { it.priority }
            SortOrder.CATEGORY -> searchFiltered.sortedBy { it.category.name }
        }
    }

    fun handleTaskStatusChange(task: Task, isCompleted: Boolean) {
        val newStatus = if (isCompleted) TaskStatus.COMPLETED else TaskStatus.TODO
        val updatedTask = task.copy(status = newStatus)
        viewModel.updateTask(updatedTask)

        if (newStatus == TaskStatus.COMPLETED) {
            NotificationHelper.cancelNotification(context, task.id)

            if (task.repeatInterval != RepeatInterval.NONE && task.dueDate != null) {
                val nextDueDate = calculateNextDueDate(task.dueDate, task.repeatInterval)
                val nextTask = task.copy(
                    id = UUID.randomUUID().toString(),
                    status = TaskStatus.TODO,
                    dueDate = nextDueDate,
                    createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                )
                viewModel.addTask(nextTask)

                NotificationHelper.scheduleNotification(
                    context, nextTask.id, nextTask.title, nextTask.description, nextTask.dueDate!!
                )
            }
        } else if (task.dueDate != null) {
            NotificationHelper.scheduleNotification(
                context, task.id, task.title, task.description, task.dueDate
            )
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("My Tasks") },
                    actions = {
                        IconButton(onClick = {
                            isSearchActive = !isSearchActive
                            if (!isSearchActive) searchQuery = ""
                        }) {
                            Icon(
                                if (isSearchActive) Icons.Default.SearchOff else Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort tasks")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Newest First") },
                                onClick = {
                                    sortOrder = SortOrder.DATE_DESC
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Oldest First") },
                                onClick = {
                                    sortOrder = SortOrder.DATE_ASC
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sort by Priority") },
                                onClick = {
                                    sortOrder = SortOrder.PRIORITY
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sort by Category") },
                                onClick = {
                                    sortOrder = SortOrder.CATEGORY
                                    showSortMenu = false
                                }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                if (isSearchActive) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search tasks...") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            focusedTrailingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unfocusedTrailingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            focusedPlaceholderColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                            focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(0.dp)
                    )
                }
                SecondaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(text = title) }
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTaskClick) {
                Icon(Icons.Default.Add, contentDescription = "Add task")
            }
        }
    ) { innerPadding ->
        if (filteredAndSortedTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                val emptyMessage = when (selectedTabIndex) {
                    0 -> "No tasks to do!"
                    1 -> "No tasks for today."
                    2 -> "No overdue tasks."
                    3 -> "No completed tasks yet."
                    else -> "No tasks found."
                }
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredAndSortedTasks, key = { it.id }) { task ->
                    val isCompletedTab = selectedTabIndex == 3
                    val dismissState = rememberSwipeToDismissBoxState()

                    LaunchedEffect(dismissState.currentValue) {
                        when (dismissState.currentValue) {
                            SwipeToDismissBoxValue.StartToEnd -> {
                                handleTaskStatusChange(task, !isCompletedTab)
                                dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                            }

                            SwipeToDismissBoxValue.EndToStart -> {
                                viewModel.deleteTask(task)
                                NotificationHelper.cancelNotification(context, task.id)

                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Task deleted",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.addTask(task)
                                        if (task.dueDate != null && task.status != TaskStatus.COMPLETED) {
                                            NotificationHelper.scheduleNotification(
                                                context,
                                                task.id,
                                                task.title,
                                                task.description,
                                                task.dueDate
                                            )
                                        }
                                    }
                                }
                                dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                            }

                            SwipeToDismissBoxValue.Settled -> {}
                        }
                    }

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            val direction = dismissState.dismissDirection
                            val color by animateColorAsState(
                                when (dismissState.targetValue) {
                                    SwipeToDismissBoxValue.StartToEnd -> if (isCompletedTab) Color(
                                        0xFF2196F3
                                    ) else Color(0xFF4CAF50)

                                    SwipeToDismissBoxValue.EndToStart -> Color(0xFFF44336)
                                    else -> Color.Transparent
                                }, label = "background_color"
                            )
                            val alignment = when (direction) {
                                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                else -> Alignment.Center
                            }
                            val icon = when (direction) {
                                SwipeToDismissBoxValue.StartToEnd -> if (isCompletedTab) Icons.Default.RadioButtonUnchecked else Icons.Default.Check
                                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                                else -> null
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color, MaterialTheme.shapes.medium)
                                    .padding(horizontal = 20.dp),
                                contentAlignment = alignment
                            ) {
                                icon?.let {
                                    Icon(
                                        imageVector = it,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    ) {
                        TaskItem(
                            task = task,
                            onTaskClick = { onTaskClick(task.id) },
                            onStatusChange = { isCompleted ->
                                handleTaskStatusChange(task, isCompleted)
                            }
                        )
                    }
                }
            }
        }
    }
}
