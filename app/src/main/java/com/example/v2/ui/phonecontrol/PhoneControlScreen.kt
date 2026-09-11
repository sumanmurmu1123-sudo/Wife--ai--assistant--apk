package com.example.v2.ui.phonecontrol

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.v2.ui.theme.Cyan
import com.example.v2.ui.theme.DarkMidnightBlue
import com.example.v2.ui.theme.NeonPink
import com.example.v2.voice.VoiceViewModel

@Composable
fun PhoneControlScreen(
    viewModel: VoiceViewModel,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .padding(top = 40.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Wife Remote",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text("QUICK CONTROL", color = Cyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ControlButton(
                    icon = Icons.Default.FlashlightOn,
                    label = "Flashlight",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.triggerAction("Flashlight On", it) }
                )
                ControlButton(
                    icon = Icons.Default.VolumeUp,
                    label = "Volume 50%",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.triggerAction("Volume 50 percent", it) }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("CONNECTIVITY", color = NeonPink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ControlButton(
                    icon = Icons.Default.Wifi,
                    label = "Wi-Fi",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.triggerAction("Open Wi-Fi Settings", it) }
                )
                ControlButton(
                    icon = Icons.Default.Settings,
                    label = "System",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.triggerAction("Open Settings", it) }
                )
            }
        }
    }
}

@Composable
fun ControlButton(icon: ImageVector, label: String, modifier: Modifier = Modifier, onClick: (android.content.Context) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24)),
        onClick = { onClick(context) }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, color = Color.White, fontSize = 14.sp)
        }
    }
}
