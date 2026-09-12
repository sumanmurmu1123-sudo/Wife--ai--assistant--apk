package com.example.v2.video.domain.model

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
