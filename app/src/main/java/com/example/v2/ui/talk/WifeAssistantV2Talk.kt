package com.example.v2.ui.talk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifeAssistantV2Talk(
    viewModel: VoiceViewModel,
    modifier: Modifier = Modifier
) {
    val voiceState by viewModel.state.collectAsState()
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }

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
            modifier = Modifier.fillMaxSize().padding(bottom = 80.dp),
            contentAlignment = Alignment.Center
        ) {
            HeartNode(
                state = voiceState,
                modifier = Modifier.size(160.dp) // Larger in Talk screen
            )
        }

        // Bottom Controls
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 90.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MicrophoneButton(
                state = voiceState,
                onClick = { viewModel.onMicrophoneTapped(context) }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Text Fallback (in case mic fails)
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                placeholder = { Text("Or type a command...", color = Color.White.copy(alpha = 0.5f)) },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.White.copy(alpha = 0.1f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                    focusedIndicatorColor = Violet,
                    unfocusedIndicatorColor = Color.White.copy(alpha = 0.2f),
                    cursorColor = Violet
                ),
                shape = RoundedCornerShape(24.dp),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendTextCommand(inputText)
                                inputText = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Violet)
                    }
                }
            )
        }
    }
}
