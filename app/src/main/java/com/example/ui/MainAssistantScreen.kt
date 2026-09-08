package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AssistantState
import com.example.data.AssistantMood
import com.example.data.CompanionPersona
import com.example.ui.components.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainAssistantScreen(
    state: AssistantState,
    currentIp: String,
    onUpdateIp: (String) -> Unit,
    onShutdownPcClick: () -> Unit = {},
    onStartVoice: () -> Unit = {},
    onEndVoice: () -> Unit = {},
    isBorderLightActive: Boolean = false,
    bossName: String = "Boss",
    onUpdateBossName: (String) -> Unit = {},
    isSocialModeActive: Boolean = false,
    onToggleSocialMode: (Boolean) -> Unit = {},
    activePersona: CompanionPersona = CompanionPersona.GIRLFRIEND,
    onTogglePersona: (CompanionPersona) -> Unit = {},
    isVideoStudioActive: Boolean = false,
    onCloseVideoStudio: () -> Unit = {},
    onTriggerLaugh: () -> Unit = {},
    onTriggerAppDownload: () -> Unit = {},
    isVideoCallActive: Boolean = false,
    onStartVideoCall: () -> Unit = {},
    onEndVideoCall: () -> Unit = {},
    onToggleMute: () -> Unit = {},
    onFlipCamera: () -> Unit = {},
    isVoiceCallActive: Boolean = false,
    onStartVoiceCall: () -> Unit = {},
    onEndVoiceCall: () -> Unit = {},
    onToggleSpeaker: (Boolean) -> Unit = {},
    onTriggerGreeting: () -> Unit = {},
    isAutoReplyActive: Boolean = false,
    onToggleAutoReply: () -> Unit = {}
) {
    val isSystemReady = true
    val isMicPermissionGranted = true

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070510)) // Deep space black
    ) {
        // Subtle ambient neon glows
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF00FFFF).copy(alpha = 0.1f), Color.Transparent),
                    center = Offset(size.width * 0.2f, size.height * 0.2f),
                    radius = size.width * 0.8f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFF007F).copy(alpha = 0.08f), Color.Transparent),
                    center = Offset(size.width * 0.8f, size.height * 0.8f),
                    radius = size.width * 0.8f
                )
            )
        }

        CyberParticles()

        EmotionalEdgeLighting(
            mood = AssistantMood.NEUTRAL,
            isActive = isBorderLightActive || state == AssistantState.SPEAKING || state == AssistantState.LISTENING
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text(
                    text = "WIFE AI 💕",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Technical Debug Badges (English)
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(text = "GEMINI • CONNECTED", isActive = true)
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(text = "LATENCY 42ms", isActive = true, color = Color(0xFF00FF66))
                }
            }

            // Central AI Core Orb
            WifeAnimatedCore(
                state = state,
                onOrbTapped = {
                    if (state == AssistantState.IDLE) onStartVoice() else onEndVoice()
                }
            )

            // Bottom Section: Control Chips & Microphone
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                CyberAudioWave(
                    state = state
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Control Chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ControlChip(text = if (activePersona == CompanionPersona.GIRLFRIEND) "Girlfriend Mode" else "Bestie Mode", isActive = true, onClick = { onTogglePersona(if(activePersona == CompanionPersona.GIRLFRIEND) CompanionPersona.BEST_FRIEND else CompanionPersona.GIRLFRIEND) })
                    ControlChip(text = "Boss Mode", isActive = false, onClick = { })
                    ControlChip(text = "PC Sync", isActive = currentIp.isNotEmpty(), onClick = { })
                    ControlChip(text = "Security", isActive = true, onClick = { })
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Large Microphone Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF141020))
                        .border(
                            width = 2.dp,
                            brush = Brush.sweepGradient(
                                listOf(Color(0xFF00FFFF), Color(0xFF4285F4), Color(0xFF00FFFF))
                            ),
                            shape = CircleShape
                        )
                        .clickable(onClick = {
                            if (state == AssistantState.IDLE) onStartVoice() else onEndVoice()
                        }),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Microphone",
                        tint = if (state == AssistantState.LISTENING) Color(0xFF00FFFF) else Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun StatusBadge(text: String, isActive: Boolean, color: Color = Color(0xFF00F5FF)) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.4f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (isActive) color else Color.Gray)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = if (isActive) color else Color.Gray,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun ControlChip(text: String, isActive: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isActive) Color(0xFF1A1525) else Color(0xFF100C1A))
            .border(
                width = 1.dp,
                color = if (isActive) Color(0xFF9D00FF).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            color = if (isActive) Color.White else Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun WifeAnimatedCore(
    state: AssistantState,
    onOrbTapped: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbTransitions")

    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IdleBreathing"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (state == AssistantState.THINKING) 3000 else 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbRotation"
    )

    val waveAmplitude by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = if (state == AssistantState.SPEAKING) 40f else if (state == AssistantState.LISTENING) 25f else 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WaveAmplitude"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onOrbTapped
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasCenter = center
                val baseRadius = 85.dp.toPx()

                when (state) {
                    AssistantState.IDLE -> {
                        for (i in 0..2) {
                            val offsetAngle = Math.toRadians((rotationAngle + i * 120).toDouble())
                            val offsetX = (18.dp.toPx() * kotlin.math.cos(offsetAngle)).toFloat() * breathingScale
                            val offsetY = (18.dp.toPx() * kotlin.math.sin(offsetAngle)).toFloat() * breathingScale
                            
                            val colors = when(i) {
                                0 -> listOf(Color(0xFF00FFCC), Color.Transparent)
                                1 -> listOf(Color(0xFFFF007F), Color.Transparent)
                                else -> listOf(Color(0xFF9D00FF), Color.Transparent)
                            }

                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = colors,
                                    center = Offset(canvasCenter.x + offsetX, canvasCenter.y + offsetY),
                                    radius = baseRadius * 1.5f
                                ),
                                center = Offset(canvasCenter.x + offsetX, canvasCenter.y + offsetY),
                                radius = baseRadius * 1.5f,
                                blendMode = androidx.compose.ui.graphics.BlendMode.Screen
                            )
                        }
                        
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha=0.15f), Color.Transparent)
                            ),
                            radius = baseRadius * breathingScale
                        )
                    }

                    AssistantState.LISTENING -> {
                        val pulse = ((rotationAngle / 360f) * 2f) % 1f
                        for (i in 0..2) {
                            val ringScale = (pulse + (i * 0.33f)) % 1f
                            val alpha = (1f - ringScale).coerceIn(0f, 1f)
                            drawCircle(
                                color = Color(0xFF00FFFF).copy(alpha = alpha),
                                radius = baseRadius * (1f + ringScale * 1.2f),
                                style = Stroke(width = 4.dp.toPx())
                            )
                        }
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF00FFFF), Color(0xFF4285F4), Color.Transparent)
                            ),
                            radius = baseRadius * (1f + waveAmplitude / 100f)
                        )
                    }

                    AssistantState.THINKING -> {
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(Color.Transparent, Color(0xFF9D00FF), Color(0xFFFF007F), Color.Transparent)
                            ),
                            startAngle = rotationAngle,
                            sweepAngle = 240f,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round),
                            size = androidx.compose.ui.geometry.Size(baseRadius * 2.2f, baseRadius * 2.2f),
                            topLeft = Offset(canvasCenter.x - baseRadius * 1.1f, canvasCenter.y - baseRadius * 1.1f)
                        )
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(Color.Transparent, Color(0xFF00FFFF), Color(0xFF4285F4), Color.Transparent)
                            ),
                            startAngle = -rotationAngle * 1.5f,
                            sweepAngle = 180f,
                            useCenter = false,
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round),
                            size = androidx.compose.ui.geometry.Size(baseRadius * 1.8f, baseRadius * 1.8f),
                            topLeft = Offset(canvasCenter.x - baseRadius * 0.9f, canvasCenter.y - baseRadius * 0.9f)
                        )
                        
                        drawCircle(
                            color = Color(0xFF9D00FF).copy(alpha = 0.2f),
                            radius = baseRadius * 0.7f
                        )
                    }

                    AssistantState.SPEAKING -> {
                        val numPoints = 60
                        val path = androidx.compose.ui.graphics.Path()
                        for (i in 0 until numPoints) {
                            val angle = Math.toRadians((i * (360.0 / numPoints) + rotationAngle).toDouble())
                            val amp = if (i % 2 == 0) (waveAmplitude * 1.2f) else (waveAmplitude * 0.2f)
                            val r = baseRadius + amp
                            val x = canvasCenter.x + (r * kotlin.math.cos(angle)).toFloat()
                            val y = canvasCenter.y + (r * kotlin.math.sin(angle)).toFloat()
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        path.close()

                        drawPath(
                            path = path,
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFF007F), Color(0xFF9D00FF), Color.Transparent),
                                center = canvasCenter,
                                radius = baseRadius * 1.8f
                            )
                        )
                        
                        drawCircle(
                            color = Color(0xFFFF007F).copy(alpha = 0.4f),
                            radius = baseRadius * 1.1f
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        val stateText = when (state) {
            AssistantState.IDLE -> "প্রস্তুত"
            AssistantState.LISTENING -> "শুনছি… \uD83C\uDF99\uFE0F"
            AssistantState.THINKING -> "ভাবছি… \uD83E\uDDE0"
            AssistantState.SPEAKING -> "কথা বলছি… \uD83D\uDC95"
        }
        val stateColor = when (state) {
            AssistantState.IDLE -> Color(0xFF00FF66)
            AssistantState.LISTENING -> Color(0xFF00F5FF)
            AssistantState.THINKING -> Color(0xFF9D00FF)
            AssistantState.SPEAKING -> Color(0xFFFF0055)
        }
        
        Text(
            text = stateText,
            color = stateColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}
