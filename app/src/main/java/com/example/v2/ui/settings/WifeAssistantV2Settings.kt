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
    var pcIp by remember { mutableStateOf(prefs.getString("pc_ip", "192.168.1.100") ?: "192.168.1.100") }
    var socialMode by remember { mutableStateOf(prefs.getBoolean("social_mode", false)) }
    var autoReply by remember { mutableStateOf(prefs.getBoolean("auto_reply", false)) }
    var ambientLight by remember { mutableStateOf(prefs.getBoolean("ambient_light", true)) }
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
