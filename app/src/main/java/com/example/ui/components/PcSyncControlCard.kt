package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sync.WifePcSyncClient
import kotlinx.coroutines.launch

@Composable
fun PcSyncControlCard() {
    val coroutineScope = rememberCoroutineScope()
    var statusText by remember { mutableStateOf("PC Offline/Ready") }
    var pcIpAddress by remember { mutableStateOf("192.168.0.105") } // Default local IP

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CastConnected,
                    contentDescription = "PC Sync",
                    tint = Color(0xFF00F5D4),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "PC Hologram Sync",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "পিসির সাথে রিয়েল-টাইম কানেকশন",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = pcIpAddress,
                onValueChange = { pcIpAddress = it },
                label = { Text("PC IP Address", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00F5D4),
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val success = WifePcSyncClient.sendEventToPC(pcIpAddress, "turbo_slx")
                            statusText = if (success) "SLX Turbo Sent to PC" else "Connection Failed"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF66)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("SLX Boost", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            val success = WifePcSyncClient.sendEventToPC(pcIpAddress, "shield_lock")
                            statusText = if (success) "Lock Alert Sent" else "Connection Failed"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0055)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Lock Shield", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = statusText, 
                color = if (statusText.contains("Failed")) Color(0xFFFF5555) else Color(0xFF94A3B8), 
                fontSize = 12.sp
            )
        }
    }
}
