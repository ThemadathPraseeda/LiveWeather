package com.pt.apps.liveweather.data.api.service

import com.pt.apps.liveweather.data.api.model.AirQualityApiResponse
import com.pt.apps.liveweather.data.api.model.ForecastApiResponse
import com.pt.apps.liveweather.data.api.model.GeocodingApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ForecastApiService {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,weather_code,surface_pressure,wind_speed_10m,wind_direction_10m",
        @Query("hourly") hourly: String = "temperature_2m,relative_humidity_2m,precipitation_probability,weather_code,wind_speed_10m",
        @Query("daily") daily: String = "weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,uv_index_max,precipitation_sum,precipitation_probability_max",
        @Query("timezone") timezone: String = "auto"
    ): ForecastApiResponse
}

interface AirQualityApiService {

    @GET("v1/air-quality")
    suspend fun getAirQuality(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "european_aqi,us_aqi,pm10,pm2_5,ozone",
        @Query("timezone") timezone: String = "auto"
    ): AirQualityApiResponse
}

interface GeocodingApiService {

    @GET("v1/search")
    suspend fun searchCity(
        @Query("name") name: String,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json"
    ): GeocodingApiResponse
}


