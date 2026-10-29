package com.pt.apps.liveweather.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Query("SELECT * FROM saved_locations ORDER BY isFavorite DESC, updatedAt DESC")
    fun getAllSavedLocations(): Flow<List<SavedLocationEntity>>

    @Query("SELECT * FROM saved_locations WHERE id = :id LIMIT 1")
    suspend fun getLocationById(id: Long): SavedLocationEntity?

    @Query("SELECT * FROM saved_locations WHERE latitude = :lat AND longitude = :lon LIMIT 1")
    suspend fun findLocation(lat: Double, lon: Double): SavedLocationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: SavedLocationEntity): Long

    @Update
    suspend fun updateLocation(location: SavedLocationEntity)

    @Delete
    suspend fun deleteLocation(location: SavedLocationEntity)

    @Query("DELETE FROM saved_locations WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM saved_locations")
    suspend fun count(): Int
}