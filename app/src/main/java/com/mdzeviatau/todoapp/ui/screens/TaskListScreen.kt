package com.mdzeviatau.todoapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mdzeviatau.todoapp.data.models.task.Task
import com.mdzeviatau.todoapp.data.models.task.TaskStatus
import com.mdzeviatau.todoapp.ui.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel, onAddTaskClick: () -> Unit, onTaskClick: (String) -> Unit
) {
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Moje Zadania") }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
    }, floatingActionButton = {
        FloatingActionButton(onClick = onAddTaskClick) {
            Icon(Icons.Default.Add, contentDescription = "Dodaj zadanie")
        }
    }) { innerPadding ->
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Brak zadań. Kliknij +, aby dodać pierwsze!")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
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
                    contentDescription = "Zmień status",
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
                modifier = Modifier
                    .size(12.dp)
                    .padding(2.dp)
            ) {
                Surface(
                    color = priorityColor, shape = androidx.compose.foundation.shape.CircleShape
                ) {
                    Spacer(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}