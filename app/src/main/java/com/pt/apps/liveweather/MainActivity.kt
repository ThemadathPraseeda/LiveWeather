package com.pt.apps.liveweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.pt.apps.liveweather.data.api.client.ApiClient
import com.pt.apps.liveweather.data.api.repository.WeatherRepositoryImpl
import com.pt.apps.liveweather.data.local.AppDatabase.Companion.getDatabase
import com.pt.apps.liveweather.ui.screen.DashboardScreen
import com.pt.apps.liveweather.ui.theme.LiveWeatherTheme
import com.pt.apps.liveweather.ui.viewmodel.WeatherViewModel
import com.pt.apps.liveweather.utilities.LocationHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = getDatabase(applicationContext)
        val locationHelper = LocationHelper(applicationContext)
        val repository = WeatherRepositoryImpl(
            forecastApiService = ApiClient.forecastApiService,
            airQualityApi = ApiClient.airQualityApi,
            geocodingApi = ApiClient.geocodingApi,
            locationDao = database.locationDao()
        )

        val factory = WeatherViewModel.Factory(repository, locationHelper)
        val viewModel: WeatherViewModel by viewModels { factory }

        setContent {
            LiveWeatherTheme {
                DashboardScreen(viewmodel = viewModel)
                }

        }
    }
}


