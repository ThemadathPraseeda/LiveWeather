package com.pt.apps.liveweather.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pt.apps.liveweather.data.api.repository.WeatherRepository
import com.pt.apps.liveweather.data.api.repository.WeatherRepositoryImpl
import com.pt.apps.liveweather.domain.LocationItem
import com.pt.apps.liveweather.domain.TemperatureUnit
import com.pt.apps.liveweather.domain.WeatherDashboardData
import com.pt.apps.liveweather.utilities.LocationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data class Success(
        val data: WeatherDashboardData,
        val isRefreshing: Boolean = false
    ) : WeatherUiState
    data class Error(
        val message: String,
        val lastData: WeatherDashboardData? = null
    ) : WeatherUiState
}
data class SearchUiState(
    val query: String = "",
    val results: List<LocationItem> = emptyList(),
    val isSearching: Boolean = false,
    val isDialogOpen: Boolean = false
)

class WeatherViewModel (
    private val repository : WeatherRepository,
    private val locationHelper : LocationHelper
) : ViewModel() {
    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    //Temperature unit state
    private val _unit = MutableStateFlow(TemperatureUnit.CELSIUS)
    val unit: StateFlow<TemperatureUnit> = _unit.asStateFlow()

    //Local db search state
    private val _searchState = MutableStateFlow(SearchUiState())
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    val savedLocations: StateFlow<List<LocationItem>> = repository.getSavedLocations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),
            emptyList())

    private var currentLocation: LocationItem = LocationItem(
        name = "San Francisco",
        region = "California",
        country = "United States",
        latitude = 37.7749,
        longitude = -122.4194,
        isFavorite = true
    )

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            repository.seedInitialLocationsIfNeeded()
            // Check if GPS is available on launch
            tryFetchGpsOrDefault()
        }
    }

    private suspend fun tryFetchGpsOrDefault() {
        if (locationHelper.hasLocationPermission()) {
            val loc = locationHelper.getCurrentLocation()
            if (loc != null) {
                currentLocation = LocationItem(
                    name = "Current Location",
                    region = "",
                    country = "",
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    isGpsLocation = true
                )
            }
        }
        loadWeather(currentLocation)
    }

    fun loadWeather(location: LocationItem, isRefresh: Boolean = false) {
        currentLocation = location
        viewModelScope.launch {
            val previousData = when (val state = _uiState.value) {
                is WeatherUiState.Success -> state.data
                is WeatherUiState.Error -> state.lastData
                else -> null
            }

            if (isRefresh && previousData != null) {
                _uiState.update { WeatherUiState.Success(data = previousData, isRefreshing = true) }
            } else if (previousData == null) {
                _uiState.update { WeatherUiState.Loading }
            }

            val result = repository.getLiveWeatherData(location)
            result.fold(
                onSuccess = { data ->
                    // Check if this location is marked favorite in saved list
                    val isFav = savedLocations.value.any {
                        abs(it.latitude - location.latitude) < 0.05 &&
                                abs(it.longitude - location.longitude) < 0.05
                    }
                    val updatedData = data.copy(
                        location = data.location.copy(isFavorite = isFav)
                    )
                    _uiState.update { WeatherUiState.Success(data = updatedData, isRefreshing = false) }
                },
                onFailure = { error ->
                    _uiState.update {
                        WeatherUiState.Error(
                            message = error.localizedMessage ?: "Failed to fetch live weather data. Tap retry to check again.",
                            lastData = previousData
                        )
                    }
                }
            )
        }
    }

    fun refresh() {
        loadWeather(currentLocation, isRefresh = true)
    }

    fun toggleUnit() {
        _unit.update {
            if (it == TemperatureUnit.CELSIUS) TemperatureUnit.FAHRENHEIT else TemperatureUnit.CELSIUS
        }
    }

    fun openSearch() {
        _searchState.update {
            it.copy(
                isDialogOpen = true,
                query = "",
                results = WeatherRepositoryImpl.getPopularLocations()
            )
        }
    }

    fun closeSearch() {
        _searchState.update { it.copy(isDialogOpen = false) }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchState.update { it.copy(query = newQuery) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // Debounce
            _searchState.update { it.copy(isSearching = true) }
            val result = repository.searchLocations(newQuery)
            val locations = result.getOrElse { WeatherRepositoryImpl.getPopularLocations() }
            _searchState.update { it.copy(results = locations, isSearching = false) }
        }
    }

    fun selectLocation(location: LocationItem) {
        closeSearch()
        loadWeather(location)
    }

    fun toggleFavoriteCurrent() {
        viewModelScope.launch {
            val isNowFav = repository.toggleFavorite(currentLocation)
            val currentData = (_uiState.value as? WeatherUiState.Success)?.data ?: return@launch
            _uiState.update {
                WeatherUiState.Success(
                    data = currentData.copy(
                        location = currentData.location.copy(isFavorite = isNowFav)
                    )
                )
            }
        }
    }

    fun deleteSavedLocation(id: Long) {
        viewModelScope.launch {
            repository.deleteSavedLocation(id)
        }
    }

    fun requestGpsLocation() {
        viewModelScope.launch {
            _uiState.update { WeatherUiState.Loading }
            val loc = locationHelper.getCurrentLocation()
            if (loc != null) {
                val gpsLoc = LocationItem(
                    name = "Current Location",
                    region = "Local Coordinates",
                    country = "",
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    isGpsLocation = true
                )
                currentLocation = gpsLoc
                loadWeather(gpsLoc)
            } else {
                // If null, reload current location
                loadWeather(currentLocation)
            }
        }
    }

    class Factory(
        private val repository: WeatherRepository,
        private val locationHelper: LocationHelper
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WeatherViewModel(repository, locationHelper) as T
        }
    }
}