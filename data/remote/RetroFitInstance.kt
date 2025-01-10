package com.example.harkkat.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Retrofit instance, weather data, openweathermap.org
object RetrofitInstance {
    private const val BASEURL = "https://api.openweathermap.org/data/2.5/"

    fun createWeatherService(): ApiService {
        return Retrofit.Builder()
            .baseUrl(BASEURL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}