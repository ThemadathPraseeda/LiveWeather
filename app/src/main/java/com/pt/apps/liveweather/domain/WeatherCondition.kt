package com.pt.apps.liveweather.domain

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

data class WeatherCondition(
    val code: Int,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isNight: Boolean = false
) {
    companion object {
        fun fromWmoCode(code: Int, isDay: Boolean = true): WeatherCondition {
            return when (code) {
                0 -> if (isDay) WeatherCondition(
                    code = 0,
                    title = "Clear Sky",
                    description = "Sunny and clear",
                    icon = Icons.Filled.WbSunny
                ) else WeatherCondition(
                    code = 0,
                    title = "Clear Night",
                    description = "Clear starlit sky",
                    icon = Icons.Filled.NightsStay,
                    isNight = true
                )
                1 -> if (isDay) WeatherCondition(
                    code = 1,
                    title = "Mainly Clear",
                    description = "Mostly sunny with a few passing clouds",
                    icon = Icons.Filled.WbSunny
                ) else WeatherCondition(
                    code = 1,
                    title = "Mainly Clear",
                    description = "Mostly clear night sky",
                    icon = Icons.Filled.NightsStay,
                    isNight = true
                )
                2 -> WeatherCondition(
                    code = 2,
                    title = "Partly Cloudy",
                    description = "Scattered clouds throughout the sky",
                    icon = Icons.Filled.CloudQueue,
                    isNight = !isDay
                )
                3 -> WeatherCondition(
                    code = 3,
                    title = "Overcast",
                    description = "Completely cloud-covered sky",
                    icon = Icons.Filled.Cloud,
                    isNight = !isDay
                )
                45, 48 -> WeatherCondition(
                    code = code,
                    title = "Foggy",
                    description = "Reduced visibility with dense mist",
                    icon = Icons.Filled.FilterDrama,
                    isNight = !isDay
                )
                51, 53, 55 -> WeatherCondition(
                    code = code,
                    title = "Drizzle",
                    description = "Light intermittent drizzle",
                    icon = Icons.Filled.Grain,
                    isNight = !isDay
                )
                61, 63, 65 -> WeatherCondition(
                    code = code,
                    title = "Rain",
                    description = if (code == 65) "Heavy rainfall" else "Steady rainfall",
                    icon = Icons.Filled.WaterDrop,
                    isNight = !isDay
                )
                71, 73, 75 -> WeatherCondition(
                    code = code,
                    title = "Snow",
                    description = if (code == 75) "Heavy snowfall" else "Gentle snowfall",
                    icon = Icons.Filled.Grain,
                    isNight = !isDay
                )
                77 -> WeatherCondition(
                    code = 77,
                    title = "Snow Grains",
                    description = "Fine granular snow",
                    icon = Icons.Filled.Grain,
                    isNight = !isDay
                )
                80, 81, 82 -> WeatherCondition(
                    code = code,
                    title = "Rain Showers",
                    description = "Brief localized rain showers",
                    icon = Icons.Filled.WaterDrop,
                    isNight = !isDay
                )
                85, 86 -> WeatherCondition(
                    code = code,
                    title = "Snow Showers",
                    description = "Cold winter snow flurries",
                    icon = Icons.Filled.Grain,
                    isNight = !isDay
                )
                95, 96, 99 -> WeatherCondition(
                    code = code,
                    title = "Thunderstorm",
                    description = "Severe electrical storm with lightning",
                    icon = Icons.Filled.Thunderstorm,
                    isNight = !isDay
                )
                else -> WeatherCondition(
                    code = code,
                    title = "Fair",
                    description = "Mild atmospheric conditions",
                    icon = if (isDay) Icons.Filled.WbSunny else Icons.Filled.NightsStay,
                    isNight = !isDay
                )
            }
        }
    }
}

