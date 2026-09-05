package com.example.ui.call

import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.AssistantState
import com.example.data.AssistantMood
import com.example.ui.components.EmotionalEdgeLighting

@Composable
fun VideoCallScreen(
    state: AssistantState,
    bossName: String,
    onEndCall: () -> Unit,
    onToggleMute: () -> Unit,
    onFlipCamera: () -> Unit,
    isMuted: Boolean = false
) {
    var isVideoActive by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070710))
    ) {
        // 1. Fullscreen Main Feed: Wife Avatar Screen
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Animated Glow Aura
            EmotionalEdgeLighting(mood = AssistantMood.NEUTRAL, isActive = state == AssistantState.SPEAKING)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFFFF007F).copy(alpha = 0.5f), Color.Transparent)
                            )
                        )
                        .border(3.dp, Color(0xFFFF007F), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FaceRetouchingNatural,
                        contentDescription = "Wife Video Avatar",
                        tint = Color(0xFFFF4081),
                        modifier = Modifier.size(100.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Wife AI 💕",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (state == AssistantState.SPEAKING) "Talking to $bossName..." else "Listening to you...",
                    color = Color(0xFF00F5FF),
                    fontSize = 14.sp
                )
            }
        }

        // 2. Picture-in-Picture (PiP) Floating Self-Camera Window
        if (isVideoActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 16.dp, end = 16.dp)
                    .size(width = 110.dp, height = 160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF161626))
                    .border(2.dp, Color(0xFF00F5FF), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User Video Feed",
                    tint = Color.LightGray,
                    modifier = Modifier.size(44.dp)
                )
                Text(
                    text = bossName,
                    color = Color.White,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                )
            }
        }

        // 3. Call Controls Toolbar (Bottom HUD)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 32.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Toggle Mic Button
            CallActionRoundButton(
                icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                isActive = isMuted,
                activeColor = Color(0xFFFF5252),
                inactiveColor = Color(0xFF1E1E2E),
                onClick = onToggleMute
            )

            // End Video Call (Hang Up)
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF0044))
                    .clickable(onClick = onEndCall),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }

            // Flip Camera Button
            CallActionRoundButton(
                icon = Icons.Default.Cameraswitch,
                isActive = false,
                activeColor = Color(0xFF00F5FF),
                inactiveColor = Color(0xFF1E1E2E),
                onClick = onFlipCamera
            )

            // Toggle Video Button
            CallActionRoundButton(
                icon = if (isVideoActive) Icons.Default.Videocam else Icons.Default.VideocamOff,
                isActive = !isVideoActive,
                activeColor = Color(0xFFFF5252),
                inactiveColor = Color(0xFF1E1E2E),
                onClick = { isVideoActive = !isVideoActive }
            )
        }
    }
}

@Composable
fun CallActionRoundButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(if (isActive) activeColor else inactiveColor)
            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}
