package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.AssistantState

@Composable
fun CyberAudioWave(
    state: AssistantState,
    modifier: Modifier = Modifier
) {
    val isActive = state == AssistantState.SPEAKING || state == AssistantState.LISTENING

    val infiniteTransition = rememberInfiniteTransition(label = "WaveBars")
    val waveStep by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WaveHeight"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        val barCount = 28
        val spacing = size.width / barCount
        val barWidth = spacing * 0.45f

        for (i in 0 until barCount) {
            val factor = kotlin.math.sin(i * 0.4 + (if (isActive) waveStep * 4 else 0f))
            val barHeight = if (isActive) {
                ((factor + 1.2) * 18f * waveStep).coerceIn(4.0, 42.0).toFloat()
            } else {
                3.dp.toPx()
            }

            val x = i * spacing + (spacing - barWidth) / 2
            val y = (size.height - barHeight) / 2

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = if (isActive) {
                        listOf(Color(0xFF00F5FF), Color(0xFFFF007F))
                    } else {
                        listOf(Color(0xFF3A3A4D), Color(0xFF1E1E2E))
                    }
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
        }
    }
}
