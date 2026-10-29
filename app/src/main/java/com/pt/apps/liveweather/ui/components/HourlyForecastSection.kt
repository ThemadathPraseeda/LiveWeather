package com.pt.apps.liveweather.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pt.apps.liveweather.domain.HourlyForecastItem
import com.pt.apps.liveweather.domain.TemperatureUnit
import com.pt.apps.liveweather.ui.theme.ArcticSkyBlue
import com.pt.apps.liveweather.ui.theme.CardBorderStroke
import com.pt.apps.liveweather.ui.theme.FrostWhite
import com.pt.apps.liveweather.ui.theme.MidnightSurface
import com.pt.apps.liveweather.ui.theme.SunGold

@Composable
fun HourlyForecastSection(
    hourly: List<HourlyForecastItem>,
    unit: TemperatureUnit,
    modifier: Modifier = Modifier
) {
    if (hourly.isEmpty()) return

    Column(
        modifier = modifier
            .testTag("hourly_forecast_section")
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MidnightSurface)
            .border(1.dp, CardBorderStroke, RoundedCornerShape(24.dp))
            .padding(vertical = 20.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Hourly Forecast",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.3.sp
                    ),
                    color = FrostWhite
                )
            }
            Text(
                text = "Next 24 Hours",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Minimalist Temperature Sparkline Curve
        TemperatureSparkline(
            temperatures = hourly.take(12).map { it.tempCelsius },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Hourly Items Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(hourly) { index, item ->
                HourlyItemCard(
                    item = item,
                    unit = unit,
                    isCurrent = index == 0
                )
            }
        }
    }
}

@Composable
private fun TemperatureSparkline(
    temperatures: List<Double>,
    modifier: Modifier = Modifier
) {
    if (temperatures.size < 2) return

    val minTemp = temperatures.minOrNull() ?: 0.0
    val maxTemp = temperatures.maxOrNull() ?: 1.0
    val tempRange = if (maxTemp - minTemp < 0.1) 1.0 else maxTemp - minTemp

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val stepX = width / (temperatures.size - 1)

        val path = Path()
        val fillPath = Path()

        temperatures.forEachIndexed { i, temp ->
            val x = i * stepX
            // Normalize y coordinate: higher temp = smaller y (higher on screen)
            val normalized = ((temp - minTemp) / tempRange).toFloat()
            val y = height - (normalized * (height - 16.dp.toPx()) + 8.dp.toPx())

            if (i == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                val prevX = (i - 1) * stepX
                val prevNormalized = ((temperatures[i - 1] - minTemp) / tempRange).toFloat()
                val prevY = height - (prevNormalized * (height - 16.dp.toPx()) + 8.dp.toPx())

                val cx = (prevX + x) / 2
                path.cubicTo(cx, prevY, cx, y, x, y)
                fillPath.cubicTo(cx, prevY, cx, y, x, y)
            }

            if (i == temperatures.size - 1) {
                fillPath.lineTo(x, height)
                fillPath.close()
            }
        }

        // Draw soft gradient under curve
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    ArcticSkyBlue.copy(alpha = 0.25f),
                    Color.Transparent
                )
            )
        )

        // Draw crisp line curve
        drawPath(
            path = path,
            color = ArcticSkyBlue,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw glowing point at first index
        val firstY = height - (((temperatures[0] - minTemp) / tempRange).toFloat() * (height - 16.dp.toPx()) + 8.dp.toPx())
        drawCircle(
            color = SunGold,
            radius = 4.dp.toPx(),
            center = Offset(0f, firstY)
        )
    }
}

@Composable
private fun HourlyItemCard(
    item: HourlyForecastItem,
    unit: TemperatureUnit,
    isCurrent: Boolean
) {
    val backgroundColor = if (isCurrent) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    }
    val borderColor = if (isCurrent) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    } else {
        CardBorderStroke.copy(alpha = 0.5f)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = item.timeLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Icon(
                imageVector = item.condition.icon,
                contentDescription = item.condition.title,
                tint = if (item.condition.code == 0 && item.hourOfDay in 6..19) SunGold else FrostWhite,
                modifier = Modifier.size(26.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = unit.format(item.tempCelsius),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = FrostWhite
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (item.precipitationProbability > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.WaterDrop,
                        contentDescription = "Rain",
                        tint = ArcticSkyBlue,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${item.precipitationProbability}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = ArcticSkyBlue
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}
