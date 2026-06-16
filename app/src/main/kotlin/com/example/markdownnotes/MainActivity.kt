package com.example.markdownnotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.markdownnotes.data.Note

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MarkdownNotesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MarkdownNotesApp()
                }
            }
        }
    }
}

@Composable
fun MarkdownNotesTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = androidx.compose.ui.graphics.Color(0xFFBB86FC),
            onPrimary = androidx.compose.ui.graphics.Color.Black,
            secondary = androidx.compose.ui.graphics.Color(0xFF03DAC6),
            onSecondary = androidx.compose.ui.graphics.Color.Black,
            background = androidx.compose.ui.graphics.Color(0xFF121212),
            onBackground = androidx.compose.ui.graphics.Color.White,
            surface = androidx.compose.ui.graphics.Color(0xFF1E1E1E),
            onSurface = androidx.compose.ui.graphics.Color.White
        ),
        content = content
    )
}

enum class Screen {
    List, Edit
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkdownNotesApp(viewModel: NoteViewModel = viewModel()) {
    var currentScreen by remember { mutableStateOf(Screen.List) }
    var editingNote by remember { mutableStateOf<Note?>(null) }
    val notes by viewModel.notes.collectAsState()

    when (currentScreen) {
        Screen.List -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Markdown Notes") },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = {
                        editingNote = null
                        currentScreen = Screen.Edit
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Note")
                    }
                }
            ) { padding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(notes) { note ->
                        NoteItem(
                            note = note,
                            onClick = {
                                editingNote = note
                                currentScreen = Screen.Edit
                            },
                            onDelete = { viewModel.deleteNote(note) }
                        )
                    }
                }
            }
        }
        Screen.Edit -> {
            var title by remember { mutableStateOf(editingNote?.title ?: "") }
            var content by remember { mutableStateOf(editingNote?.content ?: "") }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(if (editingNote == null) "New Note" else "Edit Note") },
                        navigationIcon = {
                            IconButton(onClick = { currentScreen = Screen.List }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = {
                        if (title.isNotBlank() || content.isNotBlank()) {
                            val newNote = Note(
                                id = editingNote?.id ?: 0,
                                title = title.ifBlank { "Untitled" },
                                content = content
                            )
                            viewModel.saveNote(newNote)
                        }
                        currentScreen = Screen.List
                    }) {
                        Text("Save", modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Markdown Content") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun NoteItem(note: Note, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
