package com.example.ui.components

import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PanTool
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
import com.example.gesture.AirGestureService

@Composable
fun GestureControlCard() {
    val context = LocalContext.current
    var isGestureEnabled by remember { mutableStateOf(false) }

    GlassCard(isActive = isGestureEnabled) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PanTool,
                        contentDescription = "Air Gesture",
                        tint = if(isGestureEnabled) Color(0xFF00FF66) else Color(0xFF00F5FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "✋ MAGIC AIR GESTURE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if(isGestureEnabled) Color(0xFF00FF66) else Color(0xFF00F5FF),
                        letterSpacing = 1.sp
                    )
                }
                
                Switch(
                    checked = isGestureEnabled,
                    onCheckedChange = { isChecked ->
                        isGestureEnabled = isChecked
                        val intent = Intent(context, AirGestureService::class.java)
                        if (isChecked) {
                            context.startService(intent)
                        } else {
                            context.stopService(intent)
                        }
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
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("HAND TRACKING", fontSize = 10.sp, color = Color.Gray)
                Text(if(isGestureEnabled) "● ACTIVE" else "○ NOT DETECTED", fontSize = 10.sp, color = if(isGestureEnabled) Color(0xFF00FF66) else Color.Gray)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("GESTURE SENSOR", fontSize = 10.sp, color = Color.Gray)
                Text(if(isGestureEnabled) "● READY" else "○ OFFLINE", fontSize = 10.sp, color = if(isGestureEnabled) Color(0xFF00FF66) else Color.Gray)
            }

            if (isGestureEnabled) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("✋", fontSize = 24.sp)
                    Text("── SCANNING ──", fontSize = 10.sp, color = Color(0xFF00F5FF), letterSpacing = 2.sp, modifier = Modifier.padding(top = 4.dp, bottom = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Text("Wave → Wake", fontSize = 9.sp, color = Color.LightGray)
                        Text("Palm → Pause", fontSize = 9.sp, color = Color.LightGray)
                        Text("Swipe → Next", fontSize = 9.sp, color = Color.LightGray)
                    }
                }
            }
        }
    }
}
