package com.mdzeviatau.todoapp.ui.components

import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.core.net.toUri

@Composable
fun LocationSection(
    latitude: Double?,
    longitude: Double?,
    cameraPositionState: CameraPositionState,
    onLocationChange: (Double?, Double?) -> Unit,
    onMapTouched: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Text("Location", style = MaterialTheme.typography.labelLarge)
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event =
                                    awaitPointerEvent(androidx.compose.ui.input.pointer.PointerEventPass.Initial)
                                val isTouched = event.changes.any { it.pressed }
                                onMapTouched(isTouched)
                            }
                        }
                    }
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { latLng ->
                        onLocationChange(latLng.latitude, latLng.longitude)
                        scope.launch {
                            cameraPositionState.animate(CameraUpdateFactory.newLatLng(latLng))
                        }
                    },
                    onMapLongClick = { latLng ->
                        onLocationChange(latLng.latitude, latLng.longitude)
                        scope.launch {
                            cameraPositionState.animate(CameraUpdateFactory.newLatLng(latLng))
                        }
                    },
                    properties = MapProperties(
                        isMyLocationEnabled = ContextCompat.checkSelfPermission(
                            context, android.Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ),
                    uiSettings = remember {
                        MapUiSettings(
                            zoomControlsEnabled = false, scrollGesturesEnabled = true
                        )
                    }) {
                    if (latitude != null && longitude != null) {
                        Marker(
                            state = remember(latitude, longitude) {
                                MarkerState(position = LatLng(latitude, longitude))
                            }, title = "Task Location"
                        )
                    }
                }
            }
            if (latitude != null && longitude != null) {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = String.format(
                                Locale.ENGLISH, "%.4f, %.4f", latitude, longitude
                            ), style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Row {
                        TextButton(onClick = {
                            val gmmIntentUri =
                                "google.navigation:q=$latitude,$longitude".toUri()
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            context.startActivity(mapIntent)
                        }) {
                            Text("Navigate")
                        }
                        TextButton(onClick = {
                            onLocationChange(null, null)
                        }) {
                            Text("Clear")
                        }
                    }
                }

            } else {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Tap map to set location",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
