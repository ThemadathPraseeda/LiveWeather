package com.pt.apps.liveweather.ui.components



import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pt.apps.liveweather.ui.theme.ArcticSkyBlue
import com.pt.apps.liveweather.ui.theme.CardBorderStroke
import com.pt.apps.liveweather.ui.theme.MidnightSurface
import com.pt.apps.liveweather.ui.theme.QualityGood
import com.pt.apps.liveweather.ui.theme.QualityModerate
import com.pt.apps.liveweather.ui.theme.QualitySensitive
import com.pt.apps.liveweather.ui.theme.QualityUnhealthy
import com.pt.apps.liveweather.ui.theme.SunGold
import kotlin.math.roundToInt

import com.pt.apps.liveweather.domain.AirQualityInfo
import com.pt.apps.liveweather.domain.CurrentWeather
import com.pt.apps.liveweather.domain.TemperatureUnit
import com.pt.apps.liveweather.ui.theme.FrostWhite
import com.pt.apps.liveweather.ui.theme.SunsetOrange

@Composable
fun MetricsGrid(
    current: CurrentWeather,
    airQuality: AirQualityInfo?,
    unit: TemperatureUnit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .testTag("metrics_grid")
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Atmospheric Details",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.3.sp
            ),
            color = FrostWhite,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // Row 1: Wind & Humidity
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Air,
                iconTint = ArcticSkyBlue,
                title = "WIND",
                value = "${current.windSpeedKmh.roundToInt()} km/h",
                subtitle = "Direction ${current.windDirectionDegrees}°",
                extraContent = {
                    Icon(
                        imageVector = Icons.Filled.Navigation,
                        contentDescription = "Wind Direction",
                        tint = ArcticSkyBlue,
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(current.windDirectionDegrees.toFloat())
                    )
                }
            )

            val humidityDesc = when {
                current.humidityPercentage < 30 -> "Dry air"
                current.humidityPercentage <= 60 -> "Comfortable"
                else -> "High humidity"
            }
            MetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Opacity,
                iconTint = ArcticSkyBlue,
                title = "HUMIDITY",
                value = "${current.humidityPercentage}%",
                subtitle = humidityDesc
            )
        }

        // Row 2: UV Index & Air Quality
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val uvRisk = when {
                current.uvIndex <= 2 -> "Low"
                current.uvIndex <= 5 -> "Moderate"
                current.uvIndex <= 7 -> "High"
                current.uvIndex <= 10 -> "Very High"
                else -> "Extreme"
            }
            val uvColor = when {
                current.uvIndex <= 2 -> QualityGood
                current.uvIndex <= 5 -> QualityModerate
                current.uvIndex <= 7 -> QualitySensitive
                else -> QualityUnhealthy
            }

            MetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.WbSunny,
                iconTint = SunGold,
                title = "UV INDEX",
                value = "%.1f".format(current.uvIndex),
                subtitle = uvRisk,
                badgeColor = uvColor
            )

            val aqi = airQuality?.usAqi ?: 35
            val aqiStatus = airQuality?.status ?: "Good"
            val aqiColor = when {
                aqi <= 50 -> QualityGood
                aqi <= 100 -> QualityModerate
                aqi <= 150 -> QualitySensitive
                else -> QualityUnhealthy
            }

            MetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Thermostat,
                iconTint = aqiColor,
                title = "AIR QUALITY",
                value = "$aqi AQI",
                subtitle = aqiStatus,
                badgeColor = aqiColor
            )
        }

        // Row 3: Precipitation & Pressure
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.WaterDrop,
                iconTint = ArcticSkyBlue,
                title = "PRECIPITATION",
                value = "${current.precipitationProbability}%",
                subtitle = if (current.precipitationMm > 0) "%.1f mm recorded".format(current.precipitationMm) else "No rain expected"
            )

            MetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Compress,
                iconTint = ArcticSkyBlue,
                title = "PRESSURE",
                value = "${Math.round(current.pressureHpa)} hPa",
                subtitle = if (current.pressureHpa >= 1013) "Standard high" else "Low pressure"
            )
        }

        // Row 4: Sunrise & Sunset Banner Card
        SunTimelineCard(
            sunrise = current.sunriseTime,
            sunset = current.sunsetTime,
            isDay = current.isDay,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    badgeColor: Color? = null,
    extraContent: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MidnightSurface)
            .border(1.dp, CardBorderStroke, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                extraContent?.invoke()
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.5).sp
                ),
                color = FrostWhite
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (badgeColor != null) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(badgeColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SunTimelineCard(
    sunrise: String,
    sunset: String,
    isDay: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MidnightSurface)
            .border(1.dp, CardBorderStroke, RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sunrise
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SunGold.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.WbSunny,
                        contentDescription = "Sunrise",
                        tint = SunGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "SUNRISE",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = sunrise,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = FrostWhite
                    )
                }
            }

            // Divider Dot
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(CardBorderStroke)
            )

            // Sunset
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "SUNSET",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = sunset,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = FrostWhite
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SunsetOrange.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.WbTwilight,
                        contentDescription = "Sunset",
                        tint = SunsetOrange,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
