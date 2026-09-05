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

            // ১. বাইরের গ্লোয়িং আলোর আভা (Outer Hologram Glow)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x6600F5D4), // Cyan Glow
                        Color(0x227B2CBF), // Purple Tint
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.8f * pulseScale
                ),
                radius = baseRadius * 1.8f * pulseScale,
                center = center
            )

            // ২. ভিতরের গ্লাস কোর (Hologram Core)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFE0AAFF),
                        Color(0xFF00BBF9),
                        Color(0xFF03045E)
                    ),
                    center = center,
                    radius = baseRadius
                ),
                radius = baseRadius * pulseScale,
                center = center
            )

            // ৩. এনার্জি অরবিটাল রিং (Rotating Energy Ring)
            drawCircle(
                color = Color(0xFF00F5D4),
                radius = baseRadius * 1.25f,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}
