package com.csci448.cvonfeldt.cvonfeldt_A4.ui

import android.location.Location
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

// Screen that displays current location information and map interface
@Composable
fun LocationScreen(
    modifier: Modifier = Modifier,
    location: Location?,
    locationAvailable: Boolean,
    onGetLocation: () -> Unit,
    onNotify: (Location) -> Unit = {},
    address: String
) {
    // Track map state and camera position
    val mapReadyState = remember { mutableStateOf(false) }
    val cameraPositionState = rememberCameraPositionState()

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display location coords and address
        Text(text = "Latitude / Longitude")
        Text(text = "Lat: ${location?.latitude ?: "N/A"}, Lon: ${location?.longitude ?: "N/A"}")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Address")
        Text(text = address)
        Spacer(modifier = Modifier.height(16.dp))

        // location control buttons
        Button(
            enabled = locationAvailable,
            onClick = onGetLocation
        ) {
            Text(text = "Get Current Location")
        }

        Button(
            enabled = (location != null),
            onClick = { location?.let { onNotify(it) } }
        ) {
            Text(text = "Notify Me Later")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // google maps component with current location marker
        GoogleMap(
            modifier = Modifier.weight(1f),
            cameraPositionState = cameraPositionState,
            onMapLoaded = { mapReadyState.value =true }
        ) {
            location?.let { loc ->
                Marker(
                    state = MarkerState(
                        position = LatLng(loc.latitude, loc.longitude)
                    ),
                    title = "Current Location",
                    snippet = address
                )
            }
        }

        //animate camera to current location when available
        LaunchedEffect(location, mapReadyState.value) {
            if (location != null && mapReadyState.value) {
                val cameraPosition = CameraPosition.Builder()
                    .target(LatLng(location.latitude,location.longitude))
                    .zoom(15f)
                    .build()
                cameraPositionState.animate(
                    CameraUpdateFactory.newCameraPosition(cameraPosition),
                    1000
                )
            }
        }
    }
}

// preview composable for development and testing
@Preview(showBackground = true)
@Composable
private fun PreviewLocationScreen() {
    val locationState = remember { mutableStateOf<Location?>(null) }
    val addressState = remember { mutableStateOf("") }
    LocationScreen(
        location = locationState.value,
        locationAvailable = true,
        onGetLocation = {
            locationState.value = Location("").apply {
                latitude = 1.35
                longitude = 103.87
            }
            addressState.value = "Singapore"
        },
        onNotify = {},
        address = addressState.value
    )
}