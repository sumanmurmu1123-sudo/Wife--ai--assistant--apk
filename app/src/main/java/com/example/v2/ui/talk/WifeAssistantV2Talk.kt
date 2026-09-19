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
import com.example.v2.ui.theme.DarkMidnightBlue
import com.example.v2.ui.theme.Violet
import com.example.v2.voice.VoiceViewModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifeAssistantV2Talk(
    viewModel: VoiceViewModel,
    modifier: Modifier = Modifier
) {
    val voiceState by viewModel.state.collectAsState()
    val audioLevel by viewModel.audioLevel.collectAsState()
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onMicrophoneTapped(context)
        }
    }

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
            val languageState by viewModel.languageState.collectAsState()
            val prefs = context.getSharedPreferences("wife_v2_prefs", android.content.Context.MODE_PRIVATE)
            val languageMode = prefs.getString("language_mode", "AUTO_DETECT") ?: "AUTO_DETECT"
            
            val displayMode = when (val state = languageState) {
                is com.example.v2.voice.LanguageState.Detected -> "AUTO • ${state.name.uppercase()}"
                is com.example.v2.voice.LanguageState.Manual -> state.name.uppercase()
                else -> {
                    if (languageMode == "AUTO_DETECT") "AUTO DETECT"
                    else (prefs.getString("preferred_language", "Bengali") ?: "Bengali").uppercase()
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                com.example.v2.ui.components.LanguageIndicator(modeName = displayMode)
                Spacer(modifier = Modifier.height(8.dp))
                
                androidx.compose.material3.Text(
                    text = voiceState.displayText,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }
        }

        // Center HeartNode
        Box(
            modifier = Modifier.fillMaxSize().padding(bottom = 80.dp).clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (voiceState is com.example.v2.voice.VoiceState.PermissionRequired) {
                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                } else {
                    viewModel.onMicrophoneTapped(context)
                }
            },
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
                                viewModel.sendTextCommand(inputText, context)
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
