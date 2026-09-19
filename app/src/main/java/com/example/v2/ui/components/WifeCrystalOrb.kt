package com.example.v2.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.v2.ui.theme.Cyan
import com.example.v2.ui.theme.Violet

@Composable
fun WifeCrystalOrb(
    audioLevel: Float,
    onDrag: (Float, Float) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbInfinite")
    
    // Breathing/Pulse
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbPulse"
    )

    // Rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbRotation"
    )

    // Reactive scale based on real audio amplitude
    val reactiveScale = 1f + (audioLevel * 0.5f)

    Box(
        modifier = modifier
            .size(70.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = this.center
            val baseRadius = size.minDimension / 2.8f
            val radius = baseRadius * pulse * (1f + (audioLevel * 0.2f))

            // 1. Soft Glow / Halo (Ambient presence)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Cyan.copy(alpha = 0.2f * reactiveScale),
                        Violet.copy(alpha = 0.1f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 2.5f * reactiveScale
                ),
                radius = baseRadius * 2.5f * reactiveScale,
                center = center
            )

            // 2. Crystal Core (Glass Refraction effect)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.45f),
                        Cyan.copy(alpha = 0.25f),
                        Violet.copy(alpha = 0.15f),
                        Color(0x11000000)
                    ),
                    center = center.copy(x = center.x - radius * 0.3f, y = center.y - radius * 0.3f),
                    radius = radius * 1.6f
                ),
                radius = radius,
                center = center
            )

            // 3. Inner Holographic Energy (Swirling cyan/violet core)
            withTransform({
                rotate(rotation, center)
            }) {
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = listOf(Cyan.copy(alpha = 0.7f), Violet.copy(alpha = 0.7f), Cyan.copy(alpha = 0.7f)),
                        start = Offset(center.x - radius * 0.6f, center.y),
                        end = Offset(center.x + radius * 0.6f, center.y)
                    ),
                    radius = radius * 0.5f,
                    center = center,
                    alpha = 0.5f + (audioLevel * 0.5f)
                )
            }

            // 4. Holographic HUD Rings (Thin, elegant)
            withTransform({
                rotate(-rotation * 0.8f, center)
            }) {
                // Outer static-ish thin ring
                drawCircle(
                    color = Cyan.copy(alpha = 0.2f + (audioLevel * 0.4f)),
                    radius = radius * 1.4f,
                    style = Stroke(width = 0.5.dp.toPx()),
                    center = center
                )
                
                // Segmented rotating arcs
                drawArc(
                    color = Cyan.copy(alpha = 0.5f + (audioLevel * 0.5f)),
                    startAngle = 45f,
                    sweepAngle = 60f,
                    useCenter = false,
                    style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round),
                    size = Size(radius * 3.0f, radius * 3.0f),
                    topLeft = Offset(center.x - radius * 1.5f, center.y - radius * 1.5f)
                )
                
                drawArc(
                    color = Violet.copy(alpha = 0.5f),
                    startAngle = 225f,
                    sweepAngle = 60f,
                    useCenter = false,
                    style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round),
                    size = Size(radius * 3.0f, radius * 3.0f),
                    topLeft = Offset(center.x - radius * 1.5f, center.y - radius * 1.5f)
                )
            }

            // 5. Glossy Surface Highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.7f), Color.Transparent),
                    center = center.copy(x = center.x - radius * 0.45f, y = center.y - radius * 0.45f),
                    radius = radius * 0.4f
                ),
                radius = radius * 0.4f,
                center = center.copy(x = center.x - radius * 0.45f, y = center.y - radius * 0.45f)
            )
        }
    }
}
