package com.mdzeviatau.todoapp.data.repository

import com.mdzeviatau.todoapp.data.local.dao.TaskDao
import com.mdzeviatau.todoapp.data.models.task.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId

class TaskRepository(private val taskDao: TaskDao) {
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    fun getTasksForToday(): Flow<List<Task>> {
        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay =
            today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

        return taskDao.getTasksForToday(startOfDay, endOfDay)
    }

    fun getOverdueTasks(): Flow<List<Task>> {
        val currentTime = System.currentTimeMillis()

        return taskDao.getOverdueTasks(currentTime)
    }

    suspend fun getTaskById(id: String): Task? {
        return taskDao.getTaskById(id)
    }

    suspend fun insertTask(task: Task) {
        taskDao.insertTask(task)
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }
}