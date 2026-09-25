package com.example.v2.ui.talk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
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
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val voiceState by viewModel.state.collectAsState()
    val audioLevel by viewModel.audioLevel.collectAsState()
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Rule: Restore overlay after dialog
        com.example.v2.core.StateManager.updateState { it.copy(overlayState = com.example.v2.core.OverlayState.VISIBLE) }
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
                .padding(top = 8.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            // Back Button
            androidx.compose.material3.IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.align(Alignment.TopStart).padding(start = 8.dp)
            ) {
                androidx.compose.material3.Icon(
                    androidx.compose.material.icons.Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

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
            val currentExpression by viewModel.currentExpression.collectAsState()
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                com.example.v2.ui.components.LanguageIndicator(modeName = displayMode)
                Spacer(modifier = Modifier.height(8.dp))
                
                androidx.compose.material3.Text(
                    text = "${voiceState.displayText} • ${currentExpression.uppercase()}",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Move Text Input to the Top
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    placeholder = { Text("Or type a command...", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp) },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.White.copy(alpha = 0.08f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.08f),
                        focusedIndicatorColor = Violet,
                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.2f),
                        cursorColor = Violet
                    ),
                    shape = RoundedCornerShape(24.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
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

                // Recent AI Message Preview at the top
                val messages by viewModel.messages.collectAsState()
                val lastAiMessage = messages.lastOrNull { !it.isFromUser }
                if (lastAiMessage != null && voiceState is com.example.v2.voice.VoiceState.Speaking) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Text(
                            text = lastAiMessage.text,
                            color = Color.White,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Center HeartNode
        Box(
            modifier = Modifier.fillMaxSize().padding(bottom = 80.dp).clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (voiceState is com.example.v2.voice.VoiceState.MicPermissionRequired) {
                    com.example.v2.core.StateManager.updateState { it.copy(overlayState = com.example.v2.core.OverlayState.SUSPENDED_FOR_PERMISSION) }
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
        // Removed text input from bottom
    }
}
