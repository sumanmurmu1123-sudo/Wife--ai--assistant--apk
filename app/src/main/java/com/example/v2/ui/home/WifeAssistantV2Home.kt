package com.example.v2.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.v2.ui.components.HeartNode
import com.example.v2.ui.components.StatusStrip
import com.example.v2.ui.components.WifeSaysCard
import com.example.v2.ui.theme.Cyan
import com.example.v2.ui.theme.DarkMidnightBlue
import com.example.v2.ui.theme.GlassBorder
import com.example.v2.ui.theme.NeonPink
import com.example.v2.ui.theme.Violet
import com.example.v2.voice.VoiceViewModel
import com.example.v2.voice.VoiceState
import com.example.v2.ui.home.WeatherCard
import com.example.v2.ui.home.WeatherViewModel
import com.example.v2.ui.theme.*
import java.util.Calendar

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Settings
import android.os.Build
import android.provider.Settings

@Composable
fun WifeAssistantV2Home(
    viewModel: VoiceViewModel,
    weatherViewModel: WeatherViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    modifier: Modifier = Modifier,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToTools: () -> Unit = {}
) {
    val voiceState by viewModel.state.collectAsState()
    val appState by com.example.v2.core.StateManager.state.collectAsState()
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            weatherViewModel.refreshWeather()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.triggerFirstGreeting(context)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkMidnightBlue)
    ) {
        // Hero Background Glows
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Violet.copy(alpha = 0.2f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(500f, 1000f),
                        radius = 1500f
                    )
                )
        )

        // Avatar Image
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp), // space for bottom nav
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = com.example.R.drawable.wife_hero_neon_v1_1789749019555),
                contentDescription = "Wife Assistant Avatar",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.82f
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                DarkMidnightBlue.copy(alpha = 0.35f),
                                DarkMidnightBlue.copy(alpha = 0.65f),
                                DarkMidnightBlue.copy(alpha = 0.95f)
                            )
                        )
                    )
            )
        }

        // Floating Help Line Service badge matching the uploaded artwork
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 64.dp, end = 16.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            DarkMidnightBlue.copy(alpha = 0.85f),
                            Violet.copy(alpha = 0.35f)
                        )
                    )
                )
                .border(2.dp, Brush.sweepGradient(listOf(Cyan, Violet, NeonPink, Cyan)), CircleShape)
                .clickable {
                    viewModel.onMicrophoneTapped(context)
                }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "🎧", fontSize = 18.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Help Line",
                    color = Cyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Service",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                }
                IconButton(
                    onClick = onNavigateToTools,
                    modifier = Modifier.testTag("open_tools_button")
                ) {
                    Icon(Icons.Default.Build, contentDescription = "All Tools", tint = Cyan)
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(DarkMidnightBlue).border(1.dp, GlassBorder)
                ) {
                    DropdownMenuItem(
                        text = { Text("All Tools Engine", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Build, contentDescription = null, tint = Cyan) },
                        onClick = { 
                            showMenu = false
                            onNavigateToTools()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Accessibility Settings", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF00FF88)) },
                        onClick = { 
                            showMenu = false
                            val intent = android.content.Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "💗", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Wife",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            
            IconButton(onClick = { 
                onNavigateToProfile()
            }) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.wife_avatar_ultimate_neon_1789779678600),
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(1.dp, Cyan, CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
        }

        // Main Content Overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp, bottom = 120.dp), // keep space for nav
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val prefs = context.getSharedPreferences("wife_v2_prefs", android.content.Context.MODE_PRIVATE)
            val languageMode = prefs.getString("language_mode", "AUTO_DETECT") ?: "AUTO_DETECT"
            val displayMode = when (languageMode) {
                "FIXED" -> prefs.getString("preferred_language", "Bengali") ?: "Bengali"
                "MULTILINGUAL" -> "Multilingual"
                else -> "Auto Detect"
            }
            com.example.v2.ui.components.LanguageIndicator(modeName = displayMode)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Critical Warnings Section
            val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(context) else true
            val hasMic = context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED
            val hasApiKey = viewModel.state.collectAsState().value !is VoiceState.NotConfigured

            if (!hasOverlay || !hasMic || !hasApiKey) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        .background(NeonPink.copy(alpha = 0.15f))
                        .border(1.dp, NeonPink.copy(alpha = 0.4f), androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        .clickable {
                            if (!hasOverlay) {
                                val intent = android.content.Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    android.net.Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            } else if (!hasMic) {
                                viewModel.onMicrophoneTapped(context)
                            } else {
                                onNavigateToProfile() // Assuming profile/settings is where API Key is
                            }
                        }
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val warningText = when {
                        !hasOverlay -> "⚠️ Overlay Permission Missing (Click to Fix)"
                        !hasMic -> "🎙️ Microphone Permission Missing"
                        !hasApiKey -> "🔑 Gemini API Key Required"
                        else -> ""
                    }
                    Text(
                        text = warningText,
                        color = NeonPink,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            StatusStrip()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            WeatherCard(
                state = appState.weatherState,
                onRefresh = { 
                    weatherViewModel.refreshWeather()
                    Toast.makeText(context, "Updating weather...", Toast.LENGTH_SHORT).show()
                },
                onRequestPermission = { 
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Greeting & HeartNode
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val greeting = when (hour) {
                in 5..11 -> "Good morning, sujithero ☀️"
                in 12..17 -> "Hey, I'm right here \uD83D\uDC97"
                in 18..21 -> "Good evening, sujithero ✨"
                else -> "Good night, sujithero \uD83C\uDF19"
            }
            
            Text(
                text = greeting,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            val (secondaryText, secondaryColor) = when (voiceState) {
                is VoiceState.NotConfigured -> "CONNECT GEMINI" to NeonPink
                is VoiceState.Connecting, is VoiceState.Reconnecting -> "CONNECTING..." to Color.Yellow
                is VoiceState.Connected -> "READY" to Cyan
                is VoiceState.Listening -> "LISTENING... 🎙️" to Cyan
                is VoiceState.Thinking -> "THINKING... 🧠" to Violet
                is VoiceState.Speaking -> "SPEAKING... 💕" to NeonPink
                is VoiceState.Error -> (voiceState as VoiceState.Error).message to NeonPink
                else -> "DISCONNECTED" to Color.White.copy(alpha = 0.6f)
            }

            Text(
                text = secondaryText,
                color = secondaryColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            HeartNode(
                state = voiceState,
                modifier = Modifier.clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = { viewModel.onMicrophoneTapped(context) }
                )
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            WifeSaysCard(
                text = "Raat ho gayi...\nab so jao.\nSweet dreams \uD83C\uDF19",
                onReplayClick = {
                    viewModel.triggerAction("Replay", context)
                }
            )
            
        }
    }
}
