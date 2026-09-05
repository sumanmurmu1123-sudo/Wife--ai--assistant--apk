package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AssistantState

@Composable
fun GameMoodHUD(
    state: AssistantState,
    onExitGameMood: () -> Unit,
    onVoiceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "NeonGamePulse")
    val neonGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Glow"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF03010A))
    ) {
        // Cyber Grid Lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF8A2BE2).copy(alpha = 0.25f * neonGlow),
                        Color(0xFF00FF66).copy(alpha = 0.1f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.minDimension * 0.9f
                ),
                radius = size.minDimension * 0.9f
            )
        }

        // Top Gaming Telemetry
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassmorphicHudChip(label = "PROFILE", value = "TURBO", accentColor = Color(0xFF00FF66))
                GlassmorphicHudChip(label = "DND", value = "SHIELDED", accentColor = Color(0xFFFF0055))
            }

            IconButton(
                onClick = onExitGameMood,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E102A))
                    .border(1.dp, Color(0xFFFF0055), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Exit Game Mode", tint = Color.White)
            }
        }

        // Center Gaming HUD Core
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "GAMING PROTOCOL",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 6.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF00FF66)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Gaming Neon Trigger Orb
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF8A2BE2), Color(0xFF0D031A))
                        )
                    )
                    .border(
                        3.dp,
                        Brush.sweepGradient(listOf(Color(0xFF00FF66), Color(0xFFFF007F), Color(0xFF00FF66))),
                        CircleShape
                    )
                    .clickable(onClick = onVoiceClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = "Game Assistant Mic",
                    tint = Color.White,
                    modifier = Modifier.size(70.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when (state) {
                    AssistantState.LISTENING -> "Listening tactical comms..."
                    AssistantState.SPEAKING -> "Wife: On your six, Boss!"
                    else -> "Tap for Tactical Voice Command"
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFD4B2FF)
            )
        }

        // Bottom Quick Launch Tiles (Discord, Steam, Stream)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            GameShortcutChip(title = "Steam", icon = Icons.Default.Games)
            GameShortcutChip(title = "Discord", icon = Icons.Default.HeadsetMic)
            GameShortcutChip(title = "Boost", icon = Icons.Default.Bolt)
        }
    }
}

@Composable
fun GameShortcutChip(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF140B24))
            .border(1.dp, Color(0xFF8A2BE2).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = title, tint = Color(0xFF00FF66), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
