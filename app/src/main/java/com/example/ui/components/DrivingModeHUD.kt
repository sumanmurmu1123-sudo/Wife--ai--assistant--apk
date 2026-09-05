package com.example.ui.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AssistantState

@Composable
fun DrivingModeHUD(
    state: AssistantState,
    onExitDrivingMode: () -> Unit,
    onVoiceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF030308))
            .padding(24.dp)
    ) {
        // Top Header with Exit Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = "Driving Mode",
                    tint = Color(0xFF00FF66),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "DRIVE MODE ACTIVE",
                    color = Color(0xFF00FF66),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }

            IconButton(
                onClick = onExitDrivingMode,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF1F1F2E))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Exit Drive Mode",
                    tint = Color.White
                )
            }
        }

        // Center Voice Trigger (Extra Large Button for Easy Tapping)
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF00FF66), Color(0xFF006622))
                        )
                    )
                    .border(4.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                    .clickable(onClick = onVoiceClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Talk while driving",
                    tint = Color.Black,
                    modifier = Modifier.size(72.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = when (state) {
                    AssistantState.LISTENING -> "Listening to you, Boss..."
                    AssistantState.SPEAKING -> "Wife is talking..."
                    else -> "Tap anywhere or say 'Wife' to command"
                },
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Bottom Shortcut Indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DrivingQuickTile(icon = Icons.Default.Navigation, label = "Navigation")
            DrivingQuickTile(icon = Icons.Default.PhoneInTalk, label = "Hands-Free")
            DrivingQuickTile(icon = Icons.Default.MusicNote, label = "Media")
        }
    }
}

@Composable
fun DrivingQuickTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF12121E))
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = Color(0xFF00F5FF), modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
