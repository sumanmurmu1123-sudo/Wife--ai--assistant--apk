package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

    // কালার ট্রানজিশন (সাধারণ অবস্থায় সায়ান, SLX মোডে বিষাক্ত ম্যাট্রিক্স গ্রিন)
    val accentColor by animateColorAsState(
        targetValue = if (isSlxActive) Color(0xFF00FF66) else Color(0xFF64748B),
        animationSpec = tween(400),
        label = "SlxGlow"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF050B14)),
        border = BorderStroke(1.5.dp, accentColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "SLX MOD PROTOCOL",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (isSlxActive) "TURBO PERFORMANCE: ACTIVE" else "STANDBY // NORMAL",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = accentColor
                        )
                    }
                }

                Switch(
                    checked = isSlxActive,
                    onCheckedChange = {
                        SlxModManager.toggleSlxMod(context, voiceManager)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = Color(0xFF00FF66),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color(0xFF1E293B)
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // স্পেসিক্স / সাই-ফাই টেলিমেট্রি ইনফো
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ENGINE: EXTREME_X4",
                    fontSize = 9.sp,
                    color = Color(0xFF94A3B8),
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "SUJIT OVERCLOCK: ${if (isSlxActive) "3.4 GHz" else "BASE"}",
                    fontSize = 9.sp,
                    color = if (isSlxActive) Color(0xFF00FF66) else Color(0xFF94A3B8),
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
