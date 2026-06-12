package com.mdzeviatau.todoapp.ui.notifications

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class GeofenceHelper(val context: Context) {
    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    @SuppressLint("MissingPermission")
    fun addGeofence(taskId: String, lat: Double, lng: Double, radiusInMeters: Float = 200f) {
        val geofence = Geofence.Builder()
            .setRequestId(taskId)
            .setCircularRegion(lat, lng, radiusInMeters)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL)
            .setLoiteringDelay(30000)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER or GeofencingRequest.INITIAL_TRIGGER_DWELL)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
            addOnSuccessListener {
                Log.d("GeofenceHelper", "Successfully added geofence (200m) for task: $taskId")
            }
            addOnFailureListener {
                Log.e("GeofenceHelper", "Failed to add geofence for task: $taskId", it)
            }
        }
    }

    fun removeGeofence(taskId: String) {
        geofencingClient.removeGeofences(listOf(taskId)).run {
            addOnSuccessListener {
                Log.d("GeofenceHelper", "Successfully removed geofence for task: $taskId")
            }
            addOnFailureListener {
                Log.e("GeofenceHelper", "Failed to remove geofence for task: $taskId", it)
            }
        }
    }
}
