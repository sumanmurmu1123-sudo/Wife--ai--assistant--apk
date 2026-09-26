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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.v2.ui.theme.Cyan
import com.example.v2.ui.theme.Violet

import com.example.v2.core.RgbEngineState

@Composable
fun MayaCrystalOrb(
    audioLevel: Float,
    rgbState: RgbEngineState,
    onDrag: (Float, Float) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbInfinite")
    
    val isEngineActive = rgbState == RgbEngineState.ACTIVE || rgbState == RgbEngineState.STATIC
    
    // Breathing/Pulse
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isEngineActive) 3000 else 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbPulse"
    )

    // 3D-like Rotation - continuous when active
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isEngineActive) 4000 else 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbRotation"
    )

    // Vertical oscillation for floating effect
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbFloat"
    )

    // Reactive scale based on real audio amplitude
    val reactiveScale = if (isEngineActive) 1f + (audioLevel * 0.4f) else 1f

    Box(
        modifier = modifier
            .size(70.dp)
            .graphicsLayer {
                translationY = floatOffset
                rotationZ = if (isEngineActive) rotation * 0.2f else 0f
            }
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
            val radius = baseRadius * pulse * (if (isEngineActive) (1f + (audioLevel * 0.2f)) else 1f)

            // 1. Soft Glow / Halo (Ambient presence)
            // Reduced alpha or hidden if OFF/ERROR
            val glowAlpha = when(rgbState) {
                RgbEngineState.ACTIVE, RgbEngineState.STATIC -> 0.2f * reactiveScale
                RgbEngineState.STARTING -> 0.1f
                else -> 0f
            }
            
            if (glowAlpha > 0f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Cyan.copy(alpha = glowAlpha),
                            Violet.copy(alpha = glowAlpha * 0.5f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = baseRadius * 2.5f * reactiveScale
                    ),
                    radius = baseRadius * 2.5f * reactiveScale,
                    center = center
                )
            }

            // 2. Crystal Core (Glass Refraction effect)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isEngineActive) 0.45f else 0.2f),
                        Cyan.copy(alpha = if (isEngineActive) 0.25f else 0.1f),
                        Violet.copy(alpha = if (isEngineActive) 0.15f else 0.05f),
                        Color(0x11000000)
                    ),
                    center = center.copy(x = center.x - radius * 0.3f, y = center.y - radius * 0.3f),
                    radius = radius * 1.6f
                ),
                radius = radius,
                center = center
            )

            // 3. Inner Holographic Energy
            if (isEngineActive) {
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
                        alpha = if (rgbState == RgbEngineState.ACTIVE) (0.5f + (audioLevel * 0.5f)) else 0.3f
                    )
                }
            }

            // 4. Holographic HUD Rings
            if (isEngineActive) {
                withTransform({
                    rotate(-rotation * 0.8f, center)
                }) {
                    // Outer static-ish thin ring
                    drawCircle(
                        color = Cyan.copy(alpha = 0.2f + (if (rgbState == RgbEngineState.ACTIVE) audioLevel * 0.4f else 0f)),
                        radius = radius * 1.4f,
                        style = Stroke(width = 0.5.dp.toPx()),
                        center = center
                    )
                    
                    // Segmented rotating arcs - only visible if ACTIVE
                    if (rgbState == RgbEngineState.ACTIVE) {
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
                }
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
