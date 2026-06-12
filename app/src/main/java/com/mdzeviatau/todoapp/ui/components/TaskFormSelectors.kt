package com.mdzeviatau.todoapp.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdzeviatau.todoapp.data.models.task.RepeatInterval
import com.mdzeviatau.todoapp.data.models.task.TaskCategory
import com.mdzeviatau.todoapp.data.models.task.TaskPriority
import com.mdzeviatau.todoapp.ui.screens.getCategoryIcon
import com.mdzeviatau.todoapp.ui.screens.getPriorityColor
import com.mdzeviatau.todoapp.ui.screens.getPriorityIcon

@Composable
fun PrioritySelector(
    selectedPriority: TaskPriority,
    onPrioritySelected: (TaskPriority) -> Unit
) {
    Text("Priority", style = MaterialTheme.typography.labelLarge)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TaskPriority.entries.forEach { p ->
            FilterChip(
                selected = selectedPriority == p,
                onClick = { onPrioritySelected(p) },
                label = { Text(p.name.lowercase().replaceFirstChar { it.uppercase() }) },
                leadingIcon = {
                    Icon(
                        imageVector = getPriorityIcon(p),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (selectedPriority == p) getPriorityColor(p) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                })
        }
    }
}

@Composable
fun CategorySelector(
    selectedCategory: TaskCategory,
    onCategorySelected: (TaskCategory) -> Unit
) {
    Text("Category", style = MaterialTheme.typography.labelLarge)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TaskCategory.entries.forEach { c ->
            FilterChip(
                selected = selectedCategory == c,
                onClick = { onCategorySelected(c) },
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

@Composable
fun RepeatSelector(
    selectedInterval: RepeatInterval,
    onIntervalSelected: (RepeatInterval) -> Unit
) {
    Text("Repeat", style = MaterialTheme.typography.labelLarge)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RepeatInterval.entries.forEach { interval ->
            FilterChip(
                selected = selectedInterval == interval,
                onClick = { onIntervalSelected(interval) },
                label = { Text(interval.name.lowercase().replaceFirstChar { it.uppercase() }) }
            )
        }
    }
}
