package com.example.v2.ui.home

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.v2.core.StateManager
import com.example.v2.core.WifeAssistantCore
import com.example.v2.core.WeatherUiState
import kotlinx.coroutines.launch

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val core = WifeAssistantCore.getInstance(application)
    private val weatherRepository = core.weatherRepository
    private val locationProvider = core.locationProvider

    init {
        refreshWeather()
    }

    fun refreshWeather() {
        viewModelScope.launch {
            StateManager.updateState { it.copy(weatherState = WeatherUiState.Loading) }

            // 1. Check Permission
            val hasPermission = ContextCompat.checkSelfPermission(
                getApplication(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                StateManager.updateState { it.copy(weatherState = WeatherUiState.PermissionRequired) }
                return@launch
            }

            // 2. Check Location Services
            if (!locationProvider.isLocationEnabled()) {
                StateManager.updateState { it.copy(weatherState = WeatherUiState.LocationServicesDisabled) }
                return@launch
            }

            // 3. Get Real Location
            val location = locationProvider.getCurrentLocation()
            if (location == null) {
                StateManager.updateState { it.copy(weatherState = WeatherUiState.LocationUnavailable) }
                return@launch
            }

            // 4. Fetch Real Weather
            val result = weatherRepository.fetchWeather(location.latitude, location.longitude)
            result.onSuccess { data ->
                StateManager.updateState { it.copy(weatherState = WeatherUiState.Success(data)) }
            }.onFailure { error ->
                if (error.message == "API_NOT_CONFIGURED") {
                    StateManager.updateState { it.copy(weatherState = WeatherUiState.ApiNotConfigured) }
                } else {
                    StateManager.updateState { it.copy(weatherState = WeatherUiState.Error(error.message ?: "Unknown Error")) }
                }
            }
        }
    }
}
