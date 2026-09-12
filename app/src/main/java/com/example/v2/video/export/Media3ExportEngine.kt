package com.example.v2.video.export

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
