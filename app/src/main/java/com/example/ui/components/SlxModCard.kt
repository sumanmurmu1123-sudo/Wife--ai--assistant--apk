package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.slx.SlxModManager
import com.example.voice.VoiceAssistantManager

@Composable
fun SlxModCard() {
    val context = LocalContext.current
    val voiceManager = remember { VoiceAssistantManager(context) }
    val isSlxActive by SlxModManager.isSlxActive.collectAsState()

    val accentColor by animateColorAsState(
        targetValue = if (isSlxActive) Color(0xFF00F5FF) else Color.DarkGray,
        animationSpec = tween(400),
        label = "SlxGlow"
    )

    GlassCard(isActive = isSlxActive) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = "SLX Core",
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "⚡ SLX MOD PROTOCOL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        letterSpacing = 1.sp
                    )
                }
                Switch(
                    checked = isSlxActive,
                    onCheckedChange = {
                        SlxModManager.toggleSlxMod(context, voiceManager)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = Color(0xFF00F5FF),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color(0xFF1E293B)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "SYSTEM CORE",
                fontSize = 10.sp,
                color = Color.Gray,
                letterSpacing = 2.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if(isSlxActive) "██████████████████░░  92%" else "████░░░░░░░░░░░░░░░░  14%",
                    fontSize = 12.sp,
                    color = if(isSlxActive) Color(0xFF00F5FF) else Color.Gray,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("AI RESPONSE", fontSize = 9.sp, color = Color.Gray)
                    Text("VOICE ENGINE", fontSize = 9.sp, color = Color.Gray)
                    Text("NEURAL LINK", fontSize = 9.sp, color = Color.Gray)
                    Text("CPU LOAD", fontSize = 9.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(if(isSlxActive) "18ms" else "45ms", fontSize = 9.sp, color = Color(0xFF00F5FF))
                    Text(if(isSlxActive) "ACTIVE" else "STANDBY", fontSize = 9.sp, color = if(isSlxActive) Color(0xFF00FF66) else Color.Gray)
                    Text(if(isSlxActive) "STABLE" else "DORMANT", fontSize = 9.sp, color = if(isSlxActive) Color(0xFF00FF66) else Color.Gray)
                    Text(if(isSlxActive) "34%" else "8%", fontSize = 9.sp, color = Color(0xFF00F5FF))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(
                    text = if(isSlxActive) "◉ CORE ONLINE" else "◎ CORE OFFLINE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if(isSlxActive) Color(0xFF00F5FF) else Color.Gray
                )
            }
        }
    }
}
