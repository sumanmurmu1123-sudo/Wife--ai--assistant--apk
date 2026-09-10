package com.example.v2.instagram.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.v2.instagram.model.ReelEditPlan
import com.example.v2.instagram.model.ReelState
import com.example.v2.instagram.model.ReelUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InstagramReelViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ReelUiState())
    val uiState: StateFlow<ReelUiState> = _uiState.asStateFlow()

    fun selectVideo(uri: Uri?) {
        if (uri != null) {
            _uiState.value = _uiState.value.copy(
                state = ReelState.PREVIEW,
                videoUri = uri,
                error = null
            )
        }
    }

    fun analyzeVideoWithAI() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(state = ReelState.ANALYZING)
            
            // Simulating Gemini AI Analysis of video content
            delay(2000)
            
            val editPlan = ReelEditPlan(
                style = "cinematic",
                effects = listOf("soft_rgb_glow", "smooth_zoom", "motion_blur"),
                captionStyle = "viral",
                hashtags = listOf("#InstagramReels", "#Cinematic", "#VideoEditing", "#AI", "#WifeAssistant")
            )
            
            _uiState.value = _uiState.value.copy(
                state = ReelState.READY_TO_EXPORT,
                editPlan = editPlan,
                suggestedTitle = "Wait for the glow ✨",
                shortCaption = "Cinematic vibes \uD83C\uDFAC",
                mediumCaption = "Turning ordinary moments into cinematic masterpieces. \uD83D\uDD25",
                longCaption = "Wait for the glow! \uD83D\uDE0E Turned this simple video into a cinematic Reel using AI. The vibes are immaculate. Drop a \uD83D\uDD25 if you love this edit!",
                selectedCaption = "Wait for the glow! \uD83D\uDE0E Turned this simple video into a cinematic Reel using AI. The vibes are immaculate. Drop a \uD83D\uDD25 if you love this edit!",
                hashtags = editPlan.hashtags.joinToString(" ")
            )
        }
    }

    fun updateCaption(newCaption: String) {
        _uiState.value = _uiState.value.copy(selectedCaption = newCaption)
    }

    fun updateHashtags(newHashtags: String) {
        _uiState.value = _uiState.value.copy(hashtags = newHashtags)
    }

    fun exportAndShare(onExportComplete: (Uri) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(state = ReelState.EXPORTING, progress = 0f)
            
            // Simulating video export process (trim, crop 9:16, apply effects)
            for (i in 1..100 step 10) {
                delay(150)
                _uiState.value = _uiState.value.copy(progress = i / 100f)
            }
            
            _uiState.value = _uiState.value.copy(state = ReelState.SUCCESS)
            
            // In a real app, this would be the URI of the exported 9:16 video.
            // We pass the original URI for the sake of sharing intent in this simulation.
            _uiState.value.videoUri?.let { uri ->
                onExportComplete(uri)
            }
        }
    }

    fun reset() {
        _uiState.value = ReelUiState()
    }
}
