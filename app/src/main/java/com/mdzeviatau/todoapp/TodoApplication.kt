package com.mdzeviatau.todoapp

import android.app.Application
import com.mdzeviatau.todoapp.data.local.AppDatabase
import com.mdzeviatau.todoapp.data.repository.TaskRepository

class TodoApplication : Application(){
    val database by lazy{ AppDatabase.getDatabase(this) }
    val repository by lazy { TaskRepository(database.taskDao()) }
}