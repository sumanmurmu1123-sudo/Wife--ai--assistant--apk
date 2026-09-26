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
import androidx.compose.ui.graphics.StrokeCap
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
    val viewModel: com.example.v2.voice.VoiceViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val expression by viewModel.currentExpression.collectAsState()
    
    val appState by com.example.v2.core.StateManager.state.collectAsState()
    val audioLevel = appState.audioLevel
    
    val infiniteTransition = rememberInfiniteTransition(label = "HeartNodeTransition")

    val pulseDuration = when (state) {
        is VoiceState.Listening -> 800
        is VoiceState.Speaking -> 500
        is VoiceState.Connecting, is VoiceState.Thinking -> 1200
        else -> 2000
    }

    val baseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseDuration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HeartScale"
    )
    
    // Combine base animation with reactive audio level
    val reactiveScale = baseScale + (audioLevel * 0.4f)
    
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
            .scale(reactiveScale)
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
            
            val baseColor = when (expression) {
                "happy" -> Color(0xFFFF69B4) // HotPink
                "excited" -> Color(0xFFFFD700) // Gold
                "surprised" -> Color(0xFF00BFFF) // DeepSkyBlue
                "curious" -> Color(0xFF9370DB) // MediumPurple
                "listening" -> com.example.v2.ui.theme.NeonPink
                else -> when (state) {
                    is VoiceState.NotConfigured -> Color.Gray
                    is VoiceState.Idle, is VoiceState.Disconnected -> com.example.v2.ui.theme.Cyan
                    is VoiceState.Listening, is VoiceState.Speaking -> com.example.v2.ui.theme.NeonPink
                    is VoiceState.Thinking -> Color.Yellow
                    is VoiceState.Error -> Color.Red
                    else -> com.example.v2.ui.theme.Cyan
                }
            }
            
            // 1. Large Outer Soft Glow
            drawPath(
                path = path,
                color = baseColor.copy(alpha = 0.2f + (audioLevel * 0.3f).coerceAtMost(0.5f)),
                style = Stroke(width = (12.dp.toPx() * reactiveScale), cap = StrokeCap.Round)
            )
            
            // 2. Medium Glow
            drawPath(
                path = path,
                color = baseColor.copy(alpha = 0.5f + (audioLevel * 0.2f).coerceAtMost(0.4f)),
                style = Stroke(width = (6.dp.toPx() * reactiveScale), cap = StrokeCap.Round)
            )
            
            // 3. Bright Core Line
            drawPath(
                path = path,
                color = if (state is VoiceState.NotConfigured) Color.White.copy(alpha = 0.5f) else Color.White,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
            
            // 4. Internal Soft Glow (Aura)
            drawPath(
                path = path,
                brush = Brush.radialGradient(
                    colors = listOf(baseColor.copy(alpha = 0.4f + audioLevel * 0.4f), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(width / 2, height / 3),
                    radius = width / (1.5f - audioLevel)
                ),
                blendMode = BlendMode.Screen
            )
            
            // 5. Reactive Center Light
            if (state is VoiceState.Listening || state is VoiceState.Speaking) {
                drawCircle(
                    color = baseColor.copy(alpha = 0.3f + audioLevel * 0.5f),
                    radius = (width / 4) * reactiveScale,
                    center = androidx.compose.ui.geometry.Offset(width / 2, height / 2.5f)
                )
            }
        }
    }
}
