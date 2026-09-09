package com.example.v2.ui.talk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.v2.ui.components.HeartNode
import com.example.v2.ui.components.MicrophoneButton
import com.example.v2.ui.theme.DarkMidnightBlue
import com.example.v2.ui.theme.Violet
import com.example.v2.voice.VoiceViewModel

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@Composable
fun WifeAssistantV2Talk(
    viewModel: VoiceViewModel,
    modifier: Modifier = Modifier
) {
    val voiceState by viewModel.state.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkMidnightBlue)
    ) {
        // Immersive Glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Violet.copy(alpha = 0.3f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(500f, 1500f),
                        radius = 2000f
                    )
                )
        )

        // Top Status Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 24.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            val prefs = context.getSharedPreferences("wife_v2_prefs", android.content.Context.MODE_PRIVATE)
            val languageMode = prefs.getString("language_mode", "AUTO_DETECT") ?: "AUTO_DETECT"
            val displayMode = when (languageMode) {
                "FIXED" -> prefs.getString("preferred_language", "Bengali") ?: "Bengali"
                "MULTILINGUAL" -> "Multilingual"
                else -> "Auto Detect"
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                com.example.v2.ui.components.LanguageIndicator(modeName = displayMode)
                Spacer(modifier = Modifier.height(8.dp))
                
                val statusText = when (voiceState) {
                    is com.example.v2.voice.VoiceState.Listening -> "Listening..."
                    is com.example.v2.voice.VoiceState.Thinking -> "Thinking..."
                    is com.example.v2.voice.VoiceState.Speaking -> "Speaking..."
                    is com.example.v2.voice.VoiceState.Connecting -> "Connecting..."
                    is com.example.v2.voice.VoiceState.Error -> "Connection error"
                    else -> "Ready"
                }
                androidx.compose.material3.Text(
                    text = statusText,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }
        }

        // Center HeartNode
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            HeartNode(
                state = voiceState,
                modifier = Modifier.size(160.dp) // Larger in Talk screen
            )
        }

        // Bottom Microphone
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp), // Space for bottom nav
            contentAlignment = Alignment.BottomCenter
        ) {
            MicrophoneButton(
                state = voiceState,
                onClick = { viewModel.onMicrophoneTapped(context) }
            )
        }
    }
}
