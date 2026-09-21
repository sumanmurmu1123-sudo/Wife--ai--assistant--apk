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
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.geometry.Offset
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
import com.example.data.UserPreferences
import com.example.v2.core.GeminiConnectionState
import com.example.v2.core.ServiceConnectionState
import com.example.v2.core.RgbEngineState
import com.example.v2.core.RgbEngineMode
import com.example.hologram.HologramBubbleService
import com.example.hologram.WifeServiceManager
import com.example.hologram.WifeServiceState

enum class SettingsRoute {
    HOME, VOICE_MODELS, ORB_CUSTOMIZATION, API_CLOUD, CONNECTORS, PERMISSIONS, DIAGNOSTICS, DIAGNOSTICS_COMPATIBILITY, MEMORY, TOOLS, PC_CONTROL, AUTOMATION, SECURITY, RGB_CONTROL, SUJITHERO_PREMIUM
}

@Composable
fun WifeAssistantV2Settings(
    viewModel: VoiceViewModel,
    apiCloudViewModel: ApiCloudViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    initialRoute: SettingsRoute = SettingsRoute.HOME,
    modifier: Modifier = Modifier
) {
    var currentRoute by remember(initialRoute) { mutableStateOf(initialRoute) }
    
    // Handle system back button to navigate to Settings Home first
    androidx.activity.compose.BackHandler(enabled = currentRoute != SettingsRoute.HOME) {
        currentRoute = SettingsRoute.HOME
    }
    
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
                SettingsRoute.RGB_CONTROL -> RgbControlSettings(onBack = { currentRoute = SettingsRoute.HOME })
                SettingsRoute.SUJITHERO_PREMIUM -> SujitHeroPremiumSettings(onBack = { currentRoute = SettingsRoute.HOME })
            }
        }
    }
}

@Composable
fun SettingsHome(onNavigate: (SettingsRoute) -> Unit) {
    val menuItems = listOf(
        SettingsItem("🎙", "Voice & AI", "Expressive Gemini Live models", SettingsRoute.VOICE_MODELS, Cyan),
        SettingsItem("✨", "Visual Orb", "Hologram and 3D visual behavior", SettingsRoute.ORB_CUSTOMIZATION, Violet),
        SettingsItem("☁️", "API & Cloud", "Gemini and ElevenLabs keys", SettingsRoute.API_CLOUD, Color.Magenta),
        SettingsItem("🌈", "RGB Lights", "Real-state engine configuration", SettingsRoute.RGB_CONTROL, Color(0xFF00FF88)),
        SettingsItem("🖥", "PC Control", "Windows Agent bridge status", SettingsRoute.PC_CONTROL, Color.Yellow),
        SettingsItem("🔗", "Connectors", "Linked accounts and services", SettingsRoute.CONNECTORS, Color.White),
        SettingsItem("🩺", "Diagnostics", "System integrity and health", SettingsRoute.DIAGNOSTICS, Color.Green),
        SettingsItem("🔐", "Security", "Permissions and data privacy", SettingsRoute.PERMISSIONS, NeonPink),
        SettingsItem("👑", "SujitHero Settings", "Exclusive developer controls", SettingsRoute.SUJITHERO_PREMIUM, Color(0xFFFFD700))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "SYSTEM", 
                color = Cyan, 
                fontSize = 32.sp, 
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "CONFIG", 
                color = Color.White.copy(alpha = 0.5f), 
                fontSize = 18.sp, 
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(menuItems) { index, item ->
                StaggeredMenuItem(index = index) {
                    NewStyleMenuCard(item) { onNavigate(item.route) }
                }
            }
        }
        
        // About Section with Version
        Spacer(modifier = Modifier.height(24.dp))
        AboutSection()
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun AboutSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GlassSurface.copy(alpha = 0.05f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Wife AI Assistant", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text("Version 4.05", color = Cyan, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Developed by SujitHero", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
    }
}

@Composable
fun SujitHeroPremiumSettings(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("wife_v2_prefs", Context.MODE_PRIVATE)
    val userPrefs = remember { UserPreferences(context) }
    
    var advancedDebugging by remember { mutableStateOf(prefs.getBoolean("advanced_debugging", true)) }
    var experimentalUi by remember { mutableStateOf(prefs.getBoolean("experimental_ui", false)) }
    var hyperSpeedMode by remember { mutableStateOf(prefs.getBoolean("hyper_speed_mode", true)) }
    
    var engineerName by remember { mutableStateOf(userPrefs.preferredEngineerName) }
    var engineerPhone by remember { mutableStateOf(userPrefs.preferredEngineerPhone) }
    
    fun saveBool(k: String, v: Boolean) { prefs.edit().putBoolean(k, v).apply() }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        SettingsScreenHeader("SujitHero Premium", onBack)
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 120.dp)) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFFFD700).copy(alpha = 0.1f), Color.Transparent)
                            )
                        )
                        .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👑", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Master Access", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Welcome SujitHero! You have exclusive access to core system overrides and experimental features.",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            item {
                GlassSectionHeader("Preferred Engineer")
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(GlassSurface)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = engineerName,
                        onValueChange = { 
                            engineerName = it
                            userPrefs.preferredEngineerName = it
                        },
                        label = { Text("Engineer Name", color = Color.White.copy(alpha = 0.6f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Cyan,
                            focusedBorderColor = Cyan,
                            unfocusedBorderColor = GlassBorder
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    OutlinedTextField(
                        value = engineerPhone,
                        onValueChange = { 
                            engineerPhone = it
                            userPrefs.preferredEngineerPhone = it
                        },
                        label = { Text("Engineer Phone", color = Color.White.copy(alpha = 0.6f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Cyan,
                            focusedBorderColor = Cyan,
                            unfocusedBorderColor = GlassBorder
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
            
            item {
                GlassSectionHeader("Developer Tools")
                Spacer(modifier = Modifier.height(8.dp))
                GlassSwitchRow("Advanced Debugging", "Enable verbose system logs", advancedDebugging, { 
                    advancedDebugging = it
                    saveBool("advanced_debugging", it)
                })
                Spacer(modifier = Modifier.height(8.dp))
                GlassSwitchRow("Experimental UI", "Try new interface layouts", experimentalUi, { 
                    experimentalUi = it
                    saveBool("experimental_ui", it)
                })
                Spacer(modifier = Modifier.height(8.dp))
                GlassSwitchRow("Hyper-Speed Mode", "Minimize response latency", hyperSpeedMode, { 
                    hyperSpeedMode = it
                    saveBool("hyper_speed_mode", it)
                })
            }
            
            item {
                GlassSectionHeader("System Overrides")
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { 
                        android.widget.Toast.makeText(context, "Neural cache rebuild started...", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700).copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Rebuild Neural Cache", color = Color(0xFFFFD700))
                }
            }
        }
    }
}

data class SettingsItem(
    val icon: String,
    val title: String,
    val subtitle: String,
    val route: SettingsRoute,
    val accentColor: Color
)

@Composable
fun StaggeredMenuItem(index: Int, content: @Composable () -> Unit) {
    val visible = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 50L)
        visible.value = true
    }

    androidx.compose.animation.AnimatedVisibility(
        visible = visible.value,
        enter = androidx.compose.animation.fadeIn(animationSpec = tween(500)) + 
                androidx.compose.animation.slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = tween(500, easing = androidx.compose.animation.core.EaseOutQuart)
                )
    ) {
        content()
    }
}

@Composable
fun NewStyleMenuCard(item: SettingsItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(GlassSurface.copy(alpha = 0.1f))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(GlassBorder, Color.Transparent, item.accentColor.copy(alpha = 0.3f))
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick)
    ) {
        // Subtle background glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = item.accentColor.copy(alpha = 0.05f),
                radius = size.width / 2,
                center = Offset(size.width, size.height / 2)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Circle
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(item.accentColor.copy(alpha = 0.1f))
                    .border(1.dp, item.accentColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.icon, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = item.subtitle,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = item.accentColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun RgbControlSettings(onBack: () -> Unit) {
    val context = LocalContext.current
    val core = com.example.v2.core.WifeAssistantCore.getInstance(context)
    val rgbEngine = core.rgbEngine
    val state by com.example.v2.core.StateManager.state.collectAsState()
    
    val currentEffectName = state.rgbEffect ?: "STATIC"
    val currentColor = state.rgbColor
    val currentStatus = state.rgbState
    val isHardware = state.rgbMode == com.example.v2.core.RgbEngineMode.HARDWARE

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        SettingsScreenHeader("RGB Engine", onBack)
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 120.dp)) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(GlassSurface)
                        .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(if (currentStatus != com.example.v2.core.RgbEngineState.OFF) Color(currentColor) else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Engine Status: ${currentStatus.name}",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isHardware) "● Hardware Sync Active" else "○ Software Simulation Mode",
                        color = if (isHardware) Cyan else Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }
            }

            item {
                GlassSectionHeader("Controls")
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { rgbEngine.start() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Cyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Power On", color = DarkMidnightBlue)
                    }
                    Button(
                        onClick = { rgbEngine.stop() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GlassBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Power Off", color = Color.White)
                    }
                }
            }

            item {
                GlassSectionHeader("Lighting Effects")
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    com.example.v2.core.rgb.RgbEffect.values().forEach { effect ->
                        val isSelected = currentEffectName == effect.name
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Cyan.copy(alpha = 0.1f) else GlassSurface)
                                .border(1.dp, if (isSelected) Cyan else GlassBorder, RoundedCornerShape(12.dp))
                                .clickable { rgbEngine.setEffect(effect) }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when(effect) {
                                    com.example.v2.core.rgb.RgbEffect.STATIC -> Icons.Default.Circle
                                    com.example.v2.core.rgb.RgbEffect.BREATHING -> Icons.Default.Air
                                    com.example.v2.core.rgb.RgbEffect.RAINBOW -> Icons.Default.Looks
                                    com.example.v2.core.rgb.RgbEffect.PULSE -> Icons.Default.Favorite
                                    com.example.v2.core.rgb.RgbEffect.VOICE_REACTIVE -> Icons.Default.Mic
                                },
                                contentDescription = null,
                                tint = if (isSelected) Cyan else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = effect.name.replace("_", " "),
                                color = if (isSelected) Cyan else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.Check, contentDescription = null, tint = Cyan, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            item {
                GlassSectionHeader("Core Color")
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val colors = listOf(0xFF00FFFF, 0xFFFF00FF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000, 0xFFFFFFFF)
                    colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(color.toInt()))
                                .border(
                                    width = if (currentColor == color.toInt()) 3.dp else 1.dp,
                                    color = if (currentColor == color.toInt()) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { rgbEngine.setColor(color.toInt()) }
                        )
                    }
                }
            }
        }
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
    val userPrefs = remember { com.example.data.UserPreferences(context) }
    val viewModel: VoiceViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    
    var selectedGeminiVoice by remember { mutableStateOf(userPrefs.selectedVoiceSlate) }
    var persona by remember { mutableStateOf(prefs.getString("persona", "Girlfriend") ?: "Girlfriend") }
    var languageMode by remember { mutableStateOf(prefs.getString("language_mode", "AUTO_DETECT") ?: "AUTO_DETECT") }
    var preferredLanguage by remember { mutableStateOf(prefs.getString("preferred_language", "Bengali") ?: "Bengali") }
    var sweetTalkEngine by remember { mutableStateOf(prefs.getBoolean("sweet_talk_engine", true)) }
    var proactiveEngine by remember { mutableStateOf(prefs.getBoolean("proactive_engine", true)) }
    var neuralVoiceEnabled by remember { mutableStateOf(prefs.getBoolean("neural_voice_enabled", true)) }
    
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
                GlassSectionHeader("Gemini Live Voice")
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(com.example.data.VoiceSlate.values()) { voice ->
                        val isSelected = selectedGeminiVoice == voice
                        Column(
                            modifier = Modifier
                                .width(130.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) voice.themeColor.copy(alpha = 0.15f) else GlassSurface)
                                .border(
                                    1.dp, 
                                    if (isSelected) voice.themeColor else GlassBorder, 
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { 
                                    selectedGeminiVoice = voice
                                    userPrefs.selectedVoiceSlate = voice
                                }
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(voice.themeColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                val icon = when(voice.voiceName) {
                                    "Aoede" -> "🌺"
                                    "Kore" -> "👸"
                                    "Charon" -> "🧔"
                                    "Puck" -> "⚡"
                                    "Fenrir" -> "🏢"
                                    else -> "🎙️"
                                }
                                Text(icon, fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = voice.displayName.split(" ")[0], 
                                color = Color.White, 
                                fontWeight = FontWeight.Bold, 
                                fontSize = 15.sp,
                                maxLines = 1
                            )
                            Text(
                                text = voice.tag.split(" /")[0], 
                                color = Color.White.copy(alpha = 0.5f), 
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
            
            item {
                GlassSectionHeader("Neural Voice")
                Spacer(modifier = Modifier.height(8.dp))
                GlassSwitchRow("High-Fidelity Neural Voice", "Use ElevenLabs for human-like speech", neuralVoiceEnabled, { neuralVoiceEnabled = it; saveBool("neural_voice_enabled", it) })
                if (neuralVoiceEnabled) {
                    Text(
                        text = "Requires ElevenLabs API Key in API & Cloud settings.",
                        color = Cyan.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                    )
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
                val secureStorage = remember { com.example.v2.core.WifeAssistantCore.getInstance(context).secureStorage }
                var selectedVoiceId by remember { mutableStateOf(secureStorage.getElevenLabsVoiceId()) }
                var showVoiceDropdown by remember { mutableStateOf(false) }
                
                val femaleVoices = listOf(
                    "Rachel (Default)" to "21m00Tcm4TlvDq8ikWAM",
                    "Bella (Soft)" to "EXAVITQu4vr4xnNLXboY",
                    "Elli (Young)" to "MF3mGyEYCl7XYW7L51Af",
                    "Mimi (Playful)" to "zrHiDhphv9ZnVXBqCLjz"
                )

                GlassSectionHeader("Voice Selection")
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(GlassSurface)
                            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                            .clickable { showVoiceDropdown = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = femaleVoices.find { it.second == selectedVoiceId }?.first ?: "Custom Voice",
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Cyan)
                    }

                    DropdownMenu(
                        expanded = showVoiceDropdown,
                        onDismissRequest = { showVoiceDropdown = false },
                        modifier = Modifier.background(DarkMidnightBlue).border(1.dp, GlassBorder)
                    ) {
                        femaleVoices.forEach { (name, id) ->
                            DropdownMenuItem(
                                text = { Text(text = name, color = Color.White) },
                                onClick = {
                                    selectedVoiceId = id
                                    secureStorage.saveElevenLabsVoiceId(id)
                                    showVoiceDropdown = false
                                }
                            )
                        }
                    }
                }
                Text(
                    text = "Neural voice selection only applies when ElevenLabs is enabled.",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                )
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
                    
                    val (statusText, statusColor) = when (state.voiceSessionState) {
                        com.example.v2.core.VoiceSessionState.CONNECTING -> "● CONNECTING" to Color.Yellow
                        com.example.v2.core.VoiceSessionState.CONNECTED -> "● CONNECTED" to Cyan
                        com.example.v2.core.VoiceSessionState.ERROR -> "● ERROR" to NeonPink
                        com.example.v2.core.VoiceSessionState.DISCONNECTED -> "● DISCONNECTED" to Color.Gray
                        com.example.v2.core.VoiceSessionState.MIC_INITIALIZING -> "● MIC INIT" to Color.Yellow
                        com.example.v2.core.VoiceSessionState.LISTENING -> "● LISTENING" to Cyan
                        com.example.v2.core.VoiceSessionState.THINKING -> "● THINKING" to Color.White
                        com.example.v2.core.VoiceSessionState.SPEAKING -> "● SPEAKING" to Color.Magenta
                    }
                    
                    Text(text = "Status: $statusText", color = statusColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    
                    if (state.voiceSessionState == com.example.v2.core.VoiceSessionState.DISCONNECTED && apiKey.isBlank()) {
                        Text(text = "API key is not configured.", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    } else if (state.voiceSessionState == com.example.v2.core.VoiceSessionState.CONNECTING) {
                        Text(text = "Establishing WebSocket session...", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    } else if (state.voiceSessionState != com.example.v2.core.VoiceSessionState.DISCONNECTED && state.voiceSessionState != com.example.v2.core.VoiceSessionState.ERROR) {
                        Text(text = "Real-time session active.", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
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
                                colors = ButtonDefaults.buttonColors(containerColor = if (apiKey.isBlank()) Cyan else GlassBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (apiKey.isBlank()) "Add API Key" else "Change Key", color = if (apiKey.isBlank()) DarkMidnightBlue else Color.White)
                            }
                            Button(
                                onClick = { viewModel.testConnection() },
                                colors = ButtonDefaults.buttonColors(containerColor = Violet),
                                modifier = Modifier.weight(1f),
                                 enabled = state.geminiState != GeminiConnectionState.CONNECTING && apiKey.isNotBlank()
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
                        if (result.startsWith("ERROR")) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = result,
                                color = NeonPink,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
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
                                colors = ButtonDefaults.buttonColors(containerColor = if (elevenLabsKey.isBlank()) Cyan else GlassBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (elevenLabsKey.isBlank()) "Add API Key" else "Change Key", color = if (elevenLabsKey.isBlank()) DarkMidnightBlue else Color.White)
                            }
                            Button(
                                onClick = { viewModel.testElevenLabsConnection() },
                                colors = ButtonDefaults.buttonColors(containerColor = Violet),
                                modifier = Modifier.weight(1f),
                                 enabled = state.elevenLabsState != ServiceConnectionState.CONNECTING && elevenLabsKey.isNotBlank()
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
                        if (result.startsWith("ERROR")) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = result,
                                color = NeonPink,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Cloud Sync", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        
                        val syncState = state.cloudSyncState
                        val indicatorColor = when (syncState) {
                            com.example.v2.core.CloudSyncState.Disconnected -> Color.Gray
                            com.example.v2.core.CloudSyncState.Connecting -> Color.Yellow
                            com.example.v2.core.CloudSyncState.Connected -> Cyan
                            is com.example.v2.core.CloudSyncState.Syncing -> Cyan
                            is com.example.v2.core.CloudSyncState.Synced -> Color.Green
                            is com.example.v2.core.CloudSyncState.Error -> NeonPink
                            com.example.v2.core.CloudSyncState.Offline -> Color.Red
                            com.example.v2.core.CloudSyncState.AuthRequired -> Color.Yellow
                        }
                        
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(indicatorColor))
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Sync your memories and settings across devices.", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val syncState = state.cloudSyncState
                    
                    when (syncState) {
                        com.example.v2.core.CloudSyncState.Disconnected, com.example.v2.core.CloudSyncState.AuthRequired -> {
                            Button(
                                onClick = { viewModel.connectCloud(context) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = GlassBorder),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Connect Cloud Account", color = Color.White)
                            }
                        }
                        com.example.v2.core.CloudSyncState.Connecting -> {
                            Button(
                                onClick = { },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = GlassBorder),
                                enabled = false,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Connecting...", color = Color.White)
                            }
                        }
                        com.example.v2.core.CloudSyncState.Connected, is com.example.v2.core.CloudSyncState.Synced, is com.example.v2.core.CloudSyncState.Error, com.example.v2.core.CloudSyncState.Offline -> {
                            Column {
                                if (state.cloudUserEmail != null) {
                                    Text(text = "Account: ${state.cloudUserEmail}", color = Cyan.copy(alpha = 0.7f), fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                
                                if (syncState is com.example.v2.core.CloudSyncState.Synced) {
                                    val date = java.util.Date(syncState.syncedAt)
                                    val format = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
                                    Text(text = "Last synced: ${format.format(date)}", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                                } else if (syncState is com.example.v2.core.CloudSyncState.Error) {
                                    Text(text = "Error: ${syncState.message}", color = NeonPink, fontSize = 11.sp)
                                } else if (syncState == com.example.v2.core.CloudSyncState.Offline) {
                                    Text(text = "Device is offline.", color = Color.Red.copy(alpha = 0.7f), fontSize = 11.sp)
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.syncNow() },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Cyan),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(if (syncState is com.example.v2.core.CloudSyncState.Error) "Retry Sync" else "Sync Now", color = DarkMidnightBlue)
                                    }
                                    
                                    Button(
                                        onClick = { viewModel.disconnectCloud() },
                                        modifier = Modifier.weight(0.5f),
                                        colors = ButtonDefaults.buttonColors(containerColor = GlassBorder.copy(alpha = 0.3f)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Logout", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                        is com.example.v2.core.CloudSyncState.Syncing -> {
                            Button(
                                onClick = { },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Cyan.copy(alpha = 0.2f)),
                                enabled = false,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Cyan, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Syncing...", color = Cyan)
                            }
                        }
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
        // Rule: Restore overlay after dialog
        com.example.v2.core.StateManager.updateState { it.copy(overlayState = com.example.v2.core.OverlayState.VISIBLE) }
    }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionManager.refreshPermissions()
        // Rule: Restore overlay after dialog
        com.example.v2.core.StateManager.updateState { it.copy(overlayState = com.example.v2.core.OverlayState.VISIBLE) }
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
                            com.example.v2.core.StateManager.updateState { it.copy(overlayState = com.example.v2.core.OverlayState.SUSPENDED_FOR_PERMISSION) }
                            micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else if (permission.id == "notifications" && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            com.example.v2.core.StateManager.updateState { it.copy(overlayState = com.example.v2.core.OverlayState.SUSPENDED_FOR_PERMISSION) }
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
                DiagnosticItem("Voice Pipeline", coreState.voiceSessionState.name, if (coreState.voiceSessionState == com.example.v2.core.VoiceSessionState.DISCONNECTED) NeonPink else Cyan)
                DiagnosticItem("Gemini Backend", coreState.geminiState.name, if (coreState.geminiState == GeminiConnectionState.CONNECTED) Cyan else NeonPink)
                DiagnosticItem("Microphone", coreState.micState.name, if (coreState.micState == com.example.v2.core.MicrophoneState.RECORDING) Cyan else Color.White)
                if (voiceState is VoiceState.Error) {
                    DiagnosticItem("Last Error", (voiceState as VoiceState.Error).message, NeonPink)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                GlassSectionHeader("Core Engines")
                DiagnosticItem("Memory Engine", "OK", Cyan)
                DiagnosticItem("Tool Engine", "OK", Cyan)
                DiagnosticItem("PC Connection", coreState.pcState.name, if (coreState.pcState == com.example.v2.core.PcConnectionState.CONNECTED) Cyan else NeonPink)
                DiagnosticItem("PC Name", coreState.pcName ?: "Unknown", if (coreState.pcName != null) Cyan else Color.Gray)
                DiagnosticItem("PC IP", coreState.pcIp ?: "None", if (coreState.pcIp != null) Cyan else Color.Gray)
                DiagnosticItem("PC Interface", coreState.pcInterface ?: "N/A", Violet)
                DiagnosticItem("PC Latency", "${coreState.pcLatency}ms", if (coreState.pcLatency < 100) Cyan else Color.Yellow)
                DiagnosticItem("PC Pairing", coreState.pcPairingState, if (coreState.pcPairingState == "PAIRED") Cyan else NeonPink)
                
                // RGB Engine Diagnostics
                val rgbColorText = when (coreState.rgbState) {
                    RgbEngineState.ACTIVE -> Cyan
                    RgbEngineState.STATIC -> Cyan
                    RgbEngineState.STARTING -> Color.Yellow
                    RgbEngineState.ERROR -> NeonPink
                    RgbEngineState.UNAVAILABLE -> Color.Gray
                    else -> Color.White.copy(alpha = 0.6f)
                }
                
                DiagnosticItem("RGB Engine", coreState.rgbState.name, rgbColorText)
                DiagnosticItem("RGB Mode", coreState.rgbMode.name, Violet)
                DiagnosticItem("RGB Hardware", if (coreState.rgbHardwareDetected) "DETECTED" else "NOT DETECTED", if (coreState.rgbHardwareDetected) Cyan else Color.Gray)
                DiagnosticItem("RGB Permission", "SYSTEM GRANTED", Cyan)
                DiagnosticItem("RGB Service State", if (coreState.foregroundServiceRunning) "RUNNING" else "STOPPED", if (coreState.foregroundServiceRunning) Cyan else Color.Gray)
                if (coreState.rgbLastError != null) {
                    DiagnosticItem("RGB Last Error", coreState.rgbLastError ?: "None", NeonPink)
                }
                DiagnosticItem("Active Effect", coreState.rgbEffect, Violet)
                
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
    val context = LocalContext.current
    val core = com.example.v2.core.WifeAssistantCore.getInstance(context)
    val state by com.example.v2.core.StateManager.state.collectAsState()
    val prefs = context.getSharedPreferences("wife_prefs", Context.MODE_PRIVATE)
    
    var ipInput by remember { mutableStateOf(prefs.getString("pc_ip", "") ?: "") }
    var portInput by remember { mutableStateOf(prefs.getString("pc_port", "8765") ?: "8765") }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        SettingsScreenHeader("PC Control", onBack)
        LazyColumn(contentPadding = PaddingValues(bottom = 120.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(GlassSurface).border(1.dp, GlassBorder, RoundedCornerShape(16.dp)).padding(16.dp)) {
                    Text("Connection Status", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val (statusText, statusColor) = when (state.pcState) {
                        com.example.v2.core.PcConnectionState.CONNECTED -> "● CONNECTED" to Cyan
                        com.example.v2.core.PcConnectionState.CONNECTING, com.example.v2.core.PcConnectionState.AUTHENTICATING -> "● CONNECTING" to Color.Yellow
                        com.example.v2.core.PcConnectionState.ERROR, com.example.v2.core.PcConnectionState.AUTH_FAILED -> "● ERROR" to NeonPink
                        else -> "● DISCONNECTED" to Color.Gray
                    }
                    
                    Text(text = statusText, color = statusColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    
                    if (state.pcState == com.example.v2.core.PcConnectionState.CONNECTED) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Linked to: ${state.pcName ?: "PC"}", color = Color.White, fontSize = 14.sp)
                        Text(text = "Latency: ${state.pcLatency}ms", color = Cyan, fontSize = 12.sp)
                    }
                }
            }
            
            item {
                Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(GlassSurface).border(1.dp, GlassBorder, RoundedCornerShape(16.dp)).padding(16.dp)) {
                    Text("Configuration", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = ipInput,
                        onValueChange = { ipInput = it },
                        label = { Text("PC IP Address", color = Color.White.copy(alpha = 0.6f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Cyan,
                            focusedBorderColor = Cyan,
                            unfocusedBorderColor = GlassBorder
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = portInput,
                        onValueChange = { portInput = it },
                        label = { Text("Port", color = Color.White.copy(alpha = 0.6f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Cyan,
                            focusedBorderColor = Cyan,
                            unfocusedBorderColor = GlassBorder
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { 
                                prefs.edit().putString("pc_ip", ipInput).putString("pc_port", portInput).apply()
                                core.pcEngine.connect(ipInput, portInput.toIntOrNull() ?: 8765)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Cyan),
                            enabled = state.pcState == com.example.v2.core.PcConnectionState.DISCONNECTED || state.pcState == com.example.v2.core.PcConnectionState.ERROR
                        ) {
                            Text("Connect", color = DarkMidnightBlue)
                        }
                        
                        Button(
                            onClick = { core.pcEngine.disconnect() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = GlassBorder),
                            enabled = state.pcState != com.example.v2.core.PcConnectionState.DISCONNECTED
                        ) {
                            Text("Disconnect", color = Color.White)
                        }
                    }
                }
            }
            
            item {
                Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(GlassSurface).border(1.dp, GlassBorder, RoundedCornerShape(16.dp)).padding(16.dp)) {
                    Text("Quick Commands", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val scope = rememberCoroutineScope()
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PcCommandButton("Lock", Icons.Default.Lock, Modifier.weight(1f)) { scope.launch { core.pcEngine.executeCommand("LOCK") } }
                        PcCommandButton("Sleep", Icons.Default.Bedtime, Modifier.weight(1f)) { scope.launch { core.pcEngine.executeCommand("SLEEP") } }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PcCommandButton("Volume +", Icons.Default.VolumeUp, Modifier.weight(1f)) { scope.launch { core.pcEngine.executeCommand("VOLUME_UP") } }
                        PcCommandButton("Volume -", Icons.Default.VolumeDown, Modifier.weight(1f)) { scope.launch { core.pcEngine.executeCommand("VOLUME_DOWN") } }
                    }
                }
            }
        }
    }
}

@Composable
fun PcCommandButton(label: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = GlassBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, fontSize = 12.sp)
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

