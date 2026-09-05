package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.AssistantMood
import com.example.data.AssistantState
import com.example.ui.WifeAnimatedCore 

@Composable
fun Wife3DGirlAvatar(
    state: AssistantState,
    currentMood: AssistantMood = AssistantMood.NEUTRAL,
    modifier: Modifier = Modifier
) {
    // Fallback to the awesome quantum orb since 3D model loading isn't provided
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        WifeAnimatedCore(state = state, onOrbTapped = {})
    }
}

@Composable
fun EdgeBorderLighting(
    isActive: Boolean = true,
    strokeWidthDp: Float = 6f,
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    val infiniteTransition = rememberInfiniteTransition(label = "RgbBorderAnimation")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "RgbGlow"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val strokePx = strokeWidthDp.dp.toPx()
        val cornerRadiusPx = 28.dp.toPx()

        val rgbColors = listOf(
            Color(0xFFFF0055).copy(alpha = glowAlpha),
            Color(0xFFFF7700).copy(alpha = glowAlpha),
            Color(0xFFFFEA00).copy(alpha = glowAlpha),
            Color(0xFF00FF66).copy(alpha = glowAlpha),
            Color(0xFF00F5FF).copy(alpha = glowAlpha),
            Color(0xFF8A2BE2).copy(alpha = glowAlpha),
            Color(0xFFFF0055).copy(alpha = glowAlpha)
        )

        drawRoundRect(
            brush = Brush.sweepGradient(
                colors = rgbColors,
                center = center
            ),
            topLeft = Offset(strokePx / 4, strokePx / 4),
            size = Size(size.width - strokePx / 2, size.height - strokePx / 2),
            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
            style = Stroke(width = strokePx * 1.8f)
        )

        drawRoundRect(
            brush = Brush.sweepGradient(
                colors = rgbColors,
                center = center
            ),
            topLeft = Offset(strokePx / 2, strokePx / 2),
            size = Size(size.width - strokePx, size.height - strokePx),
            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
            style = Stroke(width = strokePx)
        )
    }
}

@Composable
fun WifeVoiceButton(
    state: AssistantState,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color(0xFFFF007F))
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "Speak",
            tint = Color.White,
            modifier = Modifier.size(36.dp)
        )
    }
}
