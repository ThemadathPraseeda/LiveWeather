package com.pt.apps.liveweather.ui.screen

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pt.apps.liveweather.domain.LocationItem
import com.pt.apps.liveweather.domain.TemperatureUnit
import com.pt.apps.liveweather.ui.components.HeroWeatherCard
import com.pt.apps.liveweather.ui.components.HourlyForecastSection
import com.pt.apps.liveweather.ui.components.MetricsGrid
import com.pt.apps.liveweather.ui.components.MinimalTopBar
import com.pt.apps.liveweather.ui.components.WeeklyForecastSection
import com.pt.apps.liveweather.ui.theme.FrostWhite
import com.pt.apps.liveweather.ui.theme.MidnightBackground
import com.pt.apps.liveweather.ui.theme.MidnightSurface
import com.pt.apps.liveweather.ui.viewmodel.WeatherUiState
import com.pt.apps.liveweather.ui.viewmodel.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DashboardScreen(
    viewmodel : WeatherViewModel,
    modifier: Modifier = Modifier.padding(all = 10.dp),
) {
    val uiState by viewmodel.uiState.collectAsState()
    val unit by viewmodel.unit.collectAsState()
    val searchState by viewmodel.searchState.collectAsState()
    val savedLocations by viewmodel.savedLocations.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var isSavedLocationsSheetOpen by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            viewmodel.requestGpsLocation()
        }
    }

    val currentLocation = when (val s = uiState) {
        is WeatherUiState.Success -> s.data.location
        is WeatherUiState.Error -> s.lastData?.location ?: LocationItem(name = "San Francisco", latitude = 37.77, longitude = -122.41)
        WeatherUiState.Loading -> LocationItem(name = "San Francisco", latitude = 37.77, longitude = -122.41)
    }

    val isRefreshing = (uiState as? WeatherUiState.Success)?.isRefreshing == true

    Scaffold(
        modifier = modifier
            .testTag("weather_dashboard_scaffold")
            .fillMaxSize(),
        containerColor = MidnightBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.TopCenter)
                    .widthIn(max = 680.dp)
                    .statusBarsPadding()
            ) {
                // Minimal Top Bar
                MinimalTopBar(
                    location = LocationItem(),
                    unit = TemperatureUnit.FAHRENHEIT,
                    isRefreshing = false,
                    onSearchClick = { },
                    onGpsClick = {

                    },
                    onFavoriteClick = { },
                    onSavedCitiesClick = { },
                    onUnitToggle = { },
                    onRefreshClick = { }
                )

                when (val state = uiState) {
                    is WeatherUiState.Loading -> {
                        LoadingStateView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                    }

                    is WeatherUiState.Error -> {
                        ErrorStateView(
                            message = state.message,
                            onRetry = { viewmodel.refresh() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                    }

                    is WeatherUiState.Success -> {
                        val data = state.data
                        LazyColumn(
                            modifier = Modifier
                                .testTag("weather_dashboard_scroll_list")
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 8.dp,
                                bottom = WindowInsets.navigationBars.asPaddingValues()
                                    .calculateBottomPadding() + 24.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Hero Weather Card
                            item {
                                HeroWeatherCard(
                                    current = data.current,
                                    unit = unit
                                )
                            }

                            // 24-Hour Forecast Row with Canvas Sparkline
                            item {
                                HourlyForecastSection(
                                    hourly = data.hourly,
                                    unit = unit
                                )
                            }

                            // Metrics Grid (Wind, Humidity, UV, Air Quality, Pressure, Sunrise/Sunset)
                            item {
                                MetricsGrid(
                                    current = data.current,
                                    airQuality = data.airQuality,
                                    unit = unit
                                )
                            }

                            // 7-Day Forecast Section
                            item {
                                WeeklyForecastSection(
                                    daily = data.daily,
                                    unit = unit
                                )
                            }
                        }
                    }
                }
            }
        }
    }

}
    @Composable
    private fun LoadingStateView(modifier: Modifier = Modifier) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Fetching live atmospheric data...",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.3.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
@Composable
private fun ErrorStateView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(MidnightSurface)
                .padding(28.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.WarningAmber,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Connection Notice",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = FrostWhite
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("retry_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Retry Live Sync")
            }
        }
    }
}