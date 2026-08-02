package com.csci448.cvonfeldt.cvonfeldt_a4.viewmodel

import android.app.Application
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.csci448.cvonfeldt.cvonfeldt_a4.data.WeatherCheckpoint
import com.csci448.cvonfeldt.cvonfeldt_a4.data.WeatherCheckpointDao
import com.csci448.cvonfeldt.cvonfeldt_a4.data.WeatherResponse
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

// Implements weather tracking functionality with OpenWeatherMap API integration
class WeatherViewModel(
    application: Application,
    private val weatherCheckpointDao: WeatherCheckpointDao
) : AndroidViewModel(application), IWeatherViewModel {

    //OpenWeatherMap API configuration
    private val API_KEY = "e52bbd6c51ea99ed6837a07b671848df"
    private val WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather"

    // state management using Kotlin Flow
    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    override val currentLocation: StateFlow<LatLng?> = _currentLocation.asStateFlow()

    override val checkpoints: StateFlow<List<WeatherCheckpoint>> =
        weatherCheckpointDao.getAllCheckpoints()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _saveToDatabase = MutableStateFlow(true)
    override val saveToDatabase: StateFlow<Boolean> = _saveToDatabase.asStateFlow()

    // Basic state management functions
    override suspend fun toggleSaveToDatabase() {
        _saveToDatabase.value = !_saveToDatabase.value
    }


    override suspend fun updateLocation(location: LatLng) {
        _currentLocation.value =location
    }

    // database operations for weather checkpoints
    override suspend fun addCheckpoint(checkpoint: WeatherCheckpoint) {
        if (saveToDatabase.value) {
            weatherCheckpointDao.insertCheckpoint(checkpoint)
        }
    }

    override suspend fun deleteCheckpoint(checkpoint: WeatherCheckpoint) {
        weatherCheckpointDao.deleteCheckpoint(checkpoint)
    }

    override suspend fun deleteAllCheckpoints() {
        weatherCheckpointDao.deleteAllCheckpoints()
    }

    override suspend fun updateCheckpoint(checkpoint: WeatherCheckpoint) {
        if (saveToDatabase.value) {

            weatherCheckpointDao.updateCheckpoint(checkpoint)
        }
    }

    // Fetches weather data from OpenWeatherMap API with error handling
    override suspend fun fetchWeather(latitude: Double, longitude: Double): WeatherResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val formattedLat = String.format(Locale.US, "%.4f", latitude)
                val formattedLon = String.format(Locale.US, "%.4f", longitude)
                val urlString = "$WEATHER_API_URL?lat=$formattedLat&lon=$formattedLon&appid=$API_KEY&units=imperial"
                Log.d("WeatherViewModel", "Fetching weather from: $urlString")

                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestProperty("User-Agent", "Weather App")

                try {
                    val responseCode = connection.responseCode
                    Log.d("WeatherViewModel", "Response Code: $responseCode")

                    val response = try {
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            connection.inputStream.bufferedReader().use { it.readText() }
                        } else {
                            connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error message available"
                        }
                    } catch (e: Exception) {
                        Log.e("WeatherViewModel", "Error reading response: ${e.message}", e)
                        "Error reading response: ${e.message}"
                    }

                    Log.d("WeatherViewModel", "Response: $response")

                    when (responseCode) {
                        HttpURLConnection.HTTP_OK -> {
                            val jsonResponse = JSONObject(response)
                            Log.d("WeatherViewModel", "Parsed JSON successfully")

                            val main = jsonResponse.getJSONObject("main")
                            val weather = jsonResponse.getJSONArray("weather").getJSONObject(0)
                            val wind = jsonResponse.getJSONObject("wind")

                            WeatherResponse(
                                temperature = main.getDouble("temp"),
                                description = weather.getString("description"),
                                humidity = main.getInt("humidity"),
                                windSpeed = wind.getDouble("speed"),
                                pressure = main.getInt("pressure")
                            ).also {
                                Log.d("WeatherViewModel", "Successfully created WeatherResponse: temp=${it.temperature}°F, desc=${it.description}")
                            }
                        }
                        HttpURLConnection.HTTP_UNAUTHORIZED -> {
                            Log.e("WeatherViewModel", "API Key unauthorized. Please check your API key or wait for it to activate.")
                            WeatherResponse(
                                temperature = 70.0,
                                description = "API Key Error - Please check configuration",
                                humidity = 60,
                                windSpeed = 5.0,
                                pressure = 1013
                            )
                        }
                        else -> {
                            Log.e("WeatherViewModel", "Unexpected response code: $responseCode")
                            WeatherResponse(
                                temperature = 70.0,
                                description = "Error: HTTP $responseCode",
                                humidity = 60,
                                windSpeed = 5.0,
                                pressure = 1013
                            )
                        }
                    }
                } finally {
                    connection.disconnect()
                }
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Error fetching weather: ${e.message}", e)
                WeatherResponse(
                    temperature = 70.0,
                    description = "Error: ${e.message ?: "Unknown error"}",
                    humidity = 60,
                    windSpeed = 5.0,
                    pressure = 1013
                )
            }
        }
    }

    override suspend fun getAddressFromLocation(location: LatLng): String {
        val geocoder = Geocoder(getApplication(), Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (addresses?.isNotEmpty() == true) {
                addresses[0]?.getAddressLine(0) ?: "${location.latitude}, ${location.longitude}"
            } else {
                "${location.latitude}, ${location.longitude}"
            }
        } catch (e: Exception) {
            "${location.latitude}, ${location.longitude}"
        }
    }
}