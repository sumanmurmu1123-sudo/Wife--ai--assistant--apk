package com.example.v2.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.v2.ui.theme.Cyan
import com.example.v2.ui.theme.NeonPink
import com.example.v2.ui.theme.SoftGold
import com.example.v2.ui.theme.Violet
import com.example.v2.voice.VoiceState

@Composable
fun EdgeLight(
    state: VoiceState,
    audioLevel: Float = 0f,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "edge_light")
    
    val targetColor by animateColorAsState(
        targetValue = when (state) {
            is VoiceState.Listening -> Cyan
            is VoiceState.Speaking -> Violet
            is VoiceState.Thinking -> SoftGold
            is VoiceState.Error -> Color.Red
            else -> Violet.copy(alpha = 0.5f)
        },
        animationSpec = tween(1000),
        label = "color"
    )

    val breathingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val glowWidth by animateFloatAsState(
        targetValue = if (state is VoiceState.Listening || state is VoiceState.Speaking) {
            8f + (audioLevel * 40f)
        } else {
            4f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "width"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val strokeWidth = glowWidth.dp.toPx()
        
        val color = targetColor.copy(alpha = if (state is VoiceState.Listening || state is VoiceState.Speaking) 0.8f else breathingAlpha)

        // Main edge line
        drawRect(
            color = color,
            style = Stroke(width = strokeWidth / 2)
        )
        
        // Bloom layers
        val layers = 3
        for (i in 1..layers) {
            val layerAlpha = (color.alpha / (i * 2))
            val layerWidth = strokeWidth * (i * 2)
            
            // Top glow
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(color.copy(alpha = layerAlpha), Color.Transparent),
                    startY = 0f,
                    endY = layerWidth
                ),
                size = size.copy(height = layerWidth)
            )
            
            // Bottom glow
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, color.copy(alpha = layerAlpha)),
                    startY = size.height - layerWidth,
                    endY = size.height
                ),
                topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - layerWidth),
                size = size.copy(height = layerWidth)
            )

            // Left glow
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(color.copy(alpha = layerAlpha), Color.Transparent),
                    startX = 0f,
                    endX = layerWidth
                ),
                size = size.copy(width = layerWidth)
            )

            // Right glow
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, color.copy(alpha = layerAlpha)),
                    startX = size.width - layerWidth,
                    endX = size.width
                ),
                topLeft = androidx.compose.ui.geometry.Offset(size.width - layerWidth, 0f),
                size = size.copy(width = layerWidth)
            )
        }
    }
}
