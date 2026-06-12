package com.mdzeviatau.todoapp.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.mdzeviatau.todoapp.data.models.task.RepeatInterval
import com.mdzeviatau.todoapp.data.models.task.TaskCategory
import com.mdzeviatau.todoapp.data.models.task.TaskPriority
import java.time.Instant
import java.time.ZoneId

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

fun calculateNextDueDate(currentDate: Long, interval: RepeatInterval): Long {
    val localDateTime =
        Instant.ofEpochMilli(currentDate).atZone(ZoneId.systemDefault()).toLocalDateTime()
    val nextDateTime = when (interval) {
        RepeatInterval.DAILY -> localDateTime.plusDays(1)
        RepeatInterval.WEEKLY -> localDateTime.plusWeeks(1)
        RepeatInterval.MONTHLY -> localDateTime.plusMonths(1)
        RepeatInterval.NONE -> localDateTime
    }
    return nextDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

fun String.capitalize() =
    this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

fun getFileName(context: Context, uri: Uri): String {
    var name = "Unknown"
    try {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
            }
        }
    } catch (e: Exception) {
        Log.e("TaskUiUtils", "Error getting file name for URI: $uri", e)
        name = uri.lastPathSegment ?: "Unknown"
    }
    return name
}

fun isImage(context: Context, uri: Uri): Boolean {
    val mimeType = context.contentResolver.getType(uri)
    return mimeType?.startsWith("image/") == true
}

fun openFile(context: Context, uri: Uri) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, context.contentResolver.getType(uri))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Open with"))
    } catch (e: Exception) {
        Log.e("TaskUiUtils", "Error opening file: $uri", e)
    }
}
