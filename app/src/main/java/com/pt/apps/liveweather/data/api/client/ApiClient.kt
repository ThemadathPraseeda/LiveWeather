package com.pt.apps.liveweather.data.api.client

import com.pt.apps.liveweather.data.api.service.AirQualityApiService
import com.pt.apps.liveweather.data.api.service.ForecastApiService
import com.pt.apps.liveweather.data.api.service.GeocodingApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.getValue

object ApiClient {
    private val moshi : Moshi = Moshi
        .Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient : OkHttpClient = OkHttpClient
        .Builder()
        .connectTimeout(timeout = 15, unit = TimeUnit.SECONDS)
        .readTimeout(timeout = 15, unit = TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()


     val forecastApiService : ForecastApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ForecastApiService::class.java)
    }
    val airQualityApi: AirQualityApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://air-quality-api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AirQualityApiService::class.java)
    }

    val geocodingApi: GeocodingApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://geocoding-api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeocodingApiService::class.java)
    }

}