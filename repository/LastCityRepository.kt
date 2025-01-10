package com.example.harkkat.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.harkkat.database.LastCity
import com.example.harkkat.database.LastCityDao
import kotlinx.coroutines.flow.Flow


// weather data, last city to be saved to used in the next fetch
class LastCityRepository(private val lastCityDao: LastCityDao) {

    val lastCity: Flow<LastCity?> = lastCityDao.getLastCity()

    suspend fun insert(lastCity: LastCity) {
        lastCityDao.insert(lastCity)
    }
}