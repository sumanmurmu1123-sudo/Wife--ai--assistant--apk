package com.example.v2.ui.pc

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.v2.ui.theme.Cyan
import com.example.v2.ui.theme.DarkMidnightBlue
import com.example.v2.ui.theme.NeonPink
import com.example.v2.ui.theme.Violet
import com.example.v2.voice.VoiceViewModel

@Composable
fun WifeAssistantV2Pc(viewModel: VoiceViewModel, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "PcTransition")
    
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RingScale"
    )

    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RingAlpha"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkMidnightBlue),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Cyan.copy(alpha = 0.15f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(500f, 1000f),
                        radius = 1500f
                    )
                )
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                // Expanding rings
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(ringScale)
                        .clip(CircleShape)
                        .border(2.dp, Cyan.copy(alpha = ringAlpha), CircleShape)
                )
                
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(listOf(Cyan, Violet, NeonPink, Cyan))
                        )
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10142A)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Computer,
                        contentDescription = "PC",
                        tint = Cyan,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Connect Wife to your PC",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Connection Status: Disconnected",
                color = Cyan,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
