package com.example.harkkat

import com.example.harkkat.viewmodel.NoteViewModel
import com.example.harkkat.viewmodel.NoteViewModelFactory
import com.example.harkkat.viewmodel.WeatherViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.harkkat.ui.theme.HarkkatTheme
import com.example.harkkat.viewmodel.WeatherViewModelFactory

class MainActivity : ComponentActivity() {
    // initialize view models for managing notes and weather data
    private val noteViewModel: NoteViewModel by viewModels {
        NoteViewModelFactory((application as NoteApplication).noteRepository)
    }
    private val weatherViewModel: WeatherViewModel by viewModels {
        WeatherViewModelFactory(
            (application as NoteApplication).weatherRepository,
            (application as NoteApplication).lastCityRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HarkkatTheme {
                NoteApp(noteViewModel, weatherViewModel)
            }
        }
    }
}
// Main composable function for the app
@Composable
fun NoteApp(noteViewModel: NoteViewModel, weatherViewModel: WeatherViewModel) {
    val navController = rememberNavController() // navigation controller for handling screen transitions

    // nav host setup: noteList (home screen), noteDetail, and weather screen
    NavHost(navController, startDestination = "noteList") {
        composable("noteList") {
            val notes by noteViewModel.allNotes.collectAsState()
            NoteList(
                viewModel = noteViewModel,
                notes = notes,
                onAddNote = { navController.navigate("noteDetail/0") },
                onNoteClicked = { noteId -> navController.navigate("noteDetail/$noteId") },
                onWeatherNote = { navController.navigate("weather") }
            )
        }
        composable("noteDetail/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")?.toInt() ?: 0
            NoteDetailScreen(noteViewModel, noteId, navController)
        }
        composable("weather") {
            WeatherScreen(weatherViewModel, noteViewModel, navController)
        }
    }
}