package com.csci448.cvonfeldt.cvonfeldt_a4.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

// utility class for handling location related operations and permissions
class LocationUtility(private val context: Context) {

    // StateFlows for observing location-related data
    private val mCurrentLocationStateFlow: MutableStateFlow<Location?> = MutableStateFlow(null)
    val currentLocationStateFlow: StateFlow<Location?>
        get() = mCurrentLocationStateFlow.asStateFlow()

    private val mCurrentAddressStateFlow: MutableStateFlow<String> = MutableStateFlow("")
    val currentAddressStateFlow: StateFlow<String>
        get() = mCurrentAddressStateFlow.asStateFlow()

    private val mIsLocationAvailableStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLocationAvailableStateFlow: StateFlow<Boolean>
        get() = mIsLocationAvailableStateFlow.asStateFlow()

    // Configure high-accuracy location request with 10-second interval
    private val locationRequest: LocationRequest = LocationRequest.Builder(10000L)  // 10 second interval
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .setMaxUpdates(1)
        .build()

    // callback to handle location updates
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                mCurrentLocationStateFlow.value = location
            }
        }
    }

    private val fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val geocoder = Geocoder(context, Locale.getDefault())

    fun checkPermissionAndGetLocation(
        activity: Activity,
        permissionLauncher: ActivityResultLauncher<Array<String>>
    ) {
        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,

                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) || ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            ) {
                Log.d("LocationUtility", "Permission was denied.")
                Toast.makeText(
                    context,
                    "We must access your location to plot where you are",
                    Toast.LENGTH_LONG
                ).show()
            }
            else {
                Log.d("LocationUtility", "Asking for permission.")
                permissionLauncher.launch(
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            return
        }
        Log.d("LocationUtility", "Permission has been granted.")
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        ).addOnSuccessListener {
            Log.d("LocationUtility", "Location updates requested successfully")
        }.addOnFailureListener { e ->
            Log.e("LocationUtility", "Failed to request location updates", e)
        }
    }

    fun setStartingLocation(location: Location?) {
        mCurrentLocationStateFlow.value = location
    }


    fun removeLocationRequest() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    suspend fun getAddress(location: Location?) {
        val addressTextBuilder = StringBuilder()
        if (location != null) {
            try {
                val addresses = withContext(Dispatchers.IO) {
                    geocoder.getFromLocation(location.latitude, location.longitude, 1)
                }

                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    for (i in 0..address.maxAddressLineIndex) {
                        if (i > 0) {
                            addressTextBuilder.append("\n")
                        }
                        addressTextBuilder.append(address.getAddressLine(i))
                    }
                }
                else {
                    addressTextBuilder.append("No address found")
                }
            } catch (e: IOException) {
                Log.e("LocationUtility", "Error getting address", e)
                addressTextBuilder.append("Unable to get address")
            }
        } else {
            addressTextBuilder.append("Location is null")
        }
        mCurrentAddressStateFlow.update { addressTextBuilder.toString() }
    }

    fun verifyLocationSettingsStates(states: LocationSettingsStates?) {
        mIsLocationAvailableStateFlow.update { states?.isLocationUsable ?: false }
    }

    fun checkIfLocationCanBeRetrieved(
        activity: Activity,
        locationLauncher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(activity)
        client.checkLocationSettings(builder.build()).apply {
            addOnSuccessListener { response ->
                verifyLocationSettingsStates(response.locationSettingsStates)
            }
            addOnFailureListener { exc ->
                mIsLocationAvailableStateFlow.update { false }
                if (exc is ResolvableApiException) {
                    locationLauncher
                        .launch(IntentSenderRequest.Builder(exc.resolution).build())
                }
            }
        }
    }
}
