package com.csci448.cvonfeldt.cvonfeldt_a4.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date


@Entity(tableName = "weather_checkpoints")
data class WeatherCheckpoint(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val temperature: Double,
    val weatherDescription: String,
    val timestamp: Date = Date(),
    val isLocked: Boolean = false
)