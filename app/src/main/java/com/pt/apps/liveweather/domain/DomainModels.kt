package com.pt.apps.liveweather.domain

import kotlin.math.roundToInt

enum class TemperatureUnit(val symbol: String) {
    CELSIUS("°C"),
    FAHRENHEIT("°F");

fun convertFromCelsius(celsius: Double): Double {
    return when (this) {
        CELSIUS -> celsius
        FAHRENHEIT -> (celsius * 9.0 / 5.0) + 32.0
    }
}
    fun format(celsius: Double, includeSymbol: Boolean = true): String {
        val converted = convertFromCelsius(celsius)
        val rounded = converted.roundToInt()
        return if (includeSymbol) "$rounded°" else "$rounded"
    }

    fun formatWithUnit(celsius: Double): String {
        val converted = convertFromCelsius(celsius)
        val rounded = converted.roundToInt()
        return "$rounded$symbol"
    }
}


data class LocationItem(
    val id: Long = 0,
    val name: String = "",
    val region: String = "",
    val country: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isFavorite: Boolean = false,
    val isGpsLocation: Boolean = false
) {
    val displayName: String
        get() = if (region.isNotBlank() && region != name)
            "$name, $region" else if (country.isNotBlank())
                "$name, $country" else name
}

data class AirQualityInfo(
    val usAqi: Int,
    val status: String,
    val pm25: Double,
    val pm10: Double,
    val ozone: Double
) {
    companion object {
        fun create(usAqi: Int, pm25: Double = 0.0, pm10: Double = 0.0, ozone: Double = 0.0): AirQualityInfo {
            val status = when {
                usAqi <= 50 -> "Good"
                usAqi <= 100 -> "Moderate"
                usAqi <= 150 -> "Sensitive"
                usAqi <= 200 -> "Unhealthy"
                usAqi <= 300 -> "Very Unhealthy"
                else -> "Hazardous"
            }
            return AirQualityInfo(usAqi, status, pm25, pm10, ozone)
        }
    }
}

data class CurrentWeather(
    val tempCelsius: Double,
    val feelsLikeCelsius: Double,
    val minTempCelsius: Double,
    val maxTempCelsius: Double,
    val condition: WeatherCondition,
    val humidityPercentage: Int,
    val windSpeedKmh: Double,
    val windDirectionDegrees: Int,
    val uvIndex: Double,
    val pressureHpa: Double,
    val precipitationMm: Double,
    val precipitationProbability: Int,
    val isDay: Boolean,
    val sunriseTime: String,
    val sunsetTime: String,
    val updateTimeFormatted: String
)

data class HourlyForecastItem(
    val timeLabel: String,
    val hourOfDay: Int,
    val tempCelsius: Double,
    val precipitationProbability: Int,
    val condition: WeatherCondition
)

data class DailyForecastItem(
    val dayLabel: String,
    val dateFormatted: String,
    val minTempCelsius: Double,
    val maxTempCelsius: Double,
    val condition: WeatherCondition,
    val uvIndexMax: Double,
    val precipitationProbabilityMax: Int,
    val precipitationSumMm: Double
)

data class WeatherDashboardData(
    val location: LocationItem,
    val current: CurrentWeather,
    val hourly: List<HourlyForecastItem>,
    val daily: List<DailyForecastItem>,
    val airQuality: AirQualityInfo?
)
