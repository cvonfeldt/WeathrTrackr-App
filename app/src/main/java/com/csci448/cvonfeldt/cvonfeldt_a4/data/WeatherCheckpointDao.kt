package com.csci448.cvonfeldt.cvonfeldt_a4.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// Data Access Object interface for weather checkpoint database operations
@Dao
interface WeatherCheckpointDao {
    // retrieve all checkpoints ordered by most recent first
    @Query("SELECT * FROM weather_checkpoints ORDER BY timestamp DESC")
    fun getAllCheckpoints(): Flow<List<WeatherCheckpoint>>

    // add new checkpoint, replace if same ID exists
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckpoint(checkpoint: WeatherCheckpoint)

    // Update existing checkpoint in database
    @Update
    suspend fun updateCheckpoint(checkpoint: WeatherCheckpoint)

    // Remove specific checkpoint from database
    @Delete
    suspend fun deleteCheckpoint(checkpoint: WeatherCheckpoint)

    //Clear all checkpoints from database
    @Query("DELETE FROM weather_checkpoints")
    suspend fun deleteAllCheckpoints()
}