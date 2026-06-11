package com.mdzeviatau.todoapp.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.mdzeviatau.todoapp.data.models.task.TaskCategory
import com.mdzeviatau.todoapp.data.models.task.TaskPriority

@Composable
fun getPriorityIcon(priority: TaskPriority): ImageVector = when (priority) {
    TaskPriority.HIGH -> Icons.Default.PriorityHigh
    TaskPriority.MEDIUM -> Icons.Default.HorizontalRule
    TaskPriority.LOW -> Icons.Default.KeyboardArrowDown
}

@Composable
fun getPriorityColor(priority: TaskPriority): Color = when (priority) {
    TaskPriority.HIGH -> Color(0xFFD32F2F)
    TaskPriority.MEDIUM -> Color(0xFFFBC02D)
    TaskPriority.LOW -> Color(0xFF388E3C)
}

@Composable
fun getCategoryIcon(category: TaskCategory): ImageVector = when (category) {
    TaskCategory.WORK -> Icons.Default.Work
    TaskCategory.PERSONAL -> Icons.Default.Person
    TaskCategory.SHOPPING -> Icons.Default.ShoppingCart
    TaskCategory.HEALTH -> Icons.Default.Favorite
    TaskCategory.FINANCE -> Icons.Default.Payments
    TaskCategory.EDUCATION -> Icons.Default.School
    TaskCategory.HOME -> Icons.Default.Home
    TaskCategory.HOBBY -> Icons.Default.Palette
    TaskCategory.OTHER -> Icons.Default.Category
}

fun String.capitalize() =
    this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
