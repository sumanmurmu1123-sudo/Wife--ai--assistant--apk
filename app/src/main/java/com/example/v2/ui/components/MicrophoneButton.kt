package com.example.v2.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.v2.voice.VoiceState
import com.example.v2.ui.theme.*

@Composable
fun MicrophoneButton(
    state: VoiceState,
    audioLevel: Float = 0f,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "MicTransition")
    
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (state is VoiceState.Listening || state is VoiceState.Speaking) 1.5f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RingScale"
    )

    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RingAlpha"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            // Expanding rings for listening/speaking
            if (state is VoiceState.Listening || state is VoiceState.Speaking) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(ringScale)
                        .clip(CircleShape)
                        .border(2.dp, Cyan.copy(alpha = ringAlpha), CircleShape)
                )
            }

            // Main Button
            val buttonBrush = when (state) {
                is VoiceState.Thinking -> Brush.sweepGradient(listOf(NeonPink, Violet, Cyan, NeonPink))
                is VoiceState.Speaking -> Brush.sweepGradient(listOf(Cyan, Violet, NeonPink, Cyan))
                else -> Brush.radialGradient(listOf(GlassSurface, DarkMidnightBlue))
            }

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(GlassSurface)
                    .border(
                        width = 2.dp,
                        brush = buttonBrush,
                        shape = CircleShape
                    )
                    .clickable(onClick = onClick)
                    .let {
                        if (state is VoiceState.Thinking || state is VoiceState.Speaking) {
                            it.rotate(rotation)
                        } else {
                            it
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Reverse rotation for icon so it stays upright if button rotates
                val iconModifier = if (state is VoiceState.Thinking || state is VoiceState.Speaking) {
                    Modifier.rotate(-rotation)
                } else Modifier
                
                when (state) {
                    is VoiceState.PermissionRequired -> {
                        Icon(imageVector = Icons.Default.MicOff, contentDescription = "মাইক্রোফোন অনুমতি দিন", tint = Color.White, modifier = iconModifier.size(32.dp))
                    }
                    is VoiceState.Disconnected, is VoiceState.Unavailable, is VoiceState.Idle, is VoiceState.Interrupted -> {
                        Icon(imageVector = Icons.Default.WifiOff, contentDescription = "ভয়েস সংযোগ বিচ্ছিন্ন", tint = Color.White, modifier = iconModifier.size(32.dp))
                    }
                    is VoiceState.Connecting, is VoiceState.Initializing, is VoiceState.Reconnecting -> {
                        Icon(imageVector = Icons.Default.Sync, contentDescription = "ভয়েস সংযোগ হচ্ছে", tint = Color.White, modifier = iconModifier.size(32.dp))
                    }
                    is VoiceState.Connected -> {
                        Icon(imageVector = Icons.Default.Link, contentDescription = "ভয়েস চালু করুন", tint = Color.White, modifier = iconModifier.size(32.dp))
                    }
                    is VoiceState.Listening -> {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = "শুনছি, থামাতে চাপুন", tint = Cyan, modifier = iconModifier.size(36.dp))
                    }
                    is VoiceState.Thinking -> {
                        Icon(imageVector = Icons.Default.Autorenew, contentDescription = "ভাবছি", tint = Violet, modifier = iconModifier.size(32.dp))
                    }
                    is VoiceState.Speaking -> {
                        Icon(imageVector = Icons.Default.GraphicEq, contentDescription = "কথা চলছে", tint = Cyan, modifier = iconModifier.size(36.dp))
                    }
                    is VoiceState.Error -> {
                        Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = "ভয়েস ত্রুটি, পুনরায় চেষ্টা করুন", tint = NeonPink, modifier = iconModifier.size(32.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = state.displayText,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
