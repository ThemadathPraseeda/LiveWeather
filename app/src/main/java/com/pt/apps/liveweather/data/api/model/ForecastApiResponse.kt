package com.pt.apps.liveweather.data.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class ForecastApiResponse(
    @Json(name = "latitude") val latitude: Double? = null,
    @Json(name = "longitude") val longitude: Double? = null,
    @Json(name = "timezone") val timezone: String? = null,
    @Json(name = "current") val current: CurrentDto? = null,
    @Json(name = "hourly") val hourly: HourlyDto? = null,
    @Json(name = "daily") val daily: DailyDto? = null
)

@JsonClass(generateAdapter = true)
data class CurrentDto(
    @Json(name = "time") val time: String? = null,
    @Json(name = "temperature_2m") val temperature2m: Double? = null,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: Int? = null,
    @Json(name = "apparent_temperature") val apparentTemperature: Double? = null,
    @Json(name = "is_day") val isDay: Int? = null,
    @Json(name = "precipitation") val precipitation: Double? = null,
    @Json(name = "weather_code") val weatherCode: Int? = null,
    @Json(name = "surface_pressure") val surfacePressure: Double? = null,
    @Json(name = "wind_speed_10m") val windSpeed10m: Double? = null,
    @Json(name = "wind_direction_10m") val windDirection10m: Int? = null
)

@JsonClass(generateAdapter = true)
data class HourlyDto(
    @Json(name = "time") val time: List<String>? = null,
    @Json(name = "temperature_2m") val temperature2m: List<Double>? = null,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: List<Int>? = null,
    @Json(name = "precipitation_probability") val precipitationProbability: List<Int>? = null,
    @Json(name = "weather_code") val weatherCode: List<Int>? = null,
    @Json(name = "wind_speed_10m") val windSpeed10m: List<Double>? = null
)

@JsonClass(generateAdapter = true)
data class DailyDto(
    @Json(name = "time") val time: List<String>? = null,
    @Json(name = "weather_code") val weatherCode: List<Int>? = null,
    @Json(name = "temperature_2m_max") val temperature2mMax: List<Double>? = null,
    @Json(name = "temperature_2m_min") val temperature2mMin: List<Double>? = null,
    @Json(name = "sunrise") val sunrise: List<String>? = null,
    @Json(name = "sunset") val sunset: List<String>? = null,
    @Json(name = "uv_index_max") val uvIndexMax: List<Double>? = null,
    @Json(name = "precipitation_sum") val precipitationSum: List<Double>? = null,
    @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Int>? = null
)

@JsonClass(generateAdapter = true)
data class AirQualityApiResponse(
    @Json(name = "current") val current: AirQualityCurrentDto? = null
)

@JsonClass(generateAdapter = true)
data class AirQualityCurrentDto(
    @Json(name = "time") val time: String? = null,
    @Json(name = "european_aqi") val europeanAqi: Int? = null,
    @Json(name = "us_aqi") val usAqi: Int? = null,
    @Json(name = "pm10") val pm10: Double? = null,
    @Json(name = "pm2_5") val pm25: Double? = null,
    @Json(name = "ozone") val ozone: Double? = null
)

@JsonClass(generateAdapter = true)
data class GeocodingApiResponse(
    @Json(name = "results") val results: List<GeocodingResultDto>? = null
)

@JsonClass(generateAdapter = true)
data class GeocodingResultDto(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "latitude") val latitude: Double? = null,
    @Json(name = "longitude") val longitude: Double? = null,
    @Json(name = "country") val country: String? = null,
    @Json(name = "admin1") val admin1: String? = null,
    @Json(name = "country_code") val countryCode: String? = null
)


