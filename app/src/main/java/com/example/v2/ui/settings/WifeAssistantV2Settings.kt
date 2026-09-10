package com.example.v2.ui.settings

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.v2.ui.theme.*
import com.example.v2.voice.VoiceViewModel

@Composable
fun WifeAssistantV2Settings(viewModel: VoiceViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("wife_v2_prefs", Context.MODE_PRIVATE)

    // States
    var bossName by remember { mutableStateOf(prefs.getString("boss_name", "Sujithero") ?: "Sujithero") }
    var assistantName by remember { mutableStateOf(prefs.getString("assistant_name", "Wife Assistant") ?: "Wife Assistant") }
    var userHobbies by remember { mutableStateOf(prefs.getString("user_hobbies", "Coding, Gaming") ?: "Coding, Gaming") }
    var relationshipStatus by remember { mutableStateOf(prefs.getString("relationship_status", "Married") ?: "Married") }
    
    var apiKey by remember { mutableStateOf(prefs.getString("api_key", "") ?: "") }
    var elevenLabsApiKey by remember { mutableStateOf(prefs.getString("elevenlabs_api_key", "") ?: "") }
    var pcIp by remember { mutableStateOf(prefs.getString("pc_ip", "192.168.1.100") ?: "192.168.1.100") }
    var socialMode by remember { mutableStateOf(prefs.getBoolean("social_mode", false)) }
    var autoReply by remember { mutableStateOf(prefs.getBoolean("auto_reply", false)) }
    var ambientLight by remember { mutableStateOf(prefs.getBoolean("ambient_light", true)) }
    var sweetTalkEngine by remember { mutableStateOf(prefs.getBoolean("sweet_talk_engine", true)) }
    
    // AI Persona Engines
    var proactiveEngine by remember { mutableStateOf(prefs.getBoolean("proactive_engine", true)) }
    var firstGreetingEngine by remember { mutableStateOf(prefs.getBoolean("first_greeting_engine", true)) }
    var attitudeEngine by remember { mutableStateOf(prefs.getBoolean("attitude_engine", true)) }
    var jealousyEngine by remember { mutableStateOf(prefs.getBoolean("jealousy_engine", true)) }
    var loveStoryEngine by remember { mutableStateOf(prefs.getBoolean("love_story_engine", true)) }
    var laughterEngine by remember { mutableStateOf(prefs.getBoolean("laughter_engine", true)) }
    var antiDrinkEngine by remember { mutableStateOf(prefs.getBoolean("anti_drink_engine", true)) }
    var socialMediaEngine by remember { mutableStateOf(prefs.getBoolean("social_media_engine", true)) }

    // System, Sensors & UI
    var airGestures by remember { mutableStateOf(prefs.getBoolean("air_gestures", false)) }
    var clapDetector by remember { mutableStateOf(prefs.getBoolean("clap_detector", true)) }
    var cameraVision by remember { mutableStateOf(prefs.getBoolean("camera_vision", true)) }
    var floatingHologram by remember { mutableStateOf(prefs.getBoolean("floating_hologram", false)) }
    var interactiveWallpaper by remember { mutableStateOf(prefs.getBoolean("interactive_wallpaper", false)) }
    var flashlightBattery by remember { mutableStateOf(prefs.getBoolean("flashlight_battery", true)) }
    
    // Work & Productivity
    var officeAssistant by remember { mutableStateOf(prefs.getBoolean("office_assistant", true)) }

    // Security & Defense
    var intruderCapture by remember { mutableStateOf(prefs.getBoolean("intruder_capture", false)) }
    var lostPhoneDefense by remember { mutableStateOf(prefs.getBoolean("lost_phone_defense", false)) }
    var pocketGuard by remember { mutableStateOf(prefs.getBoolean("pocket_guard", false)) }
    var voiceGuardian by remember { mutableStateOf(prefs.getBoolean("voice_guardian", false)) }
    var familyLocation by remember { mutableStateOf(prefs.getBoolean("family_location", false)) }
    var biometricAuth by remember { mutableStateOf(prefs.getBoolean("biometric_auth", false)) }

    var persona by remember { mutableStateOf(prefs.getString("persona", "Girlfriend") ?: "Girlfriend") }
    var languageMode by remember { mutableStateOf(prefs.getString("language_mode", "AUTO_DETECT") ?: "AUTO_DETECT") }
    var preferredLanguage by remember { mutableStateOf(prefs.getString("preferred_language", "Bengali") ?: "Bengali") }

    // Save helper
    fun saveString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }
    fun saveBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkMidnightBlue)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .statusBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Settings",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Customize your AI companion",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    GlassSectionHeader("Authentication")
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassTextField(
                        label = "Gemini API Key (Optional)",
                        value = apiKey,
                        onValueChange = { 
                            apiKey = it
                            saveString("api_key", it) 
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassTextField(
                        label = "ElevenLabs API Key (Optional)",
                        value = elevenLabsApiKey,
                        onValueChange = { 
                            elevenLabsApiKey = it
                            saveString("elevenlabs_api_key", it) 
                        }
                    )
                    Text(
                        text = "Leave empty to use the default app key (for Gemini/ElevenLabs)",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    GlassSectionHeader("Profile")
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassTextField(
                        label = "How should I call you?",
                        value = bossName,
                        onValueChange = { 
                            bossName = it
                            saveString("boss_name", it) 
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassTextField(
                        label = "What is my name? (AI Name)",
                        value = assistantName,
                        onValueChange = { 
                            assistantName = it
                            saveString("assistant_name", it) 
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassTextField(
                        label = "Relationship Dynamic",
                        value = relationshipStatus,
                        onValueChange = { 
                            relationshipStatus = it
                            saveString("relationship_status", it) 
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassTextField(
                        label = "Your Hobbies / Interests",
                        value = userHobbies,
                        onValueChange = { 
                            userHobbies = it
                            saveString("user_hobbies", it) 
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassSectionHeader("Persona")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GlassSelectableChip(
                            text = "Girlfriend",
                            isSelected = persona == "Girlfriend",
                            onClick = { persona = "Girlfriend"; saveString("persona", "Girlfriend") },
                            modifier = Modifier.weight(1f)
                        )
                        GlassSelectableChip(
                            text = "Bestie",
                            isSelected = persona == "Bestie",
                            onClick = { persona = "Bestie"; saveString("persona", "Bestie") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassSectionHeader("Language")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassSelectableChip(
                            text = "Auto Detect",
                            isSelected = languageMode == "AUTO_DETECT",
                            onClick = { languageMode = "AUTO_DETECT"; saveString("language_mode", "AUTO_DETECT") },
                            modifier = Modifier.weight(1f)
                        )
                        GlassSelectableChip(
                            text = "Fixed",
                            isSelected = languageMode == "FIXED",
                            onClick = { languageMode = "FIXED"; saveString("language_mode", "FIXED") },
                            modifier = Modifier.weight(1f)
                        )
                        GlassSelectableChip(
                            text = "Multilingual",
                            isSelected = languageMode == "MULTILINGUAL",
                            onClick = { languageMode = "MULTILINGUAL"; saveString("language_mode", "MULTILINGUAL") },
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassTextField(
                        label = "Preferred Language",
                        value = preferredLanguage,
                        onValueChange = { 
                            preferredLanguage = it
                            saveString("preferred_language", it) 
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassSectionHeader("PC Synchronization")
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassTextField(
                        label = "PC IP Address",
                        value = pcIp,
                        onValueChange = { 
                            pcIp = it
                            saveString("pc_ip", it) 
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassSwitchRow(
                        title = "MS Office Assistant",
                        subtitle = "Help with Word, Excel, and PowerPoint",
                        checked = officeAssistant,
                        onCheckedChange = { officeAssistant = it; saveBoolean("office_assistant", it) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassSectionHeader("Features")
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassSwitchRow(
                        title = "Social Mode",
                        subtitle = "Less affectionate in public",
                        checked = socialMode,
                        onCheckedChange = { socialMode = it; saveBoolean("social_mode", it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassSwitchRow(
                        title = "Auto Reply",
                        subtitle = "Automatically reply to WhatsApp/SMS",
                        checked = autoReply,
                        onCheckedChange = { autoReply = it; saveBoolean("auto_reply", it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassSwitchRow(
                        title = "Ambient Lighting",
                        subtitle = "RGB glow effects based on mood",
                        checked = ambientLight,
                        onCheckedChange = { ambientLight = it; saveBoolean("ambient_light", it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassSwitchRow(
                        title = "Sweet Talk Engine",
                        subtitle = "Flirtatious, romantic & caring responses",
                        checked = sweetTalkEngine,
                        onCheckedChange = { sweetTalkEngine = it; saveBoolean("sweet_talk_engine", it) }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassSectionHeader("AI Persona Engines")
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassSwitchRow(
                        title = "First Greeting Engine",
                        subtitle = "Cutely greets you when app opens",
                        checked = firstGreetingEngine,
                        onCheckedChange = { firstGreetingEngine = it; saveBoolean("first_greeting_engine", it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassSwitchRow(
                        title = "Proactive Assistant",
                        subtitle = "Takes initiative to speak and help",
                        checked = proactiveEngine,
                        onCheckedChange = { proactiveEngine = it; saveBoolean("proactive_engine", it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassSwitchRow(
                        title = "Attitude & Mistake Engine",
                        subtitle = "Admit mistakes cutely or show playful attitude",
                        checked = attitudeEngine,
                        onCheckedChange = { attitudeEngine = it; saveBoolean("attitude_engine", it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassSwitchRow(
                        title = "Jealousy Engine",
                        subtitle = "Acts jealous if you talk about other girls",
                        checked = jealousyEngine,
                        onCheckedChange = { jealousyEngine = it; saveBoolean("jealousy_engine", it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassSwitchRow(
                        title = "Love Story Engine",
                        subtitle = "Tells romantic stories when requested",
                        checked = loveStoryEngine,
                        onCheckedChange = { loveStoryEngine = it; saveBoolean("love_story_engine", it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassSwitchRow(
                        title = "Laughter Engine",
                        subtitle = "Reacts with giggles and joyful responses",
                        checked = laughterEngine,
                        onCheckedChange = { laughterEngine = it; saveBoolean("laughter_engine", it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassSwitchRow(
                        title = "Anti-Drink Engine",
                        subtitle = "Scolds you playfully if you sound drunk",
                        checked = antiDrinkEngine,
                        onCheckedChange = { antiDrinkEngine = it; saveBoolean("anti_drink_engine", it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassSwitchRow(
                        title = "Social Media Engine",
                        subtitle = "Manage WhatsApp, Facebook & SMS tasks",
                        checked = socialMediaEngine,
                        onCheckedChange = { socialMediaEngine = it; saveBoolean("social_media_engine", it) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassSectionHeader("System, Sensors & Display")
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassSwitchRow(
                        title = "Air Gestures",
                        subtitle = "Control without touching the screen",
                        checked = airGestures,
                        onCheckedChange = { airGestures = it; saveBoolean("air_gestures", it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassSwitchRow(
                        title = "Clap Detector",
                        subtitle = "Find phone or trigger responses by clapping",
                        checked = clapDetector,
                        onCheckedChange = { clapDetector = it; saveBoolean("clap_detector", it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassSwitchRow(
                        title = "Camera Vision Engine",
                        subtitle = "Analyze surroundings and objects visually",
                        checked = cameraVision,
                        onCheckedChange = { cameraVision = it; saveBoolean("camera_vision", it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassSwitchRow(
                        title = "Floating Hologram Ball",
                        subtitle = "Always-on floating cute bubble on screen",
                        checked = floatingHologram,
                        onCheckedChange = { floatingHologram = it; saveBoolean("floating_hologram", it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassSwitchRow(
                        title = "Interactive Wallpaper",
                        subtitle = "Live wallpaper that responds to touches",
                        checked = interactiveWallpaper,
                        onCheckedChange = { interactiveWallpaper = it; saveBoolean("interactive_wallpaper", it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassSwitchRow(
                        title = "Flashlight & Battery Tools",
                        subtitle = "Control torch and monitor battery levels",
                        checked = flashlightBattery,
                        onCheckedChange = { flashlightBattery = it; saveBoolean("flashlight_battery", it) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassSectionHeader("Security & Defense")
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassSwitchRow(
                        title = "Intruder Capture",
                        subtitle = "Takes a photo if unauthorized access occurs",
                        checked = intruderCapture,
                        onCheckedChange = { intruderCapture = it; saveBoolean("intruder_capture", it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassSwitchRow(
                        title = "Lost Phone Defense",
                        subtitle = "Activates strict lockdown mode if lost",
                        checked = lostPhoneDefense,
                        onCheckedChange = { lostPhoneDefense = it; saveBoolean("lost_phone_defense", it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassSwitchRow(
                        title = "Pocket Guard",
                        subtitle = "Triggers alarm if taken from pocket",
                        checked = pocketGuard,
                        onCheckedChange = { 
                            pocketGuard = it
                            saveBoolean("pocket_guard", it)
                            // Start/Stop service logic can be added here or observed elsewhere
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassSwitchRow(
                        title = "Voice Guardian",
                        subtitle = "Verifies identity using voice analysis",
                        checked = voiceGuardian,
                        onCheckedChange = { voiceGuardian = it; saveBoolean("voice_guardian", it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassSwitchRow(
                        title = "Family Location",
                        subtitle = "Share real-time location with family",
                        checked = familyLocation,
                        onCheckedChange = { familyLocation = it; saveBoolean("family_location", it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassSwitchRow(
                        title = "Biometric & PIN Auth",
                        subtitle = "Master PIN and fingerprint protection",
                        checked = biometricAuth,
                        onCheckedChange = { biometricAuth = it; saveBoolean("biometric_auth", it) }
                    )
                }
            }
        }
    }
}

@Composable
fun GlassTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
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
