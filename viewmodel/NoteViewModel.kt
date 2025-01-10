package com.example.harkkat.viewmodel

import androidx.lifecycle.*
import com.example.harkkat.database.Note
import com.example.harkkat.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// view model for managing Note data
class NoteViewModel(private val repository: NoteRepository) : ViewModel() {
    //  Backing property for all notes, exposed as StateFlow
    private val _allNotes = MutableStateFlow(emptyList<Note>())
    val allNotes: StateFlow<List<Note>> = _allNotes

    // Backing property for the current note, exposed as StateFlow
    private val _currentNote = MutableStateFlow<Note?>(null)
    val currentNote: StateFlow<Note?> = _currentNote

    //  Initialize the ViewModel
    init {
        // Launch a coroutine to collect all notes from the repository
        viewModelScope.launch {
            repository.allNotes.collect {
                _allNotes.value = it
            }
        }
    }

    fun insert(note: Note) = viewModelScope.launch {
        repository.insert(note)
    }

    fun delete(note: Note) = viewModelScope.launch {
        repository.delete(note)
    }

    fun update(note: Note) = viewModelScope.launch {
        repository.update(note)
    }

    fun getNoteById(noteId: Int) = viewModelScope.launch {
        repository.getNoteById(noteId).collect {
            _currentNote.value = it
        }
    }
}

// Factory for creating instances of NoteViewModel
class NoteViewModelFactory(private val repository: NoteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}