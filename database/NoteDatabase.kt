package com.example.harkkat.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// consider setting a directory for Room to use to export the schema (now false for the demo project)
// so you can check the current schema into your version control system
@Database(entities = [Note::class, LastCity::class], version = 2, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun lastCityDao(): LastCityDao

    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        fun getDatabase(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_database"
                ).fallbackToDestructiveMigration() // drops the existing database and recreates it when a migration is needed but not provided
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}