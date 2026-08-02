package com.csci448.cvonfeldt.cvonfeldt_a4.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
@Database(
    entities = [WeatherCheckpoint::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateTypeConverter::class)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun weatherCheckpointDao(): WeatherCheckpointDao
}