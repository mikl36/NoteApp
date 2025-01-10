package com.example.harkkat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.harkkat.WeatherState
import com.example.harkkat.database.LastCity
import com.example.harkkat.repository.LastCityRepository
import com.example.harkkat.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


// Hilt dependency injection to be considered
class WeatherViewModel(
    private val weatherRepository: WeatherRepository,
    private val lastCityRepository: LastCityRepository
) : ViewModel() {

    // StateFlow to hold the current weather state
    private val _weatherState = MutableStateFlow(WeatherState(isLoading = true))
    val weatherState: StateFlow<WeatherState> = _weatherState

    // StateFlow to hold the last searched city
    private val _lastCity = MutableStateFlow<LastCity?>(null)
    val lastCity: StateFlow<LastCity?> = _lastCity

    // Initialize the ViewModel
    init {
        // Launch a coroutine to collect the last searched city from the repository
        viewModelScope.launch {
            lastCityRepository.lastCity.collect {
                _lastCity.value = it
            }
        }
    }

    // Fetch weather data for a given city (user input)
    // HttpException, IOException, Exception
    fun fetchWeather(city: String, apiKey: String, languageCode: String) {
        viewModelScope.launch {
            try {
                // Perform network request on IO dispatcher
                val response = withContext(Dispatchers.IO) {
                    weatherRepository.getWeather(city, apiKey, languageCode)
                }
                // Update weather state with the response
                _weatherState.value = _weatherState.value.copy(
                    weather = response,
                    isLoading = false,
                    isError = false
                )
                // Save the last searched city
                lastCityRepository.insert(LastCity(city = city))
            } catch (e: Exception) {
                // Handle exceptions and update weather state to indicate an error, general
                _weatherState.value = _weatherState.value.copy(
                    isLoading = false,
                    isError = true
                )
            }
        }
    }
}

// // Factory for creating instances of ViewModel
class WeatherViewModelFactory(
    private val weatherRepository: WeatherRepository,
    private val lastCityRepository: LastCityRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            WeatherViewModel(weatherRepository, lastCityRepository) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
}