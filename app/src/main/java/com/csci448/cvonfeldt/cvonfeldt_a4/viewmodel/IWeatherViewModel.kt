package com.csci448.cvonfeldt.cvonfeldt_a4.viewmodel

import com.csci448.cvonfeldt.cvonfeldt_a4.data.WeatherCheckpoint
import com.csci448.cvonfeldt.cvonfeldt_a4.data.WeatherResponse
import kotlinx.coroutines.flow.StateFlow
import com.google.android.gms.maps.model.LatLng

interface IWeatherViewModel {
    // Location state
    val currentLocation: StateFlow<LatLng?>
    suspend fun updateLocation(location: LatLng)

    // weather checkpoints
    val checkpoints: StateFlow<List<WeatherCheckpoint>>
    suspend fun addCheckpoint(checkpoint:WeatherCheckpoint)
    suspend fun deleteCheckpoint(checkpoint: WeatherCheckpoint)
    suspend fun deleteAllCheckpoints()
    suspend fun updateCheckpoint(checkpoint: WeatherCheckpoint)

    // Settings
    val saveToDatabase: StateFlow<Boolean>
    suspend fun toggleSaveToDatabase()

    // Weather data
    suspend fun fetchWeather(latitude: Double, longitude: Double): WeatherResponse?

    //address lookup
    suspend fun getAddressFromLocation(location: LatLng): String
}