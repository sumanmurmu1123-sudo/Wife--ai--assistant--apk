package com.example.ui.settings

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserPreferences
import com.example.data.VoiceSlate

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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0714))
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ManageAccounts,
                        contentDescription = null,
                        tint = Color(0xFFFF2A85),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Profile & Voice Settings",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                }
            }

            HorizontalDivider(color = Color(0xFF1E172A), modifier = Modifier.padding(bottom = 16.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Name Configuration
                item {
                    Text(
                        text = "PROFILE IDENTIFIERS",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Boss Name Field
                    OutlinedTextField(
                        value = bossNameInput,
                        onValueChange = { bossNameInput = it },
                        label = { Text("Your Name / Nickname (Boss Name)") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF00F5FF)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00F5FF),
                            unfocusedBorderColor = Color(0xFF261D38),
                            focusedContainerColor = Color(0xFF130F22),
                            unfocusedContainerColor = Color(0xFF130F22)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Assistant Name Field
                    OutlinedTextField(
                        value = assistantNameInput,
                        onValueChange = { assistantNameInput = it },
                        label = { Text("Assistant Display Name (e.g. Wife, Pari)") },
                        leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFFF2A85)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFF2A85),
                            unfocusedBorderColor = Color(0xFF261D38),
                            focusedContainerColor = Color(0xFF130F22),
                            unfocusedContainerColor = Color(0xFF130F22)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Section 1.5: API Configuration
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "API CONFIGURATION",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Gemini API Key Field
                    OutlinedTextField(
                        value = geminiKeyInput,
                        onValueChange = { geminiKeyInput = it },
                        label = { Text("Gemini API Key") },
                        leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color(0xFF00FF66)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00FF66),
                            unfocusedBorderColor = Color(0xFF261D38),
                            focusedContainerColor = Color(0xFF130F22),
                            unfocusedContainerColor = Color(0xFF130F22)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // ElevenLabs API Key Field
                    OutlinedTextField(
                        value = elevenLabsKeyInput,
                        onValueChange = { elevenLabsKeyInput = it },
                        label = { Text("ElevenLabs API Key") },
                        leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color(0xFFFFD700)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color(0xFF261D38),
                            focusedContainerColor = Color(0xFF130F22),
                            unfocusedContainerColor = Color(0xFF130F22)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Section 2: Voice Slate Selection
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "SELECT AI VOICE SLATE",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }

                items(VoiceSlate.values()) { slate ->
                    val isSelected = slate == selectedVoice
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) slate.themeColor.copy(alpha = 0.15f) else Color(0xFF130F22))
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) slate.themeColor else Color(0xFF261D38),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedVoice = slate }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(slate.themeColor.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = null,
                                        tint = slate.themeColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = slate.displayName,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = slate.description,
                                        color = Color.LightGray,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedVoice = slate },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = slate.themeColor,
                                    unselectedColor = Color.Gray
                                )
                            )
                        }
                    }
                }
            }

            com.example.ui.components.SlxModCard()
            Spacer(modifier = Modifier.height(16.dp))
            com.example.ui.components.PcSyncControlCard()
            Spacer(modifier = Modifier.height(16.dp))
            com.example.ui.components.PocketGuardCard()
            Spacer(modifier = Modifier.height(16.dp))
            com.example.ui.components.HologramLauncherCard()
            Spacer(modifier = Modifier.height(16.dp))
            com.example.ui.components.GestureControlCard()
            Spacer(modifier = Modifier.height(16.dp))
            com.example.ui.components.SetWallpaperButton()
            Spacer(modifier = Modifier.height(16.dp))
            // Save Settings Button
            Button(
                onClick = {
                    repository.bossName = bossNameInput
                    repository.assistantName = assistantNameInput
                    repository.selectedVoiceSlate = selectedVoice
                    repository.geminiApiKey = geminiKeyInput
                    repository.elevenLabsApiKey = elevenLabsKeyInput
                    onSaveAndClose(bossNameInput, assistantNameInput, selectedVoice)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2A85)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Settings & Update Voice",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
