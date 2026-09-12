package com.example.v2.video.renderer

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
