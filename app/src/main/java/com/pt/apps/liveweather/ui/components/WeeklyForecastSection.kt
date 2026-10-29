package com.pt.apps.liveweather.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pt.apps.liveweather.domain.DailyForecastItem
import com.pt.apps.liveweather.domain.TemperatureUnit
import com.pt.apps.liveweather.ui.theme.ArcticSkyBlue
import com.pt.apps.liveweather.ui.theme.CardBorderStroke
import com.pt.apps.liveweather.ui.theme.FrostWhite
import com.pt.apps.liveweather.ui.theme.MidnightSurface
import com.pt.apps.liveweather.ui.theme.SunGold


@Composable
fun WeeklyForecastSection(
    daily: List<DailyForecastItem>,
    unit: TemperatureUnit,
    modifier: Modifier = Modifier
) {
    if (daily.isEmpty()) return

    val minOfAll = daily.minOfOrNull { it.minTempCelsius } ?: 0.0
    val maxOfAll = daily.maxOfOrNull { it.maxTempCelsius } ?: 30.0
    val totalRange = if (maxOfAll - minOfAll < 0.1) 1.0 else maxOfAll - minOfAll

    Column(
        modifier = modifier
            .testTag("weekly_forecast_section")
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MidnightSurface)
            .border(1.dp, CardBorderStroke, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        // Section Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "7-Day Forecast",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.3.sp
                ),
                color = FrostWhite
            )
        }

        daily.forEachIndexed { index, item ->
            DailyRow(
                item = item,
                unit = unit,
                minOfAll = minOfAll,
                totalRange = totalRange
            )
            if (index < daily.size - 1) {
                HorizontalDivider(
                    color = CardBorderStroke.copy(alpha = 0.5f),
                    thickness = 0.8.dp,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun DailyRow(
    item: DailyForecastItem,
    unit: TemperatureUnit,
    minOfAll: Double,
    totalRange: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Day Label & Date
        Column(modifier = Modifier.width(76.dp)) {
            Text(
                text = item.dayLabel,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = FrostWhite
            )
            Text(
                text = item.dateFormatted,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Weather Icon + Rain Prob
        Row(
            modifier = Modifier.width(64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.condition.icon,
                contentDescription = item.condition.title,
                tint = if (item.condition.code == 0) SunGold else FrostWhite,
                modifier = Modifier.size(22.dp)
            )
            if (item.precipitationProbabilityMax > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.WaterDrop,
                        contentDescription = "Rain",
                        tint = ArcticSkyBlue,
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = "${item.precipitationProbabilityMax}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = ArcticSkyBlue
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Min Temp
        Text(
            text = unit.format(item.minTempCelsius),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(36.dp)
        )

        // Temperature Bar Visualizer
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .padding(horizontal = 4.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            val startRatio = ((item.minTempCelsius - minOfAll) / totalRange).toFloat().coerceIn(0f, 1f)
            val endRatio = ((item.maxTempCelsius - minOfAll) / totalRange).toFloat().coerceIn(startRatio, 1f)
            val widthRatio = (endRatio - startRatio).coerceAtLeast(0.05f)

            Row(modifier = Modifier.fillMaxWidth()) {
                if (startRatio > 0.01f) {
                    Spacer(modifier = Modifier.weight(startRatio))
                }
                Box(
                    modifier = Modifier
                        .weight(widthRatio)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(ArcticSkyBlue, SunGold)
                            )
                        )
                )
                val remainingRatio = (1f - endRatio).coerceAtLeast(0f)
                if (remainingRatio > 0.01f) {
                    Spacer(modifier = Modifier.weight(remainingRatio))
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Max Temp
        Text(
            text = unit.format(item.maxTempCelsius),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = FrostWhite,
            modifier = Modifier.width(36.dp)
        )
    }
}
