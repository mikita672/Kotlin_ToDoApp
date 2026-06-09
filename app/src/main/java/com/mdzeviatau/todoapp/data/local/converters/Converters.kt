package com.mdzeviatau.todoapp.data.local.converters

import androidx.room.TypeConverter
import com.mdzeviatau.todoapp.data.models.task.RepeatInterval
import com.mdzeviatau.todoapp.data.models.task.TaskCategory
import com.mdzeviatau.todoapp.data.models.task.TaskPriority
import com.mdzeviatau.todoapp.data.models.task.TaskStatus

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(separator = ",")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }


    @TypeConverter
    fun fromTaskStatus(value: TaskStatus): String = value.name

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus = TaskStatus.valueOf(value)

    @TypeConverter
    fun fromTaskPriority(value: TaskPriority): String = value.name

    @TypeConverter
    fun toTaskPriority(value: String): TaskPriority = TaskPriority.valueOf(value)

    @TypeConverter
    fun fromTaskCategory(value: TaskCategory): String = value.name

    @TypeConverter
    fun toTaskCategory(value: String): TaskCategory = TaskCategory.valueOf(value)

    @TypeConverter
    fun fromRecurringInterval(value: RepeatInterval): String = value.name

    @TypeConverter
    fun toRecurringInterval(value: String): RepeatInterval = RepeatInterval.valueOf(value)
}