package com.example.hologram

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.withTransform

@Composable
fun GlowingHologramBubble(
    onDrag: (Float, Float) -> Unit,
    onClick: () -> Unit
) {
    // ব্রিদিং ও পালস অ্যানিমেশন
    val infiniteTransition = rememberInfiniteTransition(label = "HologramPulse")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RingRotation"
    )

    Box(
        modifier = Modifier
            .size(110.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            }
    ) {
        
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = this.center
            val baseRadius = size.minDimension / 3.2f

            // 1. Shadow / Outer Glow (Soft realistic shadow and depth)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x88000000), // Dark shadow center
                        Color(0x3300F5D4), // Cyan Glow edge
                        Color.Transparent
                    ),
                    center = center.copy(y = center.y + 10f),
                    radius = baseRadius * 1.9f * pulseScale
                ),
                radius = baseRadius * 1.9f * pulseScale,
                center = center.copy(y = center.y + 10f)
            )

            // 2. Crystal Transparent Glass Core with Cyan/Magenta reflection
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x99FFFFFF), // Glass highlight
                        Color(0x5500F5D4), // Cyan reflection
                        Color(0x44FF00FF), // Magenta reflection
                        Color(0x11FFFFFF)  // Glass edge
                    ),
                    center = center.copy(x = center.x - 15f, y = center.y - 15f),
                    radius = baseRadius * 1.2f
                ),
                radius = baseRadius * pulseScale,
                center = center
            )

            // 3. Rotating Energy Rings (Magenta / Cyan)
            withTransform({
                rotate(ringRotation, center)
            }) {
                drawArc(
                    color = Color(0xFF00F5D4), // Cyan
                    startAngle = 0f,
                    sweepAngle = 100f,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round),
                    size = androidx.compose.ui.geometry.Size(baseRadius * 2.5f, baseRadius * 2.5f),
                    topLeft = androidx.compose.ui.geometry.Offset(center.x - baseRadius * 1.25f, center.y - baseRadius * 1.25f)
                )
                drawArc(
                    color = Color(0xFFFF00FF), // Magenta
                    startAngle = 180f,
                    sweepAngle = 100f,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round),
                    size = androidx.compose.ui.geometry.Size(baseRadius * 2.5f, baseRadius * 2.5f),
                    topLeft = androidx.compose.ui.geometry.Offset(center.x - baseRadius * 1.25f, center.y - baseRadius * 1.25f)
                )
            }
        }

    }
}
