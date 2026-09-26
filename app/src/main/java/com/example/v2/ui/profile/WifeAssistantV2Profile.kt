package com.example.v2.ui.profile

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.example.R
import com.example.v2.ui.theme.*
import com.example.v2.voice.VoiceViewModel
import com.example.v2.ui.settings.GlassTextField

@Composable
fun WifeAssistantV2Profile(
    viewModel: VoiceViewModel,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("maya_v2_prefs", Context.MODE_PRIVATE)

    var bossName by remember { mutableStateOf(prefs.getString("boss_name", "SUJITHERO") ?: "SUJITHERO") }
    var assistantName by remember { mutableStateOf(prefs.getString("assistant_name", "Maya") ?: "Maya") }
    var userHobbies by remember { mutableStateOf(prefs.getString("user_hobbies", "Coding, Gaming") ?: "Coding, Gaming") }
    var relationshipStatus by remember { mutableStateOf(prefs.getString("relationship_status", "Married") ?: "Married") }
    var preferredLanguage by remember { mutableStateOf(prefs.getString("preferred_language", "Bengali") ?: "Bengali") }

    var isEditing by remember { mutableStateOf(false) }

    fun saveString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkMidnightBlue)
    ) {
        // Back Button
        androidx.compose.material3.IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.statusBarsPadding().padding(8.dp).align(Alignment.TopStart)
        ) {
            Icon(
                androidx.compose.material.icons.Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .statusBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Profile",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "AI Companion Identity",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Profile Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(GlassSurface)
                            .border(2.dp, Cyan.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.wife_avatar_ultimate_neon_1789779678600),
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .size(136.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = bossName.uppercase(),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = assistantName,
                        color = Cyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "AI Companion",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (isEditing) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        GlassTextField(
                            label = "Your Name",
                            value = bossName,
                            onValueChange = { bossName = it }
                        )
                    }
                    item {
                        GlassTextField(
                            label = "AI Name",
                            value = assistantName,
                            onValueChange = { assistantName = it }
                        )
                    }
                    item {
                        GlassTextField(
                            label = "Relationship",
                            value = relationshipStatus,
                            onValueChange = { relationshipStatus = it }
                        )
                    }
                    item {
                        GlassTextField(
                            label = "Interests",
                            value = userHobbies,
                            onValueChange = { userHobbies = it }
                        )
                    }
                    item {
                        GlassTextField(
                            label = "Language",
                            value = preferredLanguage,
                            onValueChange = { preferredLanguage = it }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                saveString("boss_name", bossName)
                                saveString("assistant_name", assistantName)
                                saveString("relationship_status", relationshipStatus)
                                saveString("user_hobbies", userHobbies)
                                saveString("preferred_language", preferredLanguage)
                                isEditing = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Cyan),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Save Profile", color = DarkMidnightBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        ProfileInfoCard("Name", bossName)
                    }
                    item {
                        ProfileInfoCard("AI Name", assistantName)
                    }
                    item {
                        ProfileInfoCard("Relationship", relationshipStatus)
                    }
                    item {
                        ProfileInfoCard("Interests", userHobbies)
                    }
                    item {
                        ProfileInfoCard("Language", preferredLanguage)
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { isEditing = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GlassSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Edit Profile", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileInfoCard(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GlassSurface)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
