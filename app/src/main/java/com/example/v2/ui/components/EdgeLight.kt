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
    val infiniteTransition = rememberInfiniteTransition(label = "edge_lighting")
    
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )

    val baseColor by animateColorAsState(
        targetValue = when (state) {
            is VoiceState.Listening -> Cyan
            is VoiceState.Speaking -> NeonPink
            is VoiceState.Thinking -> SoftGold
            is VoiceState.Error -> Color.Red
            else -> Cyan.copy(alpha = 0.5f)
        },
        animationSpec = tween(1000),
        label = "base_color"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val perimeter = (w + h) * 2
        val heartSize = 12.dp.toPx() * (1f + audioLevel * 0.5f) * breathingScale
        val spacing = 20.dp.toPx()
        val count = (perimeter / spacing).toInt()

        for (i in 0 until count) {
            val progress = i.toFloat() / count
            val posPerimeter = progress * perimeter
            
            val (x, y) = when {
                posPerimeter < w -> posPerimeter to 0f
                posPerimeter < w + h -> w to (posPerimeter - w)
                posPerimeter < w * 2 + h -> (w - (posPerimeter - (w + h))) to h
                else -> 0f to (h - (posPerimeter - (w * 2 + h)))
            }

            // Calculate rotating color
            val colorProgress = (progress + phase) % 1f
            val heartColor = when {
                colorProgress < 0.33f -> androidx.compose.ui.graphics.lerp(Cyan, NeonPink, colorProgress / 0.33f)
                colorProgress < 0.66f -> androidx.compose.ui.graphics.lerp(NeonPink, Violet, (colorProgress - 0.33f) / 0.33f)
                else -> androidx.compose.ui.graphics.lerp(Violet, Cyan, (colorProgress - 0.66f) / 0.34f)
            }

            // Apply base color influence based on state
            val finalColor = androidx.compose.ui.graphics.lerp(heartColor, baseColor, 0.4f)
                .copy(alpha = if (state is VoiceState.Idle) 0.3f else 0.9f)

            drawHeart(
                center = androidx.compose.ui.geometry.Offset(x, y),
                size = heartSize,
                color = finalColor
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHeart(
    center: androidx.compose.ui.geometry.Offset,
    size: Float,
    color: Color
) {
    val path = androidx.compose.ui.graphics.Path().apply {
        val left = center.x - size / 2
        val top = center.y - size / 2
        val right = center.x + size / 2
        val bottom = center.y + size / 2
        
        moveTo(center.x, top + size * 0.3f)
        cubicTo(
            center.x - size * 0.2f, top,
            left, top + size * 0.2f,
            left, top + size * 0.5f
        )
        cubicTo(
            left, top + size * 0.8f,
            center.x - size * 0.1f, bottom,
            center.x, bottom
        )
        cubicTo(
            center.x + size * 0.1f, bottom,
            right, top + size * 0.8f,
            right, top + size * 0.5f
        )
        cubicTo(
            right, top + size * 0.2f,
            center.x + size * 0.2f, top,
            center.x, top + size * 0.3f
        )
        close()
    }
    drawPath(path = path, color = color)
}
