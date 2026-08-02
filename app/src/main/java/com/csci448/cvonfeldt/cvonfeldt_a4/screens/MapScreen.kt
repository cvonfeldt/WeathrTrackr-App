package com.csci448.cvonfeldt.cvonfeldt_a4.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.csci448.cvonfeldt.cvonfeldt_a4.viewmodel.IWeatherViewModel
import com.csci448.cvonfeldt.cvonfeldt_a4.data.WeatherCheckpoint
import com.google.android.gms.maps.CameraUpdateFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(  // main screen composable for displaying map and weather data
    modifier: Modifier = Modifier,
    weatherViewModel: IWeatherViewModel,
    coroutineScope: CoroutineScope
) {
    // Initialize context and location services needed for accessing device location
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()) }

    // Collect state flows from the ViewModel to observe data changes
    val checkpoints by weatherViewModel.checkpoints.collectAsState()
    val currentLocation by weatherViewModel.currentLocation.collectAsState()
    val saveToDatabase by weatherViewModel.saveToDatabase.collectAsState()

    var showLocationPermissionRequest by remember { mutableStateOf(true) }
    var showDeleteConfirmation by remember { mutableStateOf<WeatherCheckpoint?>(null)}
    var selectedMarker by remember { mutableStateOf<WeatherCheckpoint?>(null) }

    // Track all markers on the map including temp ones that aren't saved to database
    var allMarkers by remember { mutableStateOf(listOf<WeatherCheckpoint>()) }

    // set initial camera position to a default location before getting user location
    val cameraPositionState = rememberCameraPositionState {

        position = CameraPosition.fromLatLngZoom(
            LatLng(39.7555, -105.2211),
            15f
        )
    }

    // Request location and move camera to user's position when the screen is first displayed
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    coroutineScope.launch {


                        val latLng = LatLng(it.latitude,it.longitude)
                        weatherViewModel.updateLocation(latLng)
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(latLng, 15f),
                            1000
                        )
                    }
                }
            }
        }
    }

    // Show confirmation dialog when user attempts to delete a checkpoint
    if (showDeleteConfirmation != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = null},
            title = { Text("Delete Checkpoint")},
            text = { Text("Are you sure you want to delete this checkpoint?") },
            confirmButton = {
                TextButton(
                    onClick = {

                        showDeleteConfirmation?.let { checkpoint ->
                            coroutineScope.launch {
                                weatherViewModel.deleteCheckpoint(checkpoint)
                                snackbarHostState.showSnackbar(
                                    message = "Checkpoint deleted",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                        showDeleteConfirmation = null
                        selectedMarker = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    //setup location permission request launcher to handle permission result
    val locationPermissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) ==PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            coroutineScope.launch {
                                weatherViewModel.updateLocation(LatLng(it.latitude, it.longitude))
                            }
                        }
                    }
                }
            }
        }
        showLocationPermissionRequest = false
    }

    //trigger location permission request when needed
    LaunchedEffect(showLocationPermissionRequest) {

        if (showLocationPermissionRequest) {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    // Main UI container that holds the map and controls
    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = currentLocation!= null
            ),

            uiSettings = MapUiSettings(
                myLocationButtonEnabled = false,
                zoomControlsEnabled = false
            ),
            onMapClick = { selectedMarker =null }
        ) {
            // Display all checkpoints (saved and temporary) as markers on the map
            (checkpoints + allMarkers).forEach { checkpoint ->
                val position =LatLng(checkpoint.latitude, checkpoint.longitude)
                Marker(
                    state = MarkerState(position = position),
                    title = "${checkpoint.latitude}, ${checkpoint.longitude}",
                    onClick = {
                        selectedMarker = checkpoint
                        coroutineScope.launch {
                            val currentWeather = weatherViewModel.fetchWeather(
                                checkpoint.latitude,
                                checkpoint.longitude
                            )

                            val message = buildString {
                                append("Checked in: ${dateFormat.format(checkpoint.timestamp)}\n")
                                append("\nCurrent Conditions:\n")
                                append("Temperature: ${currentWeather?.temperature}°F\n")
                                append("Weather: ${currentWeather?.description}")
                            }
                            snackbarHostState.showSnackbar(
                                message = message,
                                actionLabel = "Delete",
                                duration = SnackbarDuration.Long,
                                withDismissAction = true
                            ).let { result ->
                                if (result == SnackbarResult.ActionPerformed) {
                                    showDeleteConfirmation = checkpoint
                                }
                            }
                        }
                        false
                    }
                )
            }
        }

        // Bottom UI conrtols for creating checkpoints and displaying notifications
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED) {
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            location?.let {
                                coroutineScope.launch {
                                    val latLng = LatLng(it.latitude, it.longitude)
                                    weatherViewModel.updateLocation(latLng)

                                    val weather =weatherViewModel.fetchWeather(
                                        it.latitude,
                                        it.longitude
                                    )
                                    val address = weatherViewModel.getAddressFromLocation(latLng)

                                    weather?.let { weatherData ->
                                        // Create new checkpoint with current location and weather data
                                        val checkpoint = WeatherCheckpoint(
                                            latitude = it.latitude,
                                            longitude = it.longitude,
                                            address = address,
                                            temperature = weatherData.temperature,
                                            weatherDescription = weatherData.description
                                        )

                                        // always show marker on map regardless of save setting
                                        allMarkers = allMarkers + checkpoint
                                        if (saveToDatabase) {
                                            weatherViewModel.addCheckpoint(checkpoint)
                                        }
                                        //shows check in time and weathe rinfo at check in time
                                        val message = buildString {
                                            append("Current Weather at $address\n")
                                            append("Temperature: ${weatherData.temperature}°F\n")
                                            append("Weather: ${weatherData.description}")

                                            if (!saveToDatabase) {
                                                append("\n(Save to Database is OFF)")
                                            }
                                        }

                                        // Display a toast notification with current weather information
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()

                                        // center map on new marker location
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngZoom(
                                                latLng,
                                                15f
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        showLocationPermissionRequest = true
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Check In at Current Location")
            }

            //snackbar host for displaying checkpoint information and delete option
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.fillMaxWidth()
            ) { data ->
                Snackbar(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    action = {
                        if (data.visuals.actionLabel != null) {
                            TextButton(onClick = { data.performAction() }) {
                                Text(data.visuals.actionLabel!!)
                            }
                        }
                    },
                    dismissAction = if (data.visuals.withDismissAction) {
                        {
                            IconButton(onClick = { data.dismiss() }) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss")
                            }
                        }
                    } else null
                ) {
                    Text(data.visuals.message)
                }
            }
        }
    }
}



