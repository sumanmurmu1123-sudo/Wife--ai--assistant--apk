package com.example.v2.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.BlendMode
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
            is VoiceState.Processing -> SoftGold
            is VoiceState.Error -> Color.Red
            else -> Cyan.copy(alpha = 0.5f)
        },
        animationSpec = tween(1000),
        label = "base_color"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val heartSize = 22.dp.toPx() * (1f + audioLevel * 0.4f) * breathingScale
        val inset = heartSize / 2 + 4.dp.toPx()
        val innerW = w - inset * 2
        val innerH = h - inset * 2
        val perimeter = (innerW + innerH) * 2
        val spacing = 32.dp.toPx()
        val count = (perimeter / spacing).toInt().coerceAtLeast(1)

        for (i in 0 until count) {
            val progress = (i.toFloat() / count + phase) % 1f
            val posPerimeter = progress * perimeter
            
            val (x, y) = when {
                posPerimeter < innerW -> (inset + posPerimeter) to inset
                posPerimeter < innerW + innerH -> (w - inset) to (inset + (posPerimeter - innerW))
                posPerimeter < innerW * 2 + innerH -> (w - inset - (posPerimeter - (innerW + innerH))) to (h - inset)
                else -> inset to (h - inset - (posPerimeter - (innerW * 2 + innerH)))
            }

            // Calculate rotating color
            val colorProgress = (progress + phase * 0.5f) % 1f
            val heartColor = when {
                colorProgress < 0.33f -> androidx.compose.ui.graphics.lerp(Cyan, NeonPink, colorProgress / 0.33f)
                colorProgress < 0.66f -> androidx.compose.ui.graphics.lerp(NeonPink, Violet, (colorProgress - 0.33f) / 0.33f)
                else -> androidx.compose.ui.graphics.lerp(Violet, Cyan, (colorProgress - 0.66f) / 0.34f)
            }

            // Apply base color influence based on state
            val finalColor = androidx.compose.ui.graphics.lerp(heartColor, baseColor, 0.5f)
                .copy(alpha = if (state is VoiceState.Idle || state is VoiceState.Disconnected) 0.4f else 1.0f)

            // Draw glowing heart (neon style)
            drawGlowingHeart(
                center = androidx.compose.ui.geometry.Offset(x, y),
                size = heartSize,
                color = finalColor
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGlowingHeart(
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
    
    // 1. Outer Glow
    drawPath(
        path = path,
        color = color.copy(alpha = 0.3f),
        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
    )
    
    // 2. Middle Glow
    drawPath(
        path = path,
        color = color.copy(alpha = 0.6f),
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
    )
    
    // 3. Core Line (Brightest)
    drawPath(
        path = path,
        color = Color.White.copy(alpha = 0.8f),
        style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round)
    )
    
    // Add a tiny bit of internal glow
    drawPath(
        path = path,
        color = color.copy(alpha = 0.15f),
        blendMode = BlendMode.Screen
    )
}
