package com.example.harkkat

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.harkkat.components.note.CustomTopAppBar
import com.example.harkkat.data.model.noteDetailColors
import com.example.harkkat.viewmodel.NoteViewModel
import com.example.harkkat.database.Note
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(viewModel: NoteViewModel, noteId: Int, navController: NavController) {
    //  fetch the note with the given ID from the ViewModel / first launched
    LaunchedEffect(noteId) {
        viewModel.getNoteById(noteId)
    }

    // collect the current note state from the ViewModel
    val note by viewModel.currentNote.collectAsState()
    val context = LocalContext.current
    val titleState = remember { mutableStateOf("") }
    val contentState = remember { mutableStateOf("") }

    // Update title and content states when the note changes
    LaunchedEffect(note) {
        if (noteId == 0) {
            titleState.value = ""
            contentState.value = ""
        } else {
            note?.let {
                titleState.value = it.title
                contentState.value = it.content
            }
        }
    }

    // Scaffold layout with a top app bar, note with title and content
    Scaffold(
        topBar = {
            CustomTopAppBar(
                title = stringResource(R.string.note_details_topappbar_title),
                navController = navController
            )
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                NoteDetailContent(titleState, contentState)
                NoteDetailFABs( // floating action buttons
                    noteId = noteId,
                    note = note,
                    titleState = titleState,
                    contentState = contentState,
                    viewModel = viewModel,
                    navController = navController,
                    context = context
                )
            }
        }
    )
}

// composable function to display the note's title and content, column
@Composable
fun NoteDetailContent(
    // state variables for the note's title and content
    titleState: MutableState<String>,
    contentState: MutableState<String>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = titleState.value,
            onValueChange = { newTitle -> titleState.value = newTitle },
            label = { Text(stringResource(R.string.note_detail_note_title)) },
            textStyle = MaterialTheme.typography.titleMedium.copy(
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.fillMaxWidth(),
            colors = noteDetailColors
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = contentState.value,
            onValueChange = { newContent -> contentState.value = newContent },
            label = { Text(stringResource(R.string.note_detail_note_content)) },
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal
            ),
            modifier = Modifier.fillMaxSize(),
            maxLines = Int.MAX_VALUE,
            colors = noteDetailColors
        )
    }
}

// composable function to display the floating action buttons
@Composable
fun NoteDetailFABs(
    noteId: Int,
    note: Note?,
    titleState: MutableState<String>,
    contentState: MutableState<String>,
    viewModel: NoteViewModel,
    navController: NavController,
    context: Context
) {
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 32.dp, end = 32.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        if (noteId != 0) {
                            // Delete existing note
                            note?.let { viewModel.delete(it) }
                            navController.popBackStack()
                        } else {
                            Toast.makeText(context,
                                context.getString(R.string.note_detail_toast_cannot_delete_note),
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            ) {
                Icon(Icons.Default.Delete, contentDescription =
                stringResource(R.string.note_detail_icon_delete_note))
            }
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        if (noteId == 0) {
                            // Insert new note
                            viewModel.insert(Note(
                                title = titleState.value,
                                content = contentState.value
                            ))
                        } else {
                            // Update existing note
                            note?.let {
                                viewModel.update(Note(
                                    id = it.id,
                                    title = titleState.value,
                                    content = contentState.value,
                                    timestamp = System.currentTimeMillis()
                                ))
                            }
                        }
                    }
                    navController.popBackStack()
                }
            ) {
                Icon(Icons.Default.Done, contentDescription =
                stringResource(R.string.note_detail_icon_save_note))
            }
        }
    }
}

/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailTopAppBar(navController: NavController) {
    TopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.note_details_topappbar_title))
            }
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription =
                stringResource(R.string.note_detail_icon_navigationback_text))
            }
        }
    )
} */