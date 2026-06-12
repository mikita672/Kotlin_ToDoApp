package com.mdzeviatau.todoapp.ui.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

        if (geofencingEvent.hasError()) {
            Log.e("GeofenceReceiver", "GeofencingEvent error: ${geofencingEvent.errorCode}")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences ?: emptyList()

            for (geofence in triggeringGeofences) {
                val taskId = geofence.requestId
                NotificationHelper.showNotification(
                    context = context,
                    taskId = taskId,
                    title = "Task Location Reached",
                    description = "You are near the location of one of your tasks!"
                )
                Log.d("GeofenceReceiver", "Geofence triggered for task: $taskId")
            }
        }
    }
}
