package com.mdzeviatau.todoapp

import android.app.Application
import com.mdzeviatau.todoapp.data.local.AppDatabase
import com.mdzeviatau.todoapp.data.repository.TaskRepository
import com.mdzeviatau.todoapp.data.repository.UserPreferencesRepository
import com.mdzeviatau.todoapp.ui.notifications.NotificationHelper

class TodoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { TaskRepository(database.taskDao()) }
    val userPreferencesRepository by lazy { UserPreferencesRepository(this) }
}