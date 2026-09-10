package com.example.v2.instagram.model

import android.net.Uri

enum class ReelState {
    IDLE,
    SELECTING,
    PREVIEW,
    ANALYZING,
    EDITING,
    READY_TO_EXPORT,
    EXPORTING,
    SUCCESS,
    ERROR
}

data class ReelEditPlan(
    val platform: String = "instagram_reels",
    val aspectRatio: String = "9:16",
    val style: String = "",
    val effects: List<String> = emptyList(),
    val captionStyle: String = "",
    val hashtags: List<String> = emptyList()
)

data class ReelUiState(
    val state: ReelState = ReelState.IDLE,
    val videoUri: Uri? = null,
    val shortCaption: String = "",
    val mediumCaption: String = "",
    val longCaption: String = "",
    val selectedCaption: String = "",
    val hashtags: String = "",
    val suggestedTitle: String = "",
    val editPlan: ReelEditPlan? = null,
    val error: String? = null,
    val isExporting: Boolean = false,
    val progress: Float = 0f
)
