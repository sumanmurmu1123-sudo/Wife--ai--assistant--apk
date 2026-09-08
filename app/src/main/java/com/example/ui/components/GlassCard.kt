package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val borderColor = if (isActive) Color(0xFF00F5FF) else Color(0xFF00F5FF).copy(alpha = 0.3f)
    val bgColor = Color(0xFF06030F).copy(alpha = 0.6f)
    val glowColor = if (isActive) Color(0xFF00F5FF).copy(alpha = 0.2f) else Color.Transparent

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(
                1.dp,
                if (isActive) Brush.linearGradient(
                    listOf(
                        borderColor.copy(alpha = alphaAnim),
                        borderColor
                    )
                ) else SolidColor(borderColor),
                RoundedCornerShape(8.dp)
            )
            .drawWithContent {
                drawContent()
                // Draw HUD Brackets
                val bracketSize = 12.dp.toPx()
                val bracketThickness = 2.dp.toPx()
                val pathEffect = PathEffect.cornerPathEffect(0f)
                val stroke = Stroke(width = bracketThickness, cap = StrokeCap.Square, join = StrokeJoin.Miter, pathEffect = pathEffect)
                
                // Top Left
                drawLine(borderColor, start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(bracketSize, 0f), strokeWidth = bracketThickness)
                drawLine(borderColor, start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(0f, bracketSize), strokeWidth = bracketThickness)
                
                // Top Right
                drawLine(borderColor, start = androidx.compose.ui.geometry.Offset(size.width, 0f), end = androidx.compose.ui.geometry.Offset(size.width - bracketSize, 0f), strokeWidth = bracketThickness)
                drawLine(borderColor, start = androidx.compose.ui.geometry.Offset(size.width, 0f), end = androidx.compose.ui.geometry.Offset(size.width, bracketSize), strokeWidth = bracketThickness)
                
                // Bottom Left
                drawLine(borderColor, start = androidx.compose.ui.geometry.Offset(0f, size.height), end = androidx.compose.ui.geometry.Offset(bracketSize, size.height), strokeWidth = bracketThickness)
                drawLine(borderColor, start = androidx.compose.ui.geometry.Offset(0f, size.height), end = androidx.compose.ui.geometry.Offset(0f, size.height - bracketSize), strokeWidth = bracketThickness)
                
                // Bottom Right
                drawLine(borderColor, start = androidx.compose.ui.geometry.Offset(size.width, size.height), end = androidx.compose.ui.geometry.Offset(size.width - bracketSize, size.height), strokeWidth = bracketThickness)
                drawLine(borderColor, start = androidx.compose.ui.geometry.Offset(size.width, size.height), end = androidx.compose.ui.geometry.Offset(size.width, size.height - bracketSize), strokeWidth = bracketThickness)
                
                if (isActive) {
                    drawRect(glowColor, style = Stroke(width = 8.dp.toPx()), blendMode = androidx.compose.ui.graphics.BlendMode.Screen)
                }
            }
            .clip(RoundedCornerShape(8.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
