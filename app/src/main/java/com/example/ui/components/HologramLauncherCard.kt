package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BlurOn
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
import com.example.hologram.HologramBubbleService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HologramLauncherCard() {
    val context = LocalContext.current
    var isInitializing by remember { mutableStateOf(false) }
    var initProgress by remember { mutableStateOf(0) }
    var isHologramActive by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    GlassCard(isActive = isHologramActive || isInitializing) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BlurOn,
                        contentDescription = "Hologram",
                        tint = if (isHologramActive) Color(0xFF00FF66) else Color(0xFF00F5FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "◈ HOLOGRAM LINK",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isHologramActive) Color(0xFF00FF66) else Color(0xFF00F5FF),
                        letterSpacing = 1.sp
                    )
                }
                
                Text(
                    text = if(isHologramActive) "ONLINE" else if(isInitializing) "INITIALIZING..." else "STANDBY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if(isHologramActive) Color(0xFF00FF66) else if(isInitializing) Color(0xFF00F5FF) else Color.DarkGray
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("PC LINK", fontSize = 10.sp, color = Color.Gray)
                Text(if(isHologramActive) "● CONNECTED" else "○ OFFLINE", fontSize = 10.sp, color = if(isHologramActive) Color(0xFF00FF66) else Color.Gray)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("AVATAR ENGINE", fontSize = 10.sp, color = Color.Gray)
                Text(if(isHologramActive) "● RENDERING" else "○ IDLE", fontSize = 10.sp, color = if(isHologramActive) Color(0xFF00FF66) else Color.Gray)
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            if (isInitializing) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Text(
                        text = "$initProgress%",
                        fontSize = 24.sp,
                        color = Color(0xFF00F5FF),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(
                        progress = { initProgress / 100f },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        color = Color(0xFF00F5FF),
                        trackColor = Color(0xFF00F5FF).copy(alpha = 0.2f)
                    )
                }
            } else {
                Button(
                    onClick = {
                        if (isHologramActive) {
                            val serviceIntent = Intent(context, HologramBubbleService::class.java)
                            context.stopService(serviceIntent)
                            isHologramActive = false
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            } else {
                                coroutineScope.launch {
                                    isInitializing = true
                                    for (i in 1..10) {
                                        initProgress = i * 10
                                        delay(150)
                                    }
                                    isInitializing = false
                                    isHologramActive = true
                                    val serviceIntent = Intent(context, HologramBubbleService::class.java)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        context.startForegroundService(serviceIntent)
                                    } else {
                                        context.startService(serviceIntent)
                                    }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if(isHologramActive) Color(0xFFFF0055).copy(alpha = 0.2f) else Color(0xFF00F5FF).copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Text(
                        text = if (isHologramActive) "[ DEACTIVATE HOLOGRAM ]" else "[ ACTIVATE HOLOGRAM ]",
                        color = if (isHologramActive) Color(0xFFFF0055) else Color(0xFF00F5FF),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
