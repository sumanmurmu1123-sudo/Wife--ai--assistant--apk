package com.example.v2.video.ui.editor

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
