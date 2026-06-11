package com.mdzeviatau.todoapp.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mdzeviatau.todoapp.data.models.task.Task
import com.mdzeviatau.todoapp.data.models.task.TaskStatus
import com.mdzeviatau.todoapp.ui.viewmodel.TaskViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class SortOrder {
    DATE, PRIORITY, CATEGORY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel, onAddTaskClick: () -> Unit, onTaskClick: (String) -> Unit
) {
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var sortOrder by remember { mutableStateOf(SortOrder.DATE) }
    var showSortMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val tabs = listOf("To Do", "Today", "Overdue", "Completed")

    val filteredAndSortedTasks = remember(tasks, selectedTabIndex, sortOrder) {
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

        when (sortOrder) {
            SortOrder.DATE -> baseFiltered.sortedByDescending { it.createdAt }
            SortOrder.PRIORITY -> baseFiltered.sortedBy { it.priority }
            SortOrder.CATEGORY -> baseFiltered.sortedBy { it.category.name }
        }
    }

    Scaffold(topBar = {
        Column {
            TopAppBar(
                title = { Text("My Tasks") }, actions = {
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(Icons.Default.Sort, contentDescription = "Sort tasks")
                }
                DropdownMenu(
                    expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                    DropdownMenuItem(text = { Text("Sort by Date") }, onClick = {
                        sortOrder = SortOrder.DATE
                        showSortMenu = false
                    })
                    DropdownMenuItem(text = { Text("Sort by Priority") }, onClick = {
                        sortOrder = SortOrder.PRIORITY
                        showSortMenu = false
                    })
                    DropdownMenuItem(text = { Text("Sort by Category") }, onClick = {
                        sortOrder = SortOrder.CATEGORY
                        showSortMenu = false
                    })
                }
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
            )
            SecondaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = title) })
                }
            }
        }
    }, snackbarHost = { SnackbarHost(hostState = snackbarHostState) }, floatingActionButton = {
        FloatingActionButton(onClick = onAddTaskClick) {
            Icon(Icons.Default.Add, contentDescription = "Add task")
        }
    }) { innerPadding ->
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
                    text = emptyMessage, style = MaterialTheme.typography.bodyLarge
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
                    val dismissState = rememberSwipeToDismissBoxState()

                    LaunchedEffect(dismissState.currentValue) {
                        when (dismissState.currentValue) {
                            SwipeToDismissBoxValue.StartToEnd -> {
                                viewModel.updateTask(task.copy(status = TaskStatus.COMPLETED))
                                dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                            }

                            SwipeToDismissBoxValue.EndToStart -> {
                                viewModel.deleteTask(task)
                                val result = snackbarHostState.showSnackbar(
                                    message = "Task deleted", actionLabel = "Undo"
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.addTask(task)
                                }
                                dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                            }

                            SwipeToDismissBoxValue.Settled -> {}
                        }
                    }

                    SwipeToDismissBox(
                        state = dismissState, backgroundContent = {
                            val direction = dismissState.dismissDirection
                            val color by animateColorAsState(
                                when (dismissState.targetValue) {
                                    SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50)
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
                                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Check
                                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                                else -> null
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color, MaterialTheme.shapes.medium)
                                    .padding(horizontal = 20.dp), contentAlignment = alignment
                            ) {
                                icon?.let {
                                    Icon(
                                        imageVector = it,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }
                        }) {
                        TaskItem(
                            task = task,
                            onTaskClick = { onTaskClick(task.id) },
                            onStatusChange = { isCompleted ->
                                val newStatus =
                                    if (isCompleted) TaskStatus.COMPLETED else TaskStatus.TODO
                                viewModel.updateTask(task.copy(status = newStatus))
                            })
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task, onTaskClick: () -> Unit, onStatusChange: (Boolean) -> Unit
) {
    Card(
        onClick = onTaskClick, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
            containerColor = if (task.status == TaskStatus.COMPLETED) MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.5f
            )
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onStatusChange(task.status != TaskStatus.COMPLETED) }) {
                Icon(
                    imageVector = if (task.status == TaskStatus.COMPLETED) Icons.Default.CheckCircle
                    else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Change status",
                    tint = if (task.status == TaskStatus.COMPLETED) Color.Gray else MaterialTheme.colorScheme.primary
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.status == TaskStatus.COMPLETED) TextDecoration.LineThrough
                    else null
                )
                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }
            }

            val priorityColor = when (task.priority.name) {
                "HIGH" -> Color.Red
                "MEDIUM" -> Color(0xFFFFA500)
                else -> Color.Gray
            }

            Box(
                modifier = Modifier.size(12.dp)
            ) {
                Surface(
                    color = priorityColor,
                    shape = androidx.compose.foundation.shape.CircleShape,
                    modifier = Modifier.fillMaxSize()
                ) {}
            }
        }
    }
}
