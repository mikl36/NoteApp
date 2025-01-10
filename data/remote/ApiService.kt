package com.example.harkkat.data.remote

import com.example.harkkat.data.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

// API Service interface, weather data, openweathermap.org
interface ApiService {
    @GET("weather")
    suspend fun getWeather( // parameters city, apikey & language
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("lang") lang: String
    ): WeatherResponse
}