package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WarningAmber
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
import com.example.magic.PocketGuardService

@Composable
fun PocketGuardCard() {
    val context = LocalContext.current
    var isGuardEnabled by remember { mutableStateOf(false) }

    GlassCard(isActive = isGuardEnabled) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Pocket Guard",
                        tint = if(isGuardEnabled) Color(0xFF00FF66) else Color.DarkGray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🛡 POCKET GUARD",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if(isGuardEnabled) Color(0xFF00FF66) else Color.DarkGray,
                        letterSpacing = 1.sp
                    )
                }
                
                Text(
                    text = if(isGuardEnabled) "ARMED" else "DISARMED",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if(isGuardEnabled) Color(0xFF00FF66) else Color.DarkGray
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("MOTION SENSOR", fontSize = 10.sp, color = Color.Gray)
                Text(if(isGuardEnabled) "● READY" else "○ OFFLINE", fontSize = 10.sp, color = if(isGuardEnabled) Color(0xFF00FF66) else Color.Gray)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("PROXIMITY SENSOR", fontSize = 10.sp, color = Color.Gray)
                Text(if(isGuardEnabled) "● READY" else "○ OFFLINE", fontSize = 10.sp, color = if(isGuardEnabled) Color(0xFF00FF66) else Color.Gray)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("SECURITY LEVEL", fontSize = 10.sp, color = Color.Gray, letterSpacing = 1.sp)
            Text(
                text = if(isGuardEnabled) "████████████████░░  HIGH" else "░░░░░░░░░░░░░░░░░░  NONE",
                fontSize = 12.sp,
                color = if(isGuardEnabled) Color(0xFFFF0055) else Color.DarkGray,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )
            
            Button(
                onClick = { 
                    isGuardEnabled = !isGuardEnabled
                    val intent = Intent(context, PocketGuardService::class.java)
                    if (isGuardEnabled) {
                        context.startService(intent)
                    } else {
                        context.stopService(intent)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if(isGuardEnabled) Color(0xFFFF0055).copy(alpha = 0.2f) else Color(0xFF00F5FF).copy(alpha = 0.1f)
                ),
                modifier = Modifier.fillMaxWidth().height(40.dp)
            ) {
                Text(
                    text = if (isGuardEnabled) "[ DISARM SECURITY ]" else "[ ARM SECURITY ]",
                    color = if (isGuardEnabled) Color(0xFFFF0055) else Color(0xFF00F5FF),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontSize = 12.sp
                )
            }
        }
    }
}
