package com.csci448.cvonfeldt.cvonfeldt_a4.data

import androidx.room.Entity

@Entity
data class WeatherResponse(
    val temperature: Double,
    val description: String,
    val humidity: Int,
    val windSpeed: Double,
    val pressure: Int
)