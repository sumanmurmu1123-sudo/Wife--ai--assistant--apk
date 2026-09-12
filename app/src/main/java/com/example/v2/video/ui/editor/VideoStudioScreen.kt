package com.example.v2.video.ui.editor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.v2.video.domain.model.EditorState
import com.example.v2.video.renderer.VideoPreviewSurface
import com.example.v2.video.ui.timeline.TimelineView
import com.example.v2.video.export.Media3ExportEngine
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoStudioScreen(viewModel: VideoEditorViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val project by viewModel.project.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        if (project == null) {
            viewModel.createProject("My Awesome Video")
        }
    }
    
    val mediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                // Defaulting to 10 seconds for simplicity in this demo without MediaMetadataRetriever
                viewModel.addVideoMedia(it, 10000L)
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(project?.name ?: "Video Studio") },
                actions = {
                    IconButton(onClick = { /* Undo */ }) { Icon(Icons.Default.Undo, "Undo") }
                    IconButton(onClick = { /* Redo */ }) { Icon(Icons.Default.Redo, "Redo") }
                    Button(onClick = { 
                        if (project?.timeline?.videoTracks?.isNotEmpty() == true) {
                            coroutineScope.launch {
                                viewModel.setExporting(true)
                                try {
                                    val exporter = Media3ExportEngine(context)
                                    // Normally we would generate a real output file URI
                                    // exporter.exportTimeline(project!!.timeline, outputPath)
                                    kotlinx.coroutines.delay(2000) // Simulate export
                                    viewModel.setExporting(false)
                                } catch (e: Exception) {
                                    viewModel.setError("EXPORT_FAILED: ${e.message}")
                                }
                            }
                        }
                    }) {
                        Text("Export")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            // Preview Surface
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                if (project?.timeline?.videoTracks?.isNotEmpty() == true) {
                    VideoPreviewSurface(timeline = project!!.timeline)
                } else {
                    Text("No Media Selected", color = Color.White)
                }
                
                // State Overlays
                when (state) {
                    EditorState.AI_ANALYZING -> {
                        CircularProgressIndicator()
                        Text("AI ANALYZING...", color = Color.Cyan, modifier = Modifier.padding(top = 40.dp))
                    }
                    EditorState.EXPORTING -> {
                        CircularProgressIndicator()
                        Text("EXPORTING...", color = Color.Green, modifier = Modifier.padding(top = 40.dp))
                    }
                    EditorState.COMPLETED -> {
                        Text("EXPORT COMPLETE", color = Color.Green)
                    }
                    EditorState.ERROR -> {
                        Text("ERROR", color = Color.Red)
                    }
                    else -> {}
                }
            }
            
            // Toolbar
            EditorToolbar(
                onAddMedia = { mediaPicker.launch(arrayOf("video/*")) },
                onAiEdit = { viewModel.applyAiEdit("Make it cinematic") }
            )
            
            // Timeline
            Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                if (project != null) {
                    TimelineView(timeline = project!!.timeline)
                }
            }
        }
    }
}

@Composable
fun EditorToolbar(onAddMedia: () -> Unit, onAiEdit: () -> Unit) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E))
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ToolbarButton("Add Media", Icons.Default.Add, onAddMedia) }
        item { ToolbarButton("Trim", Icons.Default.ContentCut, {}) }
        item { ToolbarButton("Text", Icons.Default.Title, {}) }
        item { ToolbarButton("Audio", Icons.Default.Audiotrack, {}) }
        item { ToolbarButton("Effects", Icons.Default.AutoAwesome, {}) }
        item { ToolbarButton("AI Edit", Icons.Default.SmartToy, onAiEdit) }
    }
}

@Composable
fun ToolbarButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = label, tint = Color.White)
        }
        Text(label, color = Color.White, style = MaterialTheme.typography.labelSmall)
    }
}
