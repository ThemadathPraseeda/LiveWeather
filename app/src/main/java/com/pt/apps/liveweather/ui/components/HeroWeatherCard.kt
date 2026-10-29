package com.pt.apps.liveweather.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pt.apps.liveweather.domain.CurrentWeather
import com.pt.apps.liveweather.domain.TemperatureUnit
import com.pt.apps.liveweather.ui.theme.CardBorderStroke
import com.pt.apps.liveweather.ui.theme.FrostWhite
import com.pt.apps.liveweather.ui.theme.SunGold

@Composable
fun HeroWeatherCard(
    current: CurrentWeather,
    unit: TemperatureUnit,
    modifier: Modifier = Modifier
) { val condition = current.condition
    val backgroundBrush = when {
        condition.code == 0 && current.isDay -> Brush.verticalGradient(
            colors = listOf(Color(0xFF1E3A8A).copy(alpha = 0.5f), Color(0xFF0F172A).copy(alpha = 0.8f))
        )
        condition.code in 1..3 && current.isDay -> Brush.verticalGradient(
            colors = listOf(Color(0xFF1E293B).copy(alpha = 0.6f), Color(0xFF090D16).copy(alpha = 0.9f))
        )
        condition.code in 51..82 -> Brush.verticalGradient(
            colors = listOf(Color(0xFF0F2B48).copy(alpha = 0.6f), Color(0xFF090D16).copy(alpha = 0.9f))
        )
        condition.code in 95..99 -> Brush.verticalGradient(
            colors = listOf(Color(0xFF2E1065).copy(alpha = 0.5f), Color(0xFF090D16).copy(alpha = 0.9f))
        )
        else -> Brush.verticalGradient(
            colors = listOf(Color(0xFF111726), Color(0xFF090D16))
        )
    }

    Box(
        modifier = modifier
            .testTag("hero_weather_card")
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(backgroundBrush)
            .border(1.dp, CardBorderStroke, RoundedCornerShape(28.dp))
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Weather icon with subtle glow styling
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = condition.icon,
                    contentDescription = condition.title,
                    tint = if (condition.code == 0 && current.isDay) SunGold else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Large Minimalist Temperature
            AnimatedContent(
                targetState = unit.format(current.tempCelsius, includeSymbol = true),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tempTransition"
            ) { tempText ->
                Text(
                    text = tempText,
                    fontSize = 68.sp,
                    lineHeight = 72.sp,
                    fontWeight = FontWeight.ExtraLight,
                    color = FrostWhite,
                    letterSpacing = (-2).sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Condition Title
            Text(
                text = condition.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                ),
                color = FrostWhite
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Condition description
            Text(
                text = condition.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(18.dp))

            // High / Low & Feels Like Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .border(0.8.dp, CardBorderStroke, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Feels like ${unit.format(current.feelsLikeCelsius)}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = FrostWhite
                )

                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "H: ${unit.format(current.maxTempCelsius)}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "L: ${unit.format(current.minTempCelsius)}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Updated time
            Text(
                text = "Updated ${current.updateTimeFormatted}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
