package com.example.ui.remote

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun SystemHealthCard(batteryLevel: Int, isCharging: Boolean, pingMs: Int) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryStd, contentDescription = "Battery", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("$batteryLevel%", color = Color.White)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Wifi, contentDescription = "Ping", tint = Color.Green)
                Spacer(modifier = Modifier.width(8.dp))
                Text("${pingMs}ms", color = Color.White)
            }
        }
    }
}

@Composable
fun VirtualTrackpadCard(onMove: (Float, Float) -> Unit, onLeftClick: () -> Unit, onRightClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Virtual Trackpad", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFF334155), shape = MaterialTheme.shapes.medium)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onMove(dragAmount.x, dragAmount.y)
                    }
                }
            ) {
                Text("Drag to move mouse", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = onLeftClick, modifier = Modifier.weight(1f)) { Text("Left Click") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onRightClick, modifier = Modifier.weight(1f)) { Text("Right Click") }
            }
        }
    }
}

@Composable
fun VolumeControlCard(onVolumeChange: (Float) -> Unit) {
    var sliderPosition by remember { mutableStateOf(50f) }
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("PC Volume", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VolumeDown, contentDescription = null, tint = Color.White)
                Slider(
                    value = sliderPosition,
                    onValueChange = { sliderPosition = it; onVolumeChange(it) },
                    valueRange = 0f..100f,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.White)
            }
        }
    }
}

@Composable
fun MacroActionButtons(onWorkModeTrigger: () -> Unit, onKillChromeTrigger: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Quick Macros", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onWorkModeTrigger, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9))) {
                    Text("Work Mode")
                }
                Button(onClick = onKillChromeTrigger, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))) {
                    Text("Clean Chrome")
                }
            }
        }
    }
}

@Composable
fun MediaPlayerCard(isPlaying: Boolean, onPlayPauseToggle: () -> Unit, onVolumeUp: () -> Unit, onVolumeDown: () -> Unit, onNext: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Media Controls", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                IconButton(onClick = onVolumeDown) { Icon(Icons.Default.VolumeDown, contentDescription = "Vol Down", tint = Color.White) }
                IconButton(onClick = onPlayPauseToggle) { Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = "Play/Pause", tint = Color.White) }
                IconButton(onClick = onNext) { Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White) }
                IconButton(onClick = onVolumeUp) { Icon(Icons.Default.VolumeUp, contentDescription = "Vol Up", tint = Color.White) }
            }
        }
    }
}
