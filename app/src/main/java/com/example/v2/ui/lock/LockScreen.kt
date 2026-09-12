package com.example.v2.ui.lock

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.v2.ui.theme.Cyan
import com.example.v2.ui.theme.DarkMidnightBlue
import com.example.v2.ui.theme.NeonPink
import com.example.v2.ui.components.HeartNode
import com.example.v2.voice.VoiceState
import com.example.v2.voice.VoiceViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

import androidx.compose.foundation.border
import com.example.v2.ui.components.SujitheroText

@Composable
fun LockScreen(viewModel: VoiceViewModel, onUnlock: () -> Unit) {
    val context = LocalContext.current
    val voiceState by viewModel.state.collectAsState()
    
    // States
    var timeString by remember { mutableStateOf("") }
    var dateString by remember { mutableStateOf("") }
    var greetingString by remember { mutableStateOf("") }
    var batteryLevel by remember { mutableStateOf(100) }
    var isWifiConnected by remember { mutableStateOf(false) }
    var isNetworkOnline by remember { mutableStateOf(false) }
    
    // Setup time updating
    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("h:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEE, d MMMM", Locale.getDefault())
        while (true) {
            val now = Calendar.getInstance()
            timeString = timeFormat.format(now.time)
            dateString = dateFormat.format(now.time).uppercase()
            
            val hour = now.get(Calendar.HOUR_OF_DAY)
            greetingString = when (hour) {
                in 5..11 -> "GOOD MORNING"
                in 12..17 -> "GOOD AFTERNOON"
                in 18..21 -> "GOOD EVENING"
                else -> "GOOD NIGHT"
            }
            
            // Battery
            val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            batteryLevel = (level * 100 / scale.toFloat()).toInt()
            
            // Network
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
            isNetworkOnline = capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || 
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            )
            isWifiConnected = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
            
            delay(1000)
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "NeonPulse")
    val neonAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha"
    )

    // Unlock drag
    var offsetY by remember { mutableStateOf(0f) }
    val maxDrag = -300f
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkMidnightBlue)
    ) {
        // Background particles / glows
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364).copy(alpha = 0.5f), Color.Black),
                        center = Offset(500f, 1500f),
                        radius = 2000f
                    )
                )
        )
        
        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Bar Area (Custom)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Jio 4G", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isWifiConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                        contentDescription = "WiFi",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                    Icon(
                        Icons.Default.SignalCellular4Bar,
                        contentDescription = "Signal",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text("$batteryLevel%", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    Icon(
                        Icons.Default.BatteryFull,
                        contentDescription = "Battery",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Time and Date
            Text(
                text = timeString,
                color = Color.White,
                fontSize = 72.sp,
                fontWeight = FontWeight.Light,
                style = TextStyle(shadow = Shadow(color = Cyan.copy(alpha = 0.5f), blurRadius = 20f))
            )
            Text(
                text = dateString,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = greetingString,
                color = Cyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // SUJITHERO Hero Element
            SujitheroText(neonAlpha = neonAlpha)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "I'M ALWAYS WITH YOU ♥",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // AI Character Area
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Holographic Rings
                Canvas(modifier = Modifier.fillMaxSize().alpha(neonAlpha)) {
                    drawCircle(
                        color = Cyan.copy(alpha = 0.2f),
                        radius = size.width / 2.2f,
                        style = Stroke(width = 4.dp.toPx())
                    )
                    drawCircle(
                        color = NeonPink.copy(alpha = 0.1f),
                        radius = size.width / 2f,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
                
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.profile_avatar),
                    contentDescription = "Profile Avatar",
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(2.dp, Cyan.copy(alpha = 0.5f), CircleShape)
                )
            }
            
            // State indicators
            Spacer(modifier = Modifier.weight(1f))
            
            val assistantStateText = when(voiceState) {
                is VoiceState.Idle -> "READY"
                is VoiceState.Listening -> "LISTENING"
                is VoiceState.Thinking -> "THINKING"
                is VoiceState.Speaking -> "SPEAKING"
                is VoiceState.Connecting -> "CONNECTING"
                is VoiceState.Error -> "CONNECTION ERROR"
                is VoiceState.Interrupted -> "READY"
                else -> "READY"
            }
            
            Text(
                text = "Gemini: ${if(isNetworkOnline) assistantStateText else "OFFLINE"}",
                color = if (voiceState is VoiceState.Error || !isNetworkOnline) NeonPink else Cyan,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                val micIcon = if (voiceState is VoiceState.Listening) Icons.Default.Mic else Icons.Default.MicOff
                val micText = if (voiceState is VoiceState.Listening) "IN USE" else "READY"
                Icon(micIcon, contentDescription = "Mic", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(12.dp))
                Text(text = "Mic: $micText", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
            }
            
            // Lock Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = offsetY.dp)
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            offsetY = (offsetY + delta).coerceIn(maxDrag, 0f)
                        },
                        onDragStopped = {
                            if (offsetY < maxDrag / 2) {
                                onUnlock()
                            } else {
                                offsetY = 0f
                            }
                        }
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        .clickable { onUnlock() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Unlock",
                        tint = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Swipe up to unlock",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = "Phone",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable {
                            val intent = Intent(Intent.ACTION_DIAL)
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        .padding(12.dp)
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Wife AI", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("ASSISTANT", color = Cyan.copy(alpha = 0.8f), fontSize = 10.sp, letterSpacing = 2.sp)
                }
                
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = "Camera",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable {
                            val intent = Intent(android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        .padding(12.dp)
                )
            }
            
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}
