package com.mdzeviatau.todoapp.data.models.task

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val createdAt: String,
    val dueDate: Long? =null,
    val status: TaskStatus = TaskStatus.TODO,
    val priority: TaskPriority = TaskPriority.LOW,
    val category: TaskCategory = TaskCategory.OTHER,
    val reminderTime: Long?=null,
    val isRepeated: Boolean = false,
    val repeatInterval: RepeatInterval = RepeatInterval.NONE,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName : String? = null,
    val attachments: List<String> = emptyList()
)