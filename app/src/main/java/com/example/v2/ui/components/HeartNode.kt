package com.example.v2.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.v2.voice.VoiceState
import com.example.v2.ui.theme.NeonPink
import com.example.v2.ui.theme.Violet
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HeartNode(state: VoiceState, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "HeartNodeTransition")

    val pulseDuration = when (state) {
        is VoiceState.Listening -> 800
        is VoiceState.Speaking -> 500
        is VoiceState.Connecting, is VoiceState.Thinking -> 1200
        else -> 2000
    }

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseDuration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HeartScale"
    )

    val rotationTarget = if (state is VoiceState.Thinking) 360f else 0f
    val rotation by animateFloatAsState(
        targetValue = rotationTarget,
        animationSpec = tween(if (rotationTarget > 0) 3000 else 1000, easing = LinearEasing),
        label = "HeartRotation"
    )
    
    // Auto-rotating continuous angle for Thinking state
    val continuousRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ContinuousRotation"
    )
    
    val actualRotation = if (state is VoiceState.Thinking) continuousRotation else rotation

    Box(
        modifier = modifier
            .size(120.dp)
            .scale(scale)
            .rotate(actualRotation),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val path = Path().apply {
                // Approximate heart shape using bezier curves
                moveTo(width / 2, height / 5)
                cubicTo(
                    width * 5 / 6, 0f,
                    width, height / 3,
                    width / 2, height * 4 / 5
                )
                cubicTo(
                    0f, height / 3,
                    width / 6, 0f,
                    width / 2, height / 5
                )
                close()
            }
            
            // Outer Glow
            drawPath(
                path = path,
                brush = Brush.radialGradient(
                    colors = listOf(NeonPink.copy(alpha = 0.5f), Violet.copy(alpha = 0.1f), Color.Transparent)
                ),
                blendMode = BlendMode.Screen
            )
            
            // Inner Stroke
            drawPath(
                path = path,
                color = NeonPink,
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}
