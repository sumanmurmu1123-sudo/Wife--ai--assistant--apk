import os

base_pkg = "app/src/main/java/com/example/v2/video"
dirs = [
    f"{base_pkg}/domain/model",
    f"{base_pkg}/ui/editor",
    f"{base_pkg}/ui/timeline",
    f"{base_pkg}/ui/components",
    f"{base_pkg}/renderer",
    f"{base_pkg}/export",
    f"{base_pkg}/ai"
]

for d in dirs:
    os.makedirs(d, exist_ok=True)

models = """package com.example.v2.video.domain.model

import android.net.Uri

data class VideoProject(
    val id: String,
    val name: String,
    val timeline: TimelineModel,
    val exportSettings: ExportSettings
)

data class TimelineModel(
    val videoTracks: List<TimelineTrack> = emptyList(),
    val audioTracks: List<TimelineTrack> = emptyList(),
    val textTracks: List<TimelineTrack> = emptyList()
)

data class TimelineTrack(
    val id: String,
    val clips: List<TimelineClip> = emptyList()
)

sealed class TimelineClip {
    abstract val id: String
    abstract val startTimeMs: Long
    abstract val durationMs: Long
}

data class VideoClip(
    override val id: String,
    val uri: Uri,
    override val startTimeMs: Long,
    override val durationMs: Long,
    val startTrimMs: Long = 0L,
    val endTrimMs: Long = durationMs,
    val speed: Float = 1f,
    val filters: List<FilterData> = emptyList(),
    val effects: List<EffectData> = emptyList(),
    val keyframes: List<Keyframe> = emptyList()
) : TimelineClip()

data class AudioClip(
    override val id: String,
    val uri: Uri,
    override val startTimeMs: Long,
    override val durationMs: Long,
    val volume: Float = 1f
) : TimelineClip()

data class TextClip(
    override val id: String,
    val text: String,
    override val startTimeMs: Long,
    override val durationMs: Long
) : TimelineClip()

data class FilterData(val id: String, val name: String, val intensity: Float = 1f)
data class EffectData(val id: String, val name: String, val parameters: Map<String, Float> = emptyMap())
data class Keyframe(val timeMs: Long, val scale: Float = 1f, val rotation: Float = 0f, val opacity: Float = 1f)

data class ExportSettings(
    val resolution: Int = 1080,
    val frameRate: Int = 30,
    val outputUri: Uri? = null
)

enum class EditorState {
    IDLE,
    MEDIA_IMPORTING,
    MEDIA_READY,
    READY,
    EDITING,
    AI_ANALYZING,
    TRANSCRIBING,
    EXPORTING,
    COMPLETED,
    ERROR
}
"""

with open(f"{base_pkg}/domain/model/Models.kt", "w") as f:
    f.write(models)

ai_engine = """package com.example.v2.video.ai

import com.example.v2.video.domain.model.TimelineModel
import kotlinx.coroutines.delay

class VideoAiEngine {
    suspend fun analyzeMediaAndGeneratePlan(prompt: String, currentTimeline: TimelineModel): TimelineModel {
        // Simulate AI analysis delay
        delay(2000)
        // In reality, this would send prompt + metadata to Gemini API
        return currentTimeline // Return unmodified or mock modified for now
    }
}
"""

with open(f"{base_pkg}/ai/VideoAiEngine.kt", "w") as f:
    f.write(ai_engine)

editor_vm = """package com.example.v2.video.ui.editor

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.v2.video.domain.model.*
import com.example.v2.video.ai.VideoAiEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class VideoEditorViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(EditorState.IDLE)
    val state: StateFlow<EditorState> = _state.asStateFlow()

    private val _project = MutableStateFlow<VideoProject?>(null)
    val project: StateFlow<VideoProject?> = _project.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val aiEngine = VideoAiEngine()

    fun createProject(name: String) {
        val newProj = VideoProject(
            id = UUID.randomUUID().toString(),
            name = name,
            timeline = TimelineModel(),
            exportSettings = ExportSettings()
        )
        _project.value = newProj
        _state.value = EditorState.READY
    }

    fun addVideoMedia(uri: Uri, durationMs: Long) {
        val proj = _project.value ?: return
        
        val newClip = VideoClip(
            id = UUID.randomUUID().toString(),
            uri = uri,
            startTimeMs = 0L,
            durationMs = durationMs
        )
        
        val newTrack = TimelineTrack(
            id = UUID.randomUUID().toString(),
            clips = listOf(newClip)
        )
        
        val newTimeline = proj.timeline.copy(
            videoTracks = proj.timeline.videoTracks + newTrack
        )
        
        _project.value = proj.copy(timeline = newTimeline)
        _state.value = EditorState.MEDIA_READY
    }

    fun applyAiEdit(prompt: String) {
        val proj = _project.value ?: return
        _state.value = EditorState.AI_ANALYZING
        
        viewModelScope.launch {
            try {
                val updatedTimeline = aiEngine.analyzeMediaAndGeneratePlan(prompt, proj.timeline)
                _project.value = proj.copy(timeline = updatedTimeline)
                _state.value = EditorState.READY
            } catch (e: Exception) {
                _errorMessage.value = "AI Analysis failed"
                _state.value = EditorState.READY
            }
        }
    }
    
    fun setExporting(exporting: Boolean) {
        if (exporting) {
            _state.value = EditorState.EXPORTING
        } else {
            _state.value = EditorState.COMPLETED
        }
    }
    
    fun setError(error: String) {
        _errorMessage.value = error
        _state.value = EditorState.ERROR
    }
    
    fun clearError() {
        _errorMessage.value = null
        if (_state.value == EditorState.ERROR) {
            _state.value = EditorState.READY
        }
    }
}
"""

with open(f"{base_pkg}/ui/editor/VideoEditorViewModel.kt", "w") as f:
    f.write(editor_vm)

editor_screen = """package com.example.v2.video.ui.editor

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
"""

with open(f"{base_pkg}/ui/editor/VideoStudioScreen.kt", "w") as f:
    f.write(editor_screen)

timeline_view = """package com.example.v2.video.ui.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.v2.video.domain.model.TimelineModel

@Composable
fun TimelineView(timeline: TimelineModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2D2D2D))
            .padding(8.dp)
    ) {
        Text("Timeline", color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
        
        timeline.videoTracks.forEachIndexed { index, track ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Color(0xFF3D3D3D))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("V${index + 1}", color = Color.White, modifier = Modifier.width(30.dp))
                
                track.clips.forEach { clip ->
                    Box(
                        modifier = Modifier
                            .height(50.dp)
                            .width(100.dp) // Mock width
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Blue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Video", color = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
"""

with open(f"{base_pkg}/ui/timeline/TimelineView.kt", "w") as f:
    f.write(timeline_view)

preview_surface = """package com.example.v2.video.renderer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.v2.video.domain.model.TimelineModel
import com.example.v2.video.domain.model.VideoClip

@Composable
fun VideoPreviewSurface(timeline: TimelineModel) {
    val context = LocalContext.current
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            // Load media items from timeline
            val mediaItems = timeline.videoTracks.flatMap { it.clips }
                .filterIsInstance<VideoClip>()
                .map { MediaItem.fromUri(it.uri) }
            
            setMediaItems(mediaItems)
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
            }
        }
    )
}
"""

with open(f"{base_pkg}/renderer/VideoPreviewSurface.kt", "w") as f:
    f.write(preview_surface)

export_engine = """package com.example.v2.video.export

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Transformer
import com.example.v2.video.domain.model.TimelineModel
import com.example.v2.video.domain.model.VideoClip
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.io.File

class Media3ExportEngine(private val context: Context) {
    suspend fun exportTimeline(timeline: TimelineModel, outputPath: String) = suspendCancellableCoroutine { continuation ->
        val transformer = Transformer.Builder(context).build()
        
        // Very basic single-track export for demonstration
        val firstTrack = timeline.videoTracks.firstOrNull()
        if (firstTrack == null || firstTrack.clips.isEmpty()) {
            continuation.resumeWithException(IllegalStateException("No video tracks to export"))
            return@suspendCancellableCoroutine
        }
        
        val firstClip = firstTrack.clips.first() as VideoClip
        val mediaItem = MediaItem.fromUri(firstClip.uri)
        val editedMediaItem = EditedMediaItem.Builder(mediaItem).build()
        
        transformer.addListener(object : Transformer.Listener {
            override fun onCompleted(composition: Composition, exportResult: androidx.media3.transformer.ExportResult) {
                continuation.resume(Unit)
            }
            override fun onError(composition: Composition, exportResult: androidx.media3.transformer.ExportResult, exportException: androidx.media3.transformer.ExportException) {
                continuation.resumeWithException(exportException)
            }
        })
        
        transformer.start(editedMediaItem, outputPath)
    }
}
"""

with open(f"{base_pkg}/export/Media3ExportEngine.kt", "w") as f:
    f.write(export_engine)

