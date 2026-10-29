package com.pt.apps.liveweather.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pt.apps.liveweather.domain.LocationItem

@Entity(tableName = "saved_locations")
data class SavedLocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val region: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val isFavorite: Boolean,
    val isGpsLocation: Boolean = false,
    val cachedTempCelsius: Double? = null,
    val cachedConditionCode: Int? = null,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): LocationItem {
        return LocationItem(
            id = id,
            name = name,
            region = region,
            country = country,
            latitude = latitude,
            longitude = longitude,
            isFavorite = isFavorite,
            isGpsLocation = isGpsLocation
        )
    }

    companion object {
        fun fromDomain(item: LocationItem): SavedLocationEntity {
            return SavedLocationEntity(
                id = item.id,
                name = item.name,
                region = item.region,
                country = item.country,
                latitude = item.latitude,
                longitude = item.longitude,
                isFavorite = item.isFavorite,
                isGpsLocation = item.isGpsLocation,
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}
