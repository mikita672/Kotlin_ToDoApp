package com.mdzeviatau.todoapp.ui.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("GeofenceReceiver", "onReceive triggered!")

        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: run {
            Log.e("GeofenceReceiver", "Received intent but GeofencingEvent is null")
            return
        }

        if (geofencingEvent.hasError()) {
            Log.e("GeofenceReceiver", "GeofencingEvent error code: ${geofencingEvent.errorCode}")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        Log.d("GeofenceReceiver", "Geofence transition detected: $geofenceTransition")

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL
        ) {

            val triggeringGeofences = geofencingEvent.triggeringGeofences ?: emptyList()

            Toast.makeText(context, "Geofence triggered!", Toast.LENGTH_SHORT).show()

            for (geofence in triggeringGeofences) {
                val taskId = geofence.requestId
                Log.d("GeofenceReceiver", "Triggered for taskId: $taskId")

                NotificationHelper.showNotification(
                    context = context,
                    taskId = taskId,
                    title = "Task Location Reached",
                    description = "You are near the location of your task!"
                )
            }
        }
    }
}
