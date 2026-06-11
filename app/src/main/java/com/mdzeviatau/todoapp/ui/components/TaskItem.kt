package com.mdzeviatau.todoapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.mdzeviatau.todoapp.data.models.task.Task
import com.mdzeviatau.todoapp.data.models.task.TaskStatus
import com.mdzeviatau.todoapp.ui.screens.capitalize
import com.mdzeviatau.todoapp.ui.screens.getCategoryIcon
import com.mdzeviatau.todoapp.ui.screens.getPriorityColor
import com.mdzeviatau.todoapp.ui.screens.getPriorityIcon

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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = getCategoryIcon(task.category),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = task.category.name.lowercase().capitalize(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = getPriorityIcon(task.priority),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = getPriorityColor(task.priority)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = task.priority.name.lowercase().capitalize(),
                            style = MaterialTheme.typography.bodySmall,
                            color = getPriorityColor(task.priority)
                        )
                    }
                }
            }
        }
    }
}
