package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sync.WifePcSyncClient
import kotlinx.coroutines.launch

@Composable
fun PcSyncControlCard() {
    val coroutineScope = rememberCoroutineScope()
    var statusText by remember { mutableStateOf("PC OFFLINE / READY") }
    var pcIpAddress by remember { mutableStateOf("192.168.0.105") } // Default local IP
    var isConnected by remember { mutableStateOf(false) }

    GlassCard(isActive = isConnected) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CastConnected,
                        contentDescription = "PC Sync",
                        tint = Color(0xFF00F5FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "✦ PC HOLOGRAM SYNC",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00F5FF),
                        letterSpacing = 1.sp
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if(isConnected) "● ON" else "● OFF",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if(isConnected) Color(0xFF00FF66) else Color(0xFFFF0055)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = pcIpAddress,
                onValueChange = { pcIpAddress = it },
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00F5FF),
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "═══════════════════════════════",
                fontSize = 10.sp,
                color = Color.DarkGray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text(
                    text = "CONNECTION: ",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Text(
                    text = if(isConnected) "████████░░  82%" else "░░░░░░░░░░   0%",
                    fontSize = 10.sp,
                    color = if(isConnected) Color(0xFF00F5FF) else Color.DarkGray
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val success = WifePcSyncClient.sendEventToPC(pcIpAddress, "turbo_slx")
                            isConnected = success
                            statusText = if (success) "SLX TURBO SENT" else "CONNECTION FAILED"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F5FF).copy(alpha = 0.2f)),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Text("SLX BOOST", color = Color(0xFF00F5FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val success = WifePcSyncClient.sendEventToPC(pcIpAddress, "shield_lock")
                            isConnected = success
                            statusText = if (success) "LOCK ALERT SENT" else "CONNECTION FAILED"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0055).copy(alpha = 0.2f)),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Text("LOCK SHIELD", color = Color(0xFFFF0055), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = statusText, 
                color = if (statusText.contains("FAILED")) Color(0xFFFF0055) else Color(0xFF00FF66), 
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
