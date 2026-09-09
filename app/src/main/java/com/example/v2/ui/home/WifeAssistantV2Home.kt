package com.example.v2.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.v2.ui.components.GlassActionDock
import com.example.v2.ui.components.HeartNode
import com.example.v2.ui.components.StatusStrip
import com.example.v2.ui.components.WifeSaysCard
import com.example.v2.ui.theme.Cyan
import com.example.v2.ui.theme.DarkMidnightBlue
import com.example.v2.ui.theme.NeonPink
import com.example.v2.ui.theme.Violet
import com.example.v2.voice.VoiceViewModel
import java.util.Calendar

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@Composable
fun WifeAssistantV2Home(
    viewModel: VoiceViewModel,
    modifier: Modifier = Modifier
) {
    val voiceState by viewModel.state.collectAsState()
    val context = LocalContext.current

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
        // In a real app we'd load the .glb here. For now, we simulate the hero area.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp), // space for bottom nav
            contentAlignment = Alignment.Center
        ) {
            // Pseudo-Avatar using the uploaded image or placeholder
            // Using a simple box with soft light if image not found
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, DarkMidnightBlue.copy(alpha = 0.9f))
                        )
                    )
            )
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
            IconButton(onClick = { 
                Toast.makeText(context, "Menu opened", Toast.LENGTH_SHORT).show() 
            }) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
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
                Toast.makeText(context, "Profile opened", Toast.LENGTH_SHORT).show() 
            }) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.1f)).padding(6.dp)
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
            
            Spacer(modifier = Modifier.height(16.dp))
            StatusStrip()
            
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
            Text(
                text = "I'm right here. Tap me to talk.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            HeartNode(state = voiceState)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            WifeSaysCard(
                text = "Raat ho gayi...\nab so jao.\nSweet dreams \uD83C\uDF19",
                onReplayClick = {
                    viewModel.triggerAction("Replay", context)
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            GlassActionDock(onActionClick = { action ->
                viewModel.triggerAction(action, context)
            })
        }
    }
}
