package com.mdzeviatau.todoapp.ui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mdzeviatau.todoapp.MainActivity

object NotificationHelper {
    const val CHANNEL_ID = "task_reminders_channel"
    const val CHANNEL_NAME = "Task Reminders"
    const val CHANNEL_DESCRIPTION = "Notifications for tasks"

    fun createNotificationChannel(context: Context) {
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
            description = CHANNEL_DESCRIPTION
            enableLights(true)
            enableVibration(true)
        }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun showNotification(context: Context, taskId: String, title: String, description: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("TASK_ID", taskId)
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, taskId.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle(title)
            .setContentText(description).setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent).setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)

        val notificationManager = NotificationManagerCompat.from(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(taskId.hashCode(), builder.build())
            }
        } else {
            notificationManager.notify(taskId.hashCode(), builder.build())
        }
    }
}
