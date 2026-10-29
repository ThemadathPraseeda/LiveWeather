package com.pt.apps.liveweather.data.api.repository

import com.pt.apps.liveweather.data.api.client.ApiClient.airQualityApi
import com.pt.apps.liveweather.data.api.client.ApiClient.geocodingApi
import com.pt.apps.liveweather.data.api.service.AirQualityApiService
import com.pt.apps.liveweather.data.api.service.ForecastApiService
import com.pt.apps.liveweather.data.api.service.GeocodingApiService
import com.pt.apps.liveweather.data.local.LocationDao
import com.pt.apps.liveweather.data.local.SavedLocationEntity
import com.pt.apps.liveweather.domain.AirQualityInfo
import com.pt.apps.liveweather.domain.CurrentWeather
import com.pt.apps.liveweather.domain.DailyForecastItem
import com.pt.apps.liveweather.domain.HourlyForecastItem
import com.pt.apps.liveweather.domain.LocationItem
import com.pt.apps.liveweather.domain.WeatherCondition
import com.pt.apps.liveweather.domain.WeatherDashboardData
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.hours

interface WeatherRepository {
    suspend fun getLiveWeatherData(location: LocationItem): Result<WeatherDashboardData>
    suspend fun searchLocations(query: String): Result<List<LocationItem>>
    fun getSavedLocations(): Flow<List<LocationItem>>
    suspend fun saveLocation(location: LocationItem): Long
    suspend fun deleteSavedLocation(id: Long)
    suspend fun toggleFavorite(location: LocationItem): Boolean
    suspend fun seedInitialLocationsIfNeeded()
}
class WeatherRepositoryImpl(
    private val forecastApiService: ForecastApiService,
    private val airQualityApi: AirQualityApiService,
    private val geocodingApi: GeocodingApiService,
    private val locationDao: LocationDao
) : WeatherRepository {

    override suspend fun getLiveWeatherData(location: LocationItem): Result<WeatherDashboardData> {
        return try {
            coroutineScope {
                val forecastDeferred = async {
                    forecastApiService.getForecast(
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                }


                val airQualityDeferred = async {
                    try {
                        airQualityApi.getAirQuality(
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                    } catch (e: Exception) {
                        null
                    }
                }




                val forecastResponse = forecastDeferred.await()
                val aqiResponse = airQualityDeferred.await()
                val currentDto = forecastResponse.current
                    ?: throw IllegalStateException("Current weather data unavailable")

                val isDay = (currentDto.isDay ?: 1) == 1
                val condition = WeatherCondition.fromWmoCode(currentDto.weatherCode ?: 0, isDay)

                // Daily data parsing
                val dailyDto = forecastResponse.daily
                val dailyItems = mutableListOf<DailyForecastItem>()
                val daysCount = dailyDto?.time?.size ?: 0
                val dailyDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dayLabelFormat = SimpleDateFormat("EEE", Locale.getDefault())
                val displayDateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

                for (i in 0 until minOf(daysCount, 7)) {
                    val dateStr = dailyDto?.time?.getOrNull(i) ?: ""
                    var dayLabel = "Day ${i + 1}"
                    var dateFormatted = dateStr
                    try {
                        val parsed = dailyDateFormat.parse(dateStr)
                        if (parsed != null) {
                            dayLabel = if (i == 0) "Today" else dayLabelFormat.format(parsed)
                            dateFormatted = displayDateFormat.format(parsed)
                        }
                    } catch (_: Exception) { }

                    val dailyCondition = WeatherCondition.fromWmoCode(
                        dailyDto?.weatherCode?.getOrNull(i) ?: 0,
                        isDay = true
                    )

                    dailyItems.add(
                        DailyForecastItem(
                            dayLabel = dayLabel,
                            dateFormatted = dateFormatted,
                            minTempCelsius = dailyDto?.temperature2mMin?.getOrNull(i)
                                ?: ((currentDto.temperature2m ?: 15.0) - 4),
                            maxTempCelsius = dailyDto?.temperature2mMax?.getOrNull(i)
                                ?: ((currentDto.temperature2m ?: 15.0) + 4),
                            condition = dailyCondition,
                            uvIndexMax = dailyDto?.uvIndexMax?.getOrNull(i) ?: 3.0,
                            precipitationProbabilityMax = dailyDto?.precipitationProbabilityMax?.getOrNull(i) ?: 0,
                            precipitationSumMm = dailyDto?.precipitationSum?.getOrNull(i) ?: 0.0
                        )
                    )
                }

                // Min and max temp for today
                val todayMin = dailyItems.firstOrNull()?.minTempCelsius ?: ((currentDto.temperature2m ?: 15.0) - 3.0)
                val todayMax = dailyItems.firstOrNull()?.maxTempCelsius ?: ((currentDto.temperature2m ?: 15.0) + 3.0)

                // Sunrise & Sunset
                val sunriseRaw = dailyDto?.sunrise?.firstOrNull() ?: ""
                val sunsetRaw = dailyDto?.sunset?.firstOrNull() ?: ""
                val sunriseTime = formatTimeOnly(sunriseRaw, "06:45")
                val sunsetTime = formatTimeOnly(sunsetRaw, "18:30")

                // Hourly data parsing (take next 24 hours starting from current time)
                val hourlyDto = forecastResponse.hourly
                val hourlyItems = mutableListOf<HourlyForecastItem>()
                val hourlyCount = hourlyDto?.time?.size ?: 0
                val currentTimeStr = currentDto.time ?: ""

                // Find index matching current hour or start at current hour
                var startIndex = 0
                if (currentTimeStr.isNotBlank() && hourlyDto?.time != null) {
                    val idx = hourlyDto.time.indexOfFirst { it.startsWith(currentTimeStr.take(13)) }
                    if (idx >= 0) {
                        startIndex = idx
                    }
                }

                val hourlyDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
                val hourLabelFormat = SimpleDateFormat("h a", Locale.getDefault())

                for (i in startIndex until minOf(startIndex + 24, hourlyCount)) {
                    val timeStr = hourlyDto?.time?.getOrNull(i) ?: continue
                    var hourLabel = "Now"
                    var hourOfDay = 12
                    try {
                        val parsed = hourlyDateFormat.parse(timeStr)
                        if (parsed != null) {
                            hourOfDay = parsed.time.hours.inWholeHours.toInt()
                            hourLabel = if (i == startIndex) "Now" else hourLabelFormat.format(parsed)
                        }
                    } catch (_: Exception) { }

                    val isHourDay = hourOfDay in 6..19
                    val hCondition = WeatherCondition.fromWmoCode(
                        hourlyDto.weatherCode?.getOrNull(i) ?: 0,
                        isHourDay
                    )

                    hourlyItems.add(
                        HourlyForecastItem(
                            timeLabel = hourLabel,
                            hourOfDay = hourOfDay,
                            tempCelsius = hourlyDto.temperature2m?.getOrNull(i) ?: 15.0,
                            precipitationProbability = hourlyDto.precipitationProbability?.getOrNull(i) ?: 0,
                            condition = hCondition
                        )
                    )
                }

                // Air Quality
                val aqiCurrent = aqiResponse?.current
                val airQualityInfo = if (aqiCurrent?.usAqi != null) {
                    AirQualityInfo.create(
                        usAqi = aqiCurrent.usAqi,
                        pm25 = aqiCurrent.pm25 ?: 0.0,
                        pm10 = aqiCurrent.pm10 ?: 0.0,
                        ozone = aqiCurrent.ozone ?: 0.0
                    )
                } else {
                    AirQualityInfo.create(usAqi = 32)
                }

                val updateTimeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                val formattedUpdate = updateTimeFormat.format(Date())

                val current = CurrentWeather(
                    tempCelsius = currentDto.temperature2m ?: 18.0,
                    feelsLikeCelsius = currentDto.apparentTemperature ?: (currentDto.temperature2m
                        ?: 18.0),
                    minTempCelsius = todayMin,
                    maxTempCelsius = todayMax,
                    condition = condition,
                    humidityPercentage = currentDto.relativeHumidity2m ?: 50,
                    windSpeedKmh = currentDto.windSpeed10m ?: 10.0,
                    windDirectionDegrees = currentDto.windDirection10m ?: 0,
                    uvIndex = dailyItems.firstOrNull()?.uvIndexMax ?: 3.0,
                    pressureHpa = currentDto.surfacePressure ?: 1013.25,
                    precipitationMm = currentDto.precipitation ?: 0.0,
                    precipitationProbability = hourlyItems.firstOrNull()?.precipitationProbability
                        ?: 0,
                    isDay = isDay,
                    sunriseTime = sunriseTime,
                    sunsetTime = sunsetTime,
                    updateTimeFormatted = formattedUpdate
                )

                Result.success(
                    WeatherDashboardData(
                        location = location,
                        current = current,
                        hourly = hourlyItems,
                        daily = dailyItems,
                        airQuality = airQualityInfo
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchLocations(query: String): Result<List<LocationItem>> {
        return try {
            val trimmed = query.trim()
            if (trimmed.length < 2) {
                return Result.success(getPopularLocations().filter {
                    it.name.contains(trimmed, ignoreCase = true) || it.country.contains(trimmed, ignoreCase = true)
                })
            }
            val response = geocodingApi.searchCity(name = trimmed, count = 10)
            val results = response.results?.map { dto ->
                LocationItem(
                    id = dto.id ?: System.currentTimeMillis(),
                    name = dto.name ?: "Unknown",
                    region = dto.admin1 ?: "",
                    country = dto.country ?: "",
                    latitude = dto.latitude ?: 0.0,
                    longitude = dto.longitude ?: 0.0,
                    isFavorite = false
                )
            } ?: emptyList()

            Result.success(results)
        } catch (e: Exception) {
            // Fallback to local filter of popular locations
            val filtered = getPopularLocations().filter {
                it.name.contains(query, ignoreCase = true) || it.country.contains(query, ignoreCase = true)
            }
            if (filtered.isNotEmpty()) {
                Result.success(filtered)
            } else {
                Result.failure(e)
            }
        }
    }

    override fun getSavedLocations(): Flow<List<LocationItem>> {
        return locationDao.getAllSavedLocations().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun saveLocation(location: LocationItem): Long {
        val existing = locationDao.findLocation(location.latitude, location.longitude)
        return if (existing != null) {
            locationDao.updateLocation(existing.copy(isFavorite = true, updatedAt = System.currentTimeMillis()))
            existing.id
        } else {
            locationDao.insertLocation(
                SavedLocationEntity.fromDomain(location.copy(isFavorite = true))
            )
        }
    }

    override suspend fun deleteSavedLocation(id: Long) {
        locationDao.deleteById(id)
    }

    override suspend fun toggleFavorite(location: LocationItem): Boolean {
        val existing = locationDao.findLocation(location.latitude, location.longitude)
        return if (existing != null) {
            val newStatus = !existing.isFavorite
            locationDao.updateLocation(existing.copy(isFavorite = newStatus))
            newStatus
        } else {
            locationDao.insertLocation(
                SavedLocationEntity.fromDomain(location.copy(isFavorite = true))
            )
            true
        }
    }

    override suspend fun seedInitialLocationsIfNeeded() {
        if (locationDao.count() == 0) {
            val initial = listOf(
                LocationItem(
                    name = "San Francisco",
                    region = "California",
                    country = "United States",
                    latitude = 37.7749,
                    longitude = -122.4194,
                    isFavorite = true
                ),
                LocationItem(
                    name = "Tokyo",
                    region = "Tokyo",
                    country = "Japan",
                    latitude = 35.6762,
                    longitude = 139.6503,
                    isFavorite = true
                ),
                LocationItem(
                    name = "London",
                    region = "England",
                    country = "United Kingdom",
                    latitude = 51.5074,
                    longitude = -0.1278,
                    isFavorite = true
                )
            )
            initial.forEach { item ->
                locationDao.insertLocation(SavedLocationEntity.fromDomain(item))
            }
        }
    }

    private fun formatTimeOnly(rawDateTime: String, fallback: String): String {
        return try {
            if (rawDateTime.contains("T")) {
                val timePart = rawDateTime.substringAfter("T")
                val parts = timePart.split(":")
                val hour = parts[0].toInt()
                val min = parts.getOrNull(1) ?: "00"
                val amPm = if (hour >= 12) "PM" else "AM"
                val displayHour = when {
                    hour == 0 -> 12
                    hour > 12 -> hour - 12
                    else -> hour
                }
                "$displayHour:$min $amPm"
            } else {
                fallback
            }
        } catch (_: Exception) {
            fallback
        }
    }

    companion object {
        fun getPopularLocations(): List<LocationItem> = listOf(
            LocationItem(name = "San Francisco", region = "California", country = "United States", latitude = 37.7749, longitude = -122.4194),
            LocationItem(name = "New York", region = "New York", country = "United States", latitude = 40.7128, longitude = -74.0060),
            LocationItem(name = "Tokyo", region = "Tokyo", country = "Japan", latitude = 35.6762, longitude = 139.6503),
            LocationItem(name = "London", region = "England", country = "United Kingdom", latitude = 51.5074, longitude = -0.1278),
            LocationItem(name = "Paris", region = "Île-de-France", country = "France", latitude = 48.8566, longitude = 2.3522),
            LocationItem(name = "Sydney", region = "New South Wales", country = "Australia", latitude = -33.8688, longitude = 151.2093),
            LocationItem(name = "Singapore", region = "Singapore", country = "Singapore", latitude = 1.3521, longitude = 103.8198),
            LocationItem(name = "Dubai", region = "Dubai", country = "United Arab Emirates", latitude = 25.2048, longitude = 55.2708),
            LocationItem(name = "Reykjavik", region = "Capital Region", country = "Iceland", latitude = 64.1466, longitude = -21.9426),
            LocationItem(name = "Rio de Janeiro", region = "Rio de Janeiro", country = "Brazil", latitude = -22.9068, longitude = -43.1729)
        )
    }
}
