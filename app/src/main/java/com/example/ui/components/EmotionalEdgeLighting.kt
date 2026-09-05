package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.data.AssistantMood

@Composable
fun EmotionalEdgeLighting(
    mood: AssistantMood,
    isActive: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    val animatedPrimary by animateColorAsState(
        targetValue = mood.primaryColor,
        animationSpec = tween(durationMillis = 800),
        label = "PrimaryMoodColor"
    )
    val animatedGlow by animateColorAsState(
        targetValue = mood.glowColor,
        animationSpec = tween(durationMillis = 800),
        label = "GlowMoodColor"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "MoodPulse")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "MoodAngle"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val strokeWidth = 5.dp.toPx()
        val cornerRadius = 24.dp.toPx()

        val gradientColors = listOf(
            animatedPrimary,
            animatedGlow,
            animatedPrimary.copy(alpha = 0.3f),
            animatedPrimary
        )

        // Rotate the brush based on sweepAngle
        // Note: Brush.sweepGradient does not take angle directly, 
        // to rotate a SweepGradient we would normally apply rotation in draw scope.
        // I will use drawContext.canvas or simpler: draw scope rotation.
        rotate(degrees = sweepAngle, pivot = center) {
            drawRoundRect(
                brush = Brush.sweepGradient(
                    colors = gradientColors,
                    center = center
                ),
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                style = Stroke(width = strokeWidth)
            )
        }
    }
}
