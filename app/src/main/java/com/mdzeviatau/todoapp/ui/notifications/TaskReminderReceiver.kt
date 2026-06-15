package com.mdzeviatau.todoapp.ui.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mdzeviatau.todoapp.TodoApplication
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as TodoApplication
        val notificationsEnabled = runBlocking {
            app.userPreferencesRepository.notificationsEnabledFlow.first()
        }

        if (!notificationsEnabled) return

        val taskId = intent.getStringExtra("TASK_ID") ?: return
        val title = intent.getStringExtra("TASK_TITLE") ?: "Task Reminder"
        val description = intent.getStringExtra("TASK_DESCRIPTION") ?: ""

        NotificationHelper.showNotification(context, taskId, title, description)
    }
}
