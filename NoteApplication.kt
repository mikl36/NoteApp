package com.example.harkkat

import android.app.Application
import com.example.harkkat.database.NoteDatabase
import com.example.harkkat.repository.NoteRepository
import com.example.harkkat.repository.WeatherRepository
import com.example.harkkat.data.remote.RetrofitInstance
import com.example.harkkat.repository.LastCityRepository

// Application class for the app, initializes repositories and database, lazy initialization for performance
class NoteApplication : Application() {
    private val database by lazy { NoteDatabase.getDatabase(this) }
    val noteRepository by lazy { NoteRepository(database.noteDao()) }
    val weatherRepository by lazy { WeatherRepository(RetrofitInstance.createWeatherService()) }
    val lastCityRepository by lazy { LastCityRepository(database.lastCityDao()) }
}