package com.example.harkkat.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// note dao: insert, delete, update, get all notes, search notes by title or content, or by id
@Dao
interface NoteDao {
    @Query("SELECT * FROM note_table ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<Note>> // asynchronous flow

    @Query("SELECT * FROM note_table WHERE title LIKE :query OR content LIKE :query ORDER BY timestamp DESC")
    fun searchNotes(query: String): Flow<List<Note>>

    @Query("SELECT * FROM note_table WHERE id = :id")
    fun getNoteById(id: Int): Flow<Note?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Update
    suspend fun update(note: Note)
}

// last city dao: insert, get last city
@Dao
interface LastCityDao {
    @Query("SELECT * FROM last_city_table LIMIT 1")

    fun getLastCity(): Flow<LastCity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(lastCity: LastCity)
}