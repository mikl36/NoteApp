package com.example.harkkat.database

import androidx.room.Entity
import androidx.room.PrimaryKey


// note entity, id, title, content and timestamp
@Entity(tableName = "note_table")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

// weather data, last city to be saved to used in the next fetch
@Entity(tableName = "last_city_table")
data class LastCity(
    @PrimaryKey val id: Int = 0, // always id = 0 / save only one city
    val city: String
)