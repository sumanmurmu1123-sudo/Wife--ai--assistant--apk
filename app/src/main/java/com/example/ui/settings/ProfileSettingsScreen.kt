package com.example.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserPreferences
import com.example.data.VoiceSlate
import com.example.ui.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    repository: UserPreferences,
    onSaveAndClose: (bossName: String, assistantName: String, voice: VoiceSlate) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var bossNameInput by remember { mutableStateOf(repository.bossName) }
    var assistantNameInput by remember { mutableStateOf(repository.assistantName) }
    var selectedVoice by remember { mutableStateOf(repository.selectedVoiceSlate) }
    var geminiKeyInput by remember { mutableStateOf(repository.geminiApiKey) }
    var elevenLabsKeyInput by remember { mutableStateOf(repository.elevenLabsApiKey) }

    var isSaving by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF030108)) // Deep Space Black
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "✦ WIFE AI CORE",
                        color = Color(0xFF00F5FF),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "SYSTEM CONTROL CENTER",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF00F5FF))
                }
            }
            
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.Center) {
                Text("◉ AI CORE", fontSize = 12.sp, color = Color(0xFF00FF66), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ONLINE • READY", fontSize = 12.sp, color = Color.Gray)
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SlxModCard()
                }

                // Section 1: Voice Core
                item {
                    GlassCard(isActive = true) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Color(0xFFFF0055), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("◉ VOICE CORE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF0055), letterSpacing = 1.sp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedTextField(
                                value = bossNameInput,
                                onValueChange = { bossNameInput = it },
                                label = { Text("User Identification (Boss Name)", color = Color.Gray, fontSize = 10.sp) },
                                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color.White),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF0055),
                                    unfocusedBorderColor = Color.DarkGray
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text("VOICE PROFILE", fontSize = 10.sp, color = Color.Gray, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Voice Selection in Glass Style
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                VoiceSlate.values().forEach { slate ->
                                    val isSelected = slate == selectedVoice
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isSelected) Color(0xFFFF0055).copy(alpha = 0.2f) else Color.Transparent)
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) Color(0xFFFF0055) else Color.DarkGray,
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .clickable { selectedVoice = slate }
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(slate.displayName, color = if(isSelected) Color(0xFFFF0055) else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text(slate.description, color = Color.Gray, fontSize = 10.sp)
                                            }
                                            if(isSelected) {
                                                Text("● ACTIVE", color = Color(0xFF00FF66), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("EMOTIONAL MODE", fontSize = 10.sp, color = Color.Gray, letterSpacing = 1.sp)
                                Text("● ON", fontSize = 10.sp, color = Color(0xFF00FF66), fontWeight = FontWeight.Bold)
                            }
                            var sliderVal by remember { mutableStateOf(0.5f) }
                            Slider(
                                value = sliderVal,
                                onValueChange = { sliderVal = it },
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFFFF0055),
                                    activeTrackColor = Color(0xFFFF0055),
                                    inactiveTrackColor = Color.DarkGray
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Soft", fontSize = 10.sp, color = Color.Gray)
                                Text("Strong", fontSize = 10.sp, color = Color.Gray)
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            var isTestingVoice by remember { mutableStateOf(false) }
                            Button(
                                onClick = { 
                                    coroutineScope.launch {
                                        isTestingVoice = true
                                        delay(2000)
                                        isTestingVoice = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, Color(0xFFFF0055)),
                                modifier = Modifier.fillMaxWidth().height(40.dp)
                            ) {
                                if (isTestingVoice) {
                                    Text("▂▅▇▃▆▇▂▅▃▇", color = Color(0xFFFF0055), fontSize = 14.sp)
                                } else {
                                    Text("[ TEST VOICE ]", color = Color(0xFFFF0055), fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
                
                // Section 1.5: API Configuration
                item {
                    GlassCard(isActive = false) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color(0xFF00F5FF), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("API CONFIGURATION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00F5FF), letterSpacing = 1.sp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = geminiKeyInput,
                                onValueChange = { geminiKeyInput = it },
                                label = { Text("Gemini Neural Engine Key", color = Color.Gray, fontSize = 10.sp) },
                                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color.White),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00F5FF),
                                    unfocusedBorderColor = Color.DarkGray
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = elevenLabsKeyInput,
                                onValueChange = { elevenLabsKeyInput = it },
                                label = { Text("ElevenLabs Voice Sync Key", color = Color.Gray, fontSize = 10.sp) },
                                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color.White),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00F5FF),
                                    unfocusedBorderColor = Color.DarkGray
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                item { PcSyncControlCard() }
                item { PocketGuardCard() }
                item { HologramLauncherCard() }
                item { GestureControlCard() }
                
                item {
                    SetWallpaperButton()
                }
                
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isSaving = true
                                repository.bossName = bossNameInput
                                repository.assistantName = assistantNameInput
                                repository.selectedVoiceSlate = selectedVoice
                                repository.geminiApiKey = geminiKeyInput
                                repository.elevenLabsApiKey = elevenLabsKeyInput
                                delay(500)
                                isSaving = false
                                onSaveAndClose(bossNameInput, assistantNameInput, selectedVoice)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0055).copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Color(0xFFFF0055)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = if (isSaving) "✓ AI PROFILE SYNCHRONIZED" else "⚡ SAVE & SYNC AI CORE",
                            color = Color(0xFFFF0055),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
