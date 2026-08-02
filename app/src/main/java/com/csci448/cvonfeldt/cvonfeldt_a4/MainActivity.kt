package com.csci448.cvonfeldt.cvonfeldt_a4

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import com.csci448.cvonfeldt.cvonfeldt_a4.presentation.navigation.WeatherNavHost
import com.csci448.cvonfeldt.cvonfeldt_a4.util.LocationUtility
import com.google.android.gms.location.LocationSettingsStates
import com.csci448.cvonfeldt.cvonfeldt_a4.data.WeatherDatabase
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.csci448.cvonfeldt.cvonfeldt_a4.ui.util.LocationAlarmReceiver
import com.csci448.cvonfeldt.cvonfeldt_a4.viewmodel.WeatherViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // core utility and component declarations
    private lateinit var locationUtility: LocationUtility
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var locationLauncher: ActivityResultLauncher<IntentSenderRequest>
    private val locationAlarmReceiver = LocationAlarmReceiver()
    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var database: WeatherDatabase
    private lateinit var weatherViewModel: WeatherViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //initialize Room database
        database = Room.databaseBuilder(
            applicationContext,
            WeatherDatabase::class.java,
            "weather_database"
        ).build()

        // Inititalize vm
        weatherViewModel = WeatherViewModel(application, database.weatherCheckpointDao())

        locationUtility = LocationUtility(this)

        notificationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->

            if (isGranted) {
                locationAlarmReceiver.checkPermissionAndScheduleAlarm(
                    this@MainActivity,
                    notificationPermissionLauncher
                )
            }
        }

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                when {
                    permissions.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                            permissions.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                        locationUtility.checkPermissionAndGetLocation(this@MainActivity, permissionLauncher)
                    }
                    else ->{
                        android.widget.Toast.makeText(
                            this,
                            "Location permissions are required for this feature",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

        locationLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {

                result.data?.let { data ->
                    val states = LocationSettingsStates.fromIntent(data)
                    locationUtility.verifyLocationSettingsStates(states)
                }
            }
        }

    // Initialize UI with material theme and nav
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                val coroutineScope =rememberCoroutineScope()


                WeatherNavHost(
                    navController = navController,
                    weatherViewModel = weatherViewModel,
                    context = this,
                    coroutineScope = coroutineScope
                )
            }
        }
    }

    // Lifecycle methods for location management
    override fun onStart() {
        super.onStart()
        locationUtility.checkIfLocationCanBeRetrieved(this, locationLauncher)
    }

    override fun onDestroy() {
        super.onDestroy()
        locationUtility.removeLocationRequest()
    }
}