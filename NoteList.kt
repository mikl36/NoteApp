package com.example.harkkat

import android.app.SearchManager
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import com.example.harkkat.database.Note
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.harkkat.viewmodel.NoteViewModel

// composable function for the note list screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteList(
    viewModel: NoteViewModel,
    notes: List<Note>,
    onAddNote: () -> Unit,
    onNoteClicked: (Int) -> Unit, // callback for when a note is clicked, passing the note's ID or remove note
    onWeatherNote: () -> Unit
) {
    // state variable for the search query
    var searchQuery by remember { mutableStateOf("") }

    // filter the notes based on the search query
    val filteredNotes = if (searchQuery.isEmpty()) {
        notes
    } else {
        notes.filter { it.title.contains(searchQuery, ignoreCase = true) ||
                it.content.contains(searchQuery, ignoreCase = true) }
    }

    val context = LocalContext.current

    // common intent for performing a Google search (internet search)
    fun performGoogleSearch(query: String) {
        val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra(SearchManager.QUERY, query)
        } // check if intent resolves, there is an activity/manager that can handle it
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, context.getString(R.string.note_list_web_search_request),
                Toast.LENGTH_SHORT).show()
        }
    }
  // Scaffold layout with a top app bar and floating action buttons
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notes_list_topbar_text)) },
                actions = {
                    SearchBar(searchQuery) { query -> searchQuery = query }
                }
            )
        },
        floatingActionButton = {
            NoteListFABs(
                onAddNote = onAddNote,
                onGoogleSearch = { performGoogleSearch(context.getString
                    (R.string.note_list_google_query_text)) },
                onWeatherNote = onWeatherNote
            )
        }
    ) { padding ->
        NoteGrid(
            notes = filteredNotes,
            onNoteClicked = onNoteClicked,
            deleteNote = { viewModel.delete(it) },
            modifier = Modifier.padding(padding)
        )
    }
}

// composable function for each note item in the list
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteItem(note: Note, onClick: () -> Unit, deleteNote: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clip(RectangleShape)
            .combinedClickable(onClick = onClick, onLongClick = deleteNote),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// composable function for the search bar of the note list to filter notes
@Composable
fun SearchBar(searchQuery: String, onSearchQueryChange: (String) -> Unit) {
    TextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = { Text(stringResource(R.string.note_list_placeholder_text_search)) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription =
            stringResource(R.string.note_list_search_icon_text))
        },
        modifier = Modifier.fillMaxWidth()
    )
}

// composable function for the floating action buttons of the note list
@Composable
fun NoteListFABs(onAddNote: () -> Unit, onGoogleSearch: () -> Unit, onWeatherNote: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(end = 16.dp, bottom = 16.dp)
    ) {
        FloatingActionButton(onClick = onAddNote) {
            Icon(Icons.Default.Add, contentDescription =
            stringResource(R.string.note_list_fbutton_add_note))
        }
        FloatingActionButton(onClick = onGoogleSearch) {
            Icon(Icons.Default.Search, contentDescription =
            stringResource(R.string.note_list_icon_text_google_search))
        }
        FloatingActionButton(onClick = onWeatherNote) {
            Icon(Icons.Default.Info, contentDescription =
            stringResource(R.string.note_list_fbutton_icon_weather_note))
        }
    }
}

// composable function for the grid of notes
@Composable
fun NoteGrid(
    notes: List<Note>,
    onNoteClicked: (Int) -> Unit,
    deleteNote: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenDivider = 2
    val itemWidth = (screenWidth - 32.dp) / screenDivider

    LazyVerticalGrid( // lazy grid to display the notes in a grid for optimal performance/experience
        columns = GridCells.Adaptive(minSize = itemWidth),
        modifier = modifier
    ) {
        items(notes) { note -> // for each note, display the note item or delete it
            NoteItem(note,
                onClick = { onNoteClicked(note.id) },
                deleteNote = { deleteNote(note) })
        }
    }
}