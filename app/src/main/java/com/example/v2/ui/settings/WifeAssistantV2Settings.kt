package com.example.v2.ui.settings
import kotlinx.coroutines.launch

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings as AndroidSettings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.v2.ui.theme.*
import com.example.v2.voice.VoiceViewModel
import com.example.v2.voice.VoiceState
import com.example.v2.core.GeminiConnectionState
import com.example.v2.core.ServiceConnectionState
import com.example.hologram.HologramBubbleService
import com.example.hologram.WifeServiceManager
import com.example.hologram.WifeServiceState

enum class SettingsRoute {
    HOME, VOICE_MODELS, ORB_CUSTOMIZATION, API_CLOUD, CONNECTORS, PERMISSIONS, DIAGNOSTICS, DIAGNOSTICS_COMPATIBILITY, MEMORY, TOOLS, PC_CONTROL, AUTOMATION, SECURITY
}

@Composable
fun WifeAssistantV2Settings(
    viewModel: VoiceViewModel,
    apiCloudViewModel: ApiCloudViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    initialRoute: SettingsRoute = SettingsRoute.HOME,
    modifier: Modifier = Modifier
) {
    var currentRoute by remember { mutableStateOf(initialRoute) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkMidnightBlue)
            .statusBarsPadding()
    ) {
        Crossfade(targetState = currentRoute, label = "SettingsNav") { route ->
            when (route) {
                SettingsRoute.HOME -> SettingsHome(onNavigate = { currentRoute = it })
                SettingsRoute.VOICE_MODELS -> VoiceModelsSettings(onBack = { currentRoute = SettingsRoute.HOME })
                SettingsRoute.ORB_CUSTOMIZATION -> OrbCustomizationSettings(onBack = { currentRoute = SettingsRoute.HOME })
                SettingsRoute.API_CLOUD -> ApiCloudSettings(apiCloudViewModel, onBack = { currentRoute = SettingsRoute.HOME })
                SettingsRoute.CONNECTORS -> ConnectorsSettings(onBack = { currentRoute = SettingsRoute.HOME })
                SettingsRoute.PERMISSIONS -> PermissionsSettings(onBack = { currentRoute = SettingsRoute.HOME })
                SettingsRoute.DIAGNOSTICS -> DiagnosticsSettings(viewModel, onBack = { currentRoute = SettingsRoute.HOME })
                SettingsRoute.DIAGNOSTICS_COMPATIBILITY -> CompatibilityDiagnosticsScreen(onBack = { currentRoute = SettingsRoute.HOME })
                SettingsRoute.MEMORY -> MemorySettings(onBack = { currentRoute = SettingsRoute.HOME })
                SettingsRoute.TOOLS -> ToolsSettings(onBack = { currentRoute = SettingsRoute.HOME })
                SettingsRoute.PC_CONTROL -> PcControlSettings(onBack = { currentRoute = SettingsRoute.HOME })
                SettingsRoute.AUTOMATION -> AutomationSettings(onBack = { currentRoute = SettingsRoute.HOME })
                SettingsRoute.SECURITY -> SecuritySettings(onBack = { currentRoute = SettingsRoute.HOME })
            }
        }
    }
}

@Composable
fun SettingsHome(onNavigate: (SettingsRoute) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "♥ Wife AI", color = Cyan, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(text = "Settings", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "SujitHero", color = Violet, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { SettingsMenuCard("🎙 Voice & AI Models", "Manage assistant voice and AI models", onClick = { onNavigate(SettingsRoute.VOICE_MODELS) }) }
            item { SettingsMenuCard("✨ Orb Customization", "Customize Wife's AI orb and visual behavior", onClick = { onNavigate(SettingsRoute.ORB_CUSTOMIZATION) }) }
            item { SettingsMenuCard("☁ API & Cloud", "Configure Gemini and cloud services", onClick = { onNavigate(SettingsRoute.API_CLOUD) }) }
            item { SettingsMenuCard("🔗 Connectors", "Connect supported services", onClick = { onNavigate(SettingsRoute.CONNECTORS) }) }
            item { SettingsMenuCard("🔐 Permissions", "Manage Android permissions", onClick = { onNavigate(SettingsRoute.PERMISSIONS) }) }
            item { SettingsMenuCard("🩺 Voice Diagnostics", "Test microphone, service, Gemini and audio", onClick = { onNavigate(SettingsRoute.DIAGNOSTICS) }) }
            item { SettingsMenuCard("📱 Device Compatibility", "Check hardware capabilities and system limits", onClick = { onNavigate(SettingsRoute.DIAGNOSTICS_COMPATIBILITY) }) }
        }
    }
}

@Composable
fun SettingsMenuCard(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GlassSurface)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = "Open", tint = Cyan)
    }
}

@Composable
fun SettingsScreenHeader(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Cyan)
        }
        Column {
            Text(text = title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "♥ Wife AI", color = Violet, fontSize = 12.sp)
        }
    }
}

// Sub-screens implementations will follow...

@Composable
fun VoiceModelsSettings(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("wife_v2_prefs", Context.MODE_PRIVATE)
    val viewModel: VoiceViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    
    var persona by remember { mutableStateOf(prefs.getString("persona", "Girlfriend") ?: "Girlfriend") }
    var languageMode by remember { mutableStateOf(prefs.getString("language_mode", "AUTO_DETECT") ?: "AUTO_DETECT") }
    var preferredLanguage by remember { mutableStateOf(prefs.getString("preferred_language", "Bengali") ?: "Bengali") }
    var sweetTalkEngine by remember { mutableStateOf(prefs.getBoolean("sweet_talk_engine", true)) }
    var proactiveEngine by remember { mutableStateOf(prefs.getBoolean("proactive_engine", true)) }
    
    val languages = com.example.v2.language.LanguageManager.supportedLanguages
    var showLanguageDropdown by remember { mutableStateOf(false) }

    fun saveString(k: String, v: String) { prefs.edit().putString(k, v).apply() }
    fun saveBool(k: String, v: Boolean) { prefs.edit().putBoolean(k, v).apply() }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        SettingsScreenHeader("Voice & AI Models", onBack)
        LazyColumn(contentPadding = PaddingValues(bottom = 120.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                GlassSectionHeader("Persona")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    GlassSelectableChip("Girlfriend", persona == "Girlfriend", { persona = "Girlfriend"; saveString("persona", "Girlfriend") }, Modifier.weight(1f))
                    GlassSelectableChip("Bestie", persona == "Bestie", { persona = "Bestie"; saveString("persona", "Bestie") }, Modifier.weight(1f))
                }
            }
            item {
                GlassSectionHeader("Language Mode")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    GlassSelectableChip("Auto Detect", languageMode == "AUTO_DETECT", { languageMode = "AUTO_DETECT"; saveString("language_mode", "AUTO_DETECT") }, Modifier.weight(1f))
                    GlassSelectableChip("Manual", languageMode == "FIXED", { languageMode = "FIXED"; saveString("language_mode", "FIXED") }, Modifier.weight(1f))
                }
            }
            item {
                GlassSectionHeader("Preferred Language")
                Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(GlassSurface)
                            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                            .clickable { showLanguageDropdown = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = languages.find { it.name == preferredLanguage }?.nativeName ?: preferredLanguage, color = Color.White, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Cyan)
                    }
                    
                    DropdownMenu(
                        expanded = showLanguageDropdown,
                        onDismissRequest = { showLanguageDropdown = false },
                        modifier = Modifier.background(DarkMidnightBlue).border(1.dp, GlassBorder)
                    ) {
                        languages.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(text = lang.nativeName, color = Color.White) },
                                onClick = { 
                                    preferredLanguage = lang.name
                                    saveString("preferred_language", lang.name)
                                    showLanguageDropdown = false
                                }
                            )
                        }
                    }
                }
            }
            
            item {
                GlassSectionHeader("Voice Preview")
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { 
                        val previewText = when(preferredLanguage) {
                            "Bengali" -> "নমস্কার, আমি আপনার স্ত্রী এ আই সহকারী।"
                            "Hindi" -> "नमस्ते, मैं आपकी पत्नी एआई सहायक हूँ।"
                            else -> "Hello, I am your Wife AI assistant."
                        }
                        viewModel.previewVoice(previewText)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Violet)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Preview Voice")
                }
            }

            item {
                GlassSectionHeader("AI Engines")
                Spacer(modifier = Modifier.height(8.dp))
                GlassSwitchRow("Sweet Talk Engine", "Enables affectionate conversation styles", sweetTalkEngine, { sweetTalkEngine = it; saveBool("sweet_talk_engine", it) })
                Spacer(modifier = Modifier.height(8.dp))
                GlassSwitchRow("Proactive Engine", "Allows AI to start conversations", proactiveEngine, { proactiveEngine = it; saveBool("proactive_engine", it) })
            }
        }
    }
}

@Composable
fun OrbCustomizationSettings(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("wife_v2_prefs", Context.MODE_PRIVATE)
    
    var floatingOrbEnabled by remember { mutableStateOf(prefs.getBoolean("floating_orb_enabled", true)) }
    var ambientLight by remember { mutableStateOf(prefs.getBoolean("ambient_light", true)) }
    var interactiveWallpaper by remember { mutableStateOf(prefs.getBoolean("interactive_wallpaper", false)) }
    
    val canDrawOverlays = AndroidSettings.canDrawOverlays(context)
    
    fun saveBool(k: String, v: Boolean) { prefs.edit().putBoolean(k, v).apply() }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        SettingsScreenHeader("Orb Customization", onBack)
        LazyColumn(contentPadding = PaddingValues(bottom = 120.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                GlassSwitchRow("Floating AI Orb", "Show Wife AI as a floating crystal orb", floatingOrbEnabled, { 
                    floatingOrbEnabled = it
                    saveBool("floating_orb_enabled", it)
                })
                
                if (floatingOrbEnabled && !canDrawOverlays) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val intent = Intent(AndroidSettings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                    ) {
                        Text("Grant Overlay Permission", color = Color.White)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                GlassSwitchRow("Ambient Light", "Shows background glow around the Orb", ambientLight, { ambientLight = it; saveBool("ambient_light", it) })
                Spacer(modifier = Modifier.height(8.dp))
                GlassSwitchRow("Interactive Wallpaper", "Orb wallpaper reacts to touches", interactiveWallpaper, { interactiveWallpaper = it; saveBool("interactive_wallpaper", it) })
            }
        }
    }
}

@Composable
fun ApiCloudSettings(viewModel: ApiCloudViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    var isEditingGeminiKey by remember { mutableStateOf(false) }
    var apiKeyInput by remember { mutableStateOf("") }
    var showApiKey by remember { mutableStateOf(false) }
    
    val apiKey by viewModel.apiKey.collectAsState()
    val testResult by viewModel.testResult.collectAsState()
    val state by viewModel.connectionState.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        SettingsScreenHeader("API & Cloud", onBack)
        LazyColumn(contentPadding = PaddingValues(bottom = 120.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(GlassSurface).border(1.dp, GlassBorder, RoundedCornerShape(16.dp)).padding(16.dp)) {
                    Text("Gemini API", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val (statusText, statusColor) = when (state.geminiState) {
                        GeminiConnectionState.CONNECTING -> "● CONNECTING" to Color.Yellow
                        GeminiConnectionState.CONNECTED -> "● CONNECTED" to Cyan
                        GeminiConnectionState.FAILED -> "● ERROR" to NeonPink
                        GeminiConnectionState.RECONNECTING -> "● RECONNECTING" to Color.Yellow
                        GeminiConnectionState.DISCONNECTED -> "● DISCONNECTED" to Color.Gray
                    }
                    
                    Text(text = "Status: $statusText", color = statusColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    
                    if (state.geminiState == GeminiConnectionState.DISCONNECTED && apiKey.isBlank()) {
                        Text(text = "API key is not configured.", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    } else if (state.geminiState == GeminiConnectionState.CONNECTING) {
                        Text(text = "Connection test is currently running.", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    } else if (state.geminiState == GeminiConnectionState.CONNECTED) {
                        Text(text = "Real-time connection established.", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (isEditingGeminiKey) {
                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = { apiKeyInput = it },
                            label = { Text("Gemini API Key", color = Color.White.copy(alpha = 0.6f)) },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (showApiKey) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showApiKey = !showApiKey }) {
                                    Icon(
                                        imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle Visibility",
                                        tint = Cyan
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Cyan,
                                focusedBorderColor = Cyan,
                                unfocusedBorderColor = GlassBorder,
                                focusedContainerColor = GlassSurface,
                                unfocusedContainerColor = GlassSurface
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { 
                                    if (apiKeyInput.isNotBlank()) {
                                        viewModel.saveApiKey(apiKeyInput)
                                        isEditingGeminiKey = false
                                        apiKeyInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Cyan),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save Key", color = DarkMidnightBlue)
                            }
                            Button(
                                onClick = { isEditingGeminiKey = false; apiKeyInput = "" },
                                colors = ButtonDefaults.buttonColors(containerColor = GlassBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel", color = Color.White)
                            }
                        }
                    } else {
                        val displayKey = if (apiKey.isNotBlank()) "••••••••••••••••" else "Not Configured"
                        Text(text = "API Key: $displayKey", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { isEditingGeminiKey = true; apiKeyInput = "" },
                                colors = ButtonDefaults.buttonColors(containerColor = GlassBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Configure Key", color = Color.White)
                            }
                            Button(
                                onClick = { viewModel.testConnection() },
                                colors = ButtonDefaults.buttonColors(containerColor = Violet),
                                modifier = Modifier.weight(1f),
                                 enabled = state.geminiState != GeminiConnectionState.CONNECTING
                            ) {
                                if (state.geminiState == GeminiConnectionState.CONNECTING) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Text("Test Connection", color = Color.White)
                                }
                            }
                        }
                    }
                    
                    testResult?.let { result ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = result,
                            color = if (state.geminiState == GeminiConnectionState.CONNECTED) Cyan else NeonPink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            item {
                val elevenLabsKey by viewModel.elevenLabsKey.collectAsState()
                val elevenLabsTestResult by viewModel.elevenLabsTestResult.collectAsState()
                var isEditingElevenLabsKey by remember { mutableStateOf(false) }
                var elevenLabsKeyInput by remember { mutableStateOf("") }
                var showElevenLabsKey by remember { mutableStateOf(false) }

                Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(GlassSurface).border(1.dp, GlassBorder, RoundedCornerShape(16.dp)).padding(16.dp)) {
                    Text("ElevenLabs TTS", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val (statusText, statusColor) = when (state.elevenLabsState) {
                        ServiceConnectionState.CONNECTING -> "● CONNECTING" to Color.Yellow
                        ServiceConnectionState.CONNECTED -> "● READY" to Cyan
                        ServiceConnectionState.ERROR -> "● FAILED" to NeonPink
                        ServiceConnectionState.NOT_CONFIGURED -> "● NOT CONFIGURED" to Color.Gray
                        ServiceConnectionState.DISCONNECTED -> "● DISCONNECTED" to Color.Gray
                    }
                    
                    Text(text = "Status: $statusText", color = statusColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    
                    if (state.elevenLabsState == ServiceConnectionState.NOT_CONFIGURED) {
                        Text(text = "Configure an ElevenLabs API key to enable neural voice.", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (isEditingElevenLabsKey) {
                        OutlinedTextField(
                            value = elevenLabsKeyInput,
                            onValueChange = { elevenLabsKeyInput = it },
                            label = { Text("ElevenLabs API Key", color = Color.White.copy(alpha = 0.6f)) },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (showElevenLabsKey) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showElevenLabsKey = !showElevenLabsKey }) {
                                    Icon(
                                        imageVector = if (showElevenLabsKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle Visibility",
                                        tint = Cyan
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Cyan,
                                focusedBorderColor = Cyan,
                                unfocusedBorderColor = GlassBorder,
                                focusedContainerColor = GlassSurface,
                                unfocusedContainerColor = GlassSurface
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { 
                                    if (elevenLabsKeyInput.isNotBlank()) {
                                        viewModel.saveElevenLabsKey(elevenLabsKeyInput)
                                        isEditingElevenLabsKey = false
                                        elevenLabsKeyInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Cyan),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save Key", color = DarkMidnightBlue)
                            }
                            Button(
                                onClick = { isEditingElevenLabsKey = false; elevenLabsKeyInput = "" },
                                colors = ButtonDefaults.buttonColors(containerColor = GlassBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel", color = Color.White)
                            }
                        }
                    } else {
                        val displayKey = if (elevenLabsKey.isNotBlank()) "••••••••••••••••" else "Not Configured"
                        Text(text = "API Key: $displayKey", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { isEditingElevenLabsKey = true; elevenLabsKeyInput = "" },
                                colors = ButtonDefaults.buttonColors(containerColor = GlassBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Configure Key", color = Color.White)
                            }
                            Button(
                                onClick = { viewModel.testElevenLabsConnection() },
                                colors = ButtonDefaults.buttonColors(containerColor = Violet),
                                modifier = Modifier.weight(1f),
                                 enabled = state.elevenLabsState != ServiceConnectionState.CONNECTING
                            ) {
                                if (state.elevenLabsState == ServiceConnectionState.CONNECTING) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Text("Test Connection", color = Color.White)
                                }
                            }
                        }
                    }
                    
                    elevenLabsTestResult?.let { result ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = result,
                            color = if (state.elevenLabsState == ServiceConnectionState.CONNECTED) Cyan else NeonPink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            item {
                val weatherApiKey by viewModel.weatherApiKey.collectAsState()
                var isEditingWeatherKey by remember { mutableStateOf(false) }
                var weatherKeyInput by remember { mutableStateOf("") }
                var showWeatherKey by remember { mutableStateOf(false) }

                Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(GlassSurface).border(1.dp, GlassBorder, RoundedCornerShape(16.dp)).padding(16.dp)) {
                    Text("Weather API (OpenWeatherMap)", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(text = "Required for local weather card.", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (isEditingWeatherKey) {
                        OutlinedTextField(
                            value = weatherKeyInput,
                            onValueChange = { weatherKeyInput = it },
                            label = { Text("OpenWeatherMap API Key", color = Color.White.copy(alpha = 0.6f)) },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (showWeatherKey) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showWeatherKey = !showWeatherKey }) {
                                    Icon(
                                        imageVector = if (showWeatherKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle Visibility",
                                        tint = Cyan
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Cyan,
                                focusedBorderColor = Cyan,
                                unfocusedBorderColor = GlassBorder,
                                focusedContainerColor = GlassSurface,
                                unfocusedContainerColor = GlassSurface
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { 
                                    if (weatherKeyInput.isNotBlank()) {
                                        viewModel.saveWeatherApiKey(weatherKeyInput)
                                        isEditingWeatherKey = false
                                        weatherKeyInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Cyan),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save Key", color = DarkMidnightBlue)
                            }
                            Button(
                                onClick = { isEditingWeatherKey = false; weatherKeyInput = "" },
                                colors = ButtonDefaults.buttonColors(containerColor = GlassBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel", color = Color.White)
                            }
                        }
                    } else {
                        val displayKey = if (weatherApiKey.isNotBlank()) "••••••••••••••••" else "Not Configured"
                        Text(text = "API Key: $displayKey", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { isEditingWeatherKey = true; weatherKeyInput = "" },
                            colors = ButtonDefaults.buttonColors(containerColor = GlassBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Configure Weather API Key", color = Color.White)
                        }
                    }
                }
            }
            
            item {
                Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(GlassSurface).border(1.dp, GlassBorder, RoundedCornerShape(16.dp)).padding(16.dp)) {
                    Text("Cloud Sync", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Sync your memories and settings across devices.", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = GlassBorder)) {
                        Text("Connect Cloud Account", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectorsSettings(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("wife_v2_prefs", Context.MODE_PRIVATE)
    
    var socialMode by remember { mutableStateOf(prefs.getBoolean("social_mode", false)) }
    var autoReply by remember { mutableStateOf(prefs.getBoolean("auto_reply", false)) }
    var officeAssistant by remember { mutableStateOf(prefs.getBoolean("office_assistant", true)) }
    
    fun saveBool(k: String, v: Boolean) { prefs.edit().putBoolean(k, v).apply() }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        SettingsScreenHeader("Connectors", onBack)
        LazyColumn(contentPadding = PaddingValues(bottom = 120.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                GlassSwitchRow("Social Mode", "Read incoming messages", socialMode, { socialMode = it; saveBool("social_mode", it) })
                Spacer(modifier = Modifier.height(8.dp))
                GlassSwitchRow("Auto Reply", "Reply automatically to messages", autoReply, { autoReply = it; saveBool("auto_reply", it) })
                Spacer(modifier = Modifier.height(8.dp))
                GlassSwitchRow("Office Assistant", "Help with productivity tasks", officeAssistant, { officeAssistant = it; saveBool("office_assistant", it) })
            }
        }
    }
}

@Composable
fun PermissionsSettings(onBack: () -> Unit) {
    val context = LocalContext.current
    val permissionManager = remember { com.example.v2.core.WifeAssistantCore.getInstance(context).permissionManager }
    
    // Refresh permissions on resume/recomposition
    LaunchedEffect(Unit) {
        permissionManager.refreshPermissions()
    }
    
    val permissions by permissionManager.permissions.collectAsState()
    
    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionManager.refreshPermissions()
    }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionManager.refreshPermissions()
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        SettingsScreenHeader("Permissions", onBack)
        LazyColumn(contentPadding = PaddingValues(bottom = 120.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(permissions.size) { index ->
                val permission = permissions[index]
                PermissionCard(
                    title = permission.name,
                    isGranted = permission.state == com.example.v2.core.permission.PermissionState.GRANTED,
                    onGrant = { 
                        if (permission.id == "mic") {
                            micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else if (permission.id == "notifications" && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PermissionCard(title: String, isGranted: Boolean, onGrant: () -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GlassSurface)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isGranted) "Status: Granted" else "Status: Permission Required",
                color = if (isGranted) Cyan else NeonPink,
                fontSize = 12.sp
            )
        }
        if (!isGranted) {
            Button(onClick = onGrant, colors = ButtonDefaults.buttonColors(containerColor = Cyan)) {
                Text("Grant", color = DarkMidnightBlue)
            }
        } else {
            Button(onClick = {
                val intent = Intent(AndroidSettings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }, colors = ButtonDefaults.buttonColors(containerColor = GlassBorder)) {
                Text("Settings", color = Color.White)
            }
        }
    }
}

@Composable
fun DiagnosticsSettings(viewModel: VoiceViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val voiceState by viewModel.state.collectAsState()
    val coreState by com.example.v2.core.StateManager.state.collectAsState()
    
    val hasMic = ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        SettingsScreenHeader("Voice Diagnostics", onBack)
        LazyColumn(contentPadding = PaddingValues(bottom = 120.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                GlassSectionHeader("Voice Engine")
                DiagnosticItem("Microphone Permission", if (hasMic) "Granted" else "Denied", if (hasMic) Cyan else NeonPink)
                DiagnosticItem("Voice State", voiceState.displayText, Violet)
                DiagnosticItem("Gemini Session", coreState.geminiState.name, if (coreState.geminiState == GeminiConnectionState.CONNECTED) Cyan else NeonPink)
                if (voiceState is VoiceState.Error) {
                    DiagnosticItem("Last Error", (voiceState as VoiceState.Error).message, NeonPink)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                GlassSectionHeader("Core Engines")
                DiagnosticItem("Memory Engine", "OK", Cyan)
                DiagnosticItem("Tool Engine", "OK", Cyan)
                DiagnosticItem("PC Connection", coreState.pcState.name, if (coreState.pcState == ServiceConnectionState.CONNECTED) Cyan else NeonPink)
                DiagnosticItem("RGB Engine", "${coreState.rgbEffect}", Violet)
                DiagnosticItem("Active Tasks", "${coreState.activeTaskCount}", Violet)
                
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.testGeminiConnection(context) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan)
                ) {
                    Text("Run Connection Diagnostic", color = DarkMidnightBlue)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { 
                        viewModel.previewVoice("নমস্কার, আমি আপনার স্ত্রী এ আই। আমি এখন কথা বলতে পারছি।")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Violet)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test System Speech (Bengali)", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun DiagnosticItem(label: String, value: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        Text(text = value, color = color, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}


@Composable
fun GlassTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(visualTransformation = if (label.contains("API")) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None, 
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.White.copy(alpha = 0.6f)) },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Cyan,
            focusedBorderColor = Cyan,
            unfocusedBorderColor = GlassBorder,
            focusedContainerColor = GlassSurface,
            unfocusedContainerColor = GlassSurface
        ),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun GlassSectionHeader(title: String) {
    Text(
        text = title,
        color = Cyan,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp
    )
}

@Composable
fun GlassSelectableChip(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Cyan.copy(alpha = 0.2f) else GlassSurface)
            .border(1.dp, if (isSelected) Cyan else GlassBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Cyan else Color.White,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun GlassSwitchRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GlassSurface)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Violet,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.DarkGray
            )
        )
    }
}

@Composable
fun MemorySettings(onBack: () -> Unit) {
    val context = LocalContext.current
    val memoryEngine = remember { com.example.v2.core.WifeAssistantCore.getInstance(context).memoryEngine }
    val memories by memoryEngine.getAllMemories().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        SettingsScreenHeader("Memory Database", onBack)
        LazyColumn(contentPadding = PaddingValues(bottom = 120.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                DiagnosticItem("Memory Engine", "Active", Cyan)
                DiagnosticItem("Active Memories", "${memories.size} Items", Color.White.copy(alpha=0.7f))
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            items(memories.size) { index ->
                val memory = memories[index]
                GlassSwitchRow(
                    title = memory.content,
                    subtitle = "Category: ${memory.category}",
                    checked = memory.enabled,
                    onCheckedChange = { isChecked ->
                        coroutineScope.launch {
                            memoryEngine.updateMemoryStatus(memory, isChecked)
                        }
                    }
                )
            }
            
            item {
                if (memories.isEmpty()) {
                    Text("No memories saved yet. Try asking Wife AI to remember something.", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun ToolsSettings(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        SettingsScreenHeader("Tools & Commands", onBack)
        LazyColumn(contentPadding = PaddingValues(bottom = 120.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                GlassSwitchRow("Device Actions", "Allow control of flashlight, volume, etc.", true, { })
                Spacer(modifier = Modifier.height(8.dp))
                GlassSwitchRow("Navigation", "Allow opening maps and directions", true, { })
            }
        }
    }
}

@Composable
fun PcControlSettings(onBack: () -> Unit) {
    val state by com.example.v2.core.StateManager.state.collectAsState()
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        SettingsScreenHeader("PC Control", onBack)
        LazyColumn(contentPadding = PaddingValues(bottom = 120.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                DiagnosticItem("PC Status", state.pcState.name, if (state.pcState == ServiceConnectionState.CONNECTED) Cyan else NeonPink)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Cyan)) {
                    Text("Connect to PC", color = DarkMidnightBlue)
                }
            }
        }
    }
}

@Composable
fun AutomationSettings(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        SettingsScreenHeader("Automation", onBack)
        LazyColumn(contentPadding = PaddingValues(bottom = 120.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Text("No active tasks.", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun SecuritySettings(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        SettingsScreenHeader("Security", onBack)
        LazyColumn(contentPadding = PaddingValues(bottom = 120.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                GlassSwitchRow("Require Confirmation", "Always ask before sensitive actions", true, { })
            }
        }
    }
}

