package com.example.harkkat.repository

import com.example.harkkat.data.model.WeatherResponse
import com.example.harkkat.data.remote.ApiService

// weather data, fetch weather data from openweathermap.org
class WeatherRepository(private val apiService: ApiService) {
    suspend fun getWeather(city: String, apiKey: String, languageCode: String): WeatherResponse {
        return apiService.getWeather(city, apiKey, languageCode)
    }
}