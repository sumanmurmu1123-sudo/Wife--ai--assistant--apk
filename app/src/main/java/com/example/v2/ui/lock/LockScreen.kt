package com.example.v2.ui.lock

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.v2.ui.theme.Cyan
import com.example.v2.ui.theme.DarkMidnightBlue
import com.example.v2.ui.theme.NeonPink
import com.example.v2.voice.VoiceState
import com.example.v2.voice.VoiceViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LockScreen(viewModel: VoiceViewModel, onUnlock: () -> Unit) {
    val context = LocalContext.current
    val voiceState by viewModel.state.collectAsState()
    
    // States
    var timeString by remember { mutableStateOf("") }
    var dateString by remember { mutableStateOf("") }
    var batteryLevel by remember { mutableStateOf(-1) }
    var isCharging by remember { mutableStateOf(false) }
    
    var networkState by remember { mutableStateOf("Offline") }
    var networkType by remember { mutableStateOf("None") }
    var hasMicPermission by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }
    
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
    }

    // Network & Battery updates
    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE • d MMMM", Locale("en", "US"))
        
        while (true) {
            val now = Calendar.getInstance()
            timeString = timeFormat.format(now.time)
            dateString = dateFormat.format(now.time)
            
            // Battery
            val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            if (batteryStatus != null) {
                val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                batteryLevel = if (scale > 0) (level * 100 / scale.toFloat()).toInt() else -1
                
                val status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            }
            
            // Network
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetwork
            val capabilities = cm.getNetworkCapabilities(activeNetwork)
            
            if (capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                networkState = if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                    "Online"
                } else {
                    "Connecting…"
                }
                
                networkType = when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile Data"
                    else -> "Network"
                }
            } else {
                networkState = "Offline"
                networkType = "None"
            }
            
            hasMicPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            
            delay(1000)
        }
    }
    
    // Auth logic
    val fragmentActivity = context as? FragmentActivity
    val authenticateUser = {
        if (fragmentActivity != null) {
            val biometricManager = BiometricManager.from(context)
            val canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            
            if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Wife AI Security")
                    .setSubtitle("Confirm your identity to unlock")
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                    .build()
                
                val biometricPrompt = BiometricPrompt(
                    fragmentActivity,
                    ContextCompat.getMainExecutor(context),
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            authError = errString.toString()
                        }

                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            authError = null
                            onUnlock()
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            authError = "Authentication failed"
                        }
                    }
                )
                biometricPrompt.authenticate(promptInfo)
            } else {
                // Device doesn't have secure auth setup, just unlock
                onUnlock()
            }
        } else {
            // Fallback if not FragmentActivity
            onUnlock()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "OrbPulse")
    val neonAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkMidnightBlue)
    ) {
        // Deep background glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF0F172A), DarkMidnightBlue),
                        center = Offset(500f, 1500f),
                        radius = 2000f
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Bar Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Network Status
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val netIcon = when (networkType) {
                        "Wi-Fi" -> Icons.Default.Wifi
                        "Mobile Data" -> Icons.Default.SignalCellular4Bar
                        else -> Icons.Default.WifiOff
                    }
                    val netColor = if (networkState == "Online") Cyan else Color.Gray
                    Icon(netIcon, contentDescription = "Network", tint = netColor, modifier = Modifier.size(16.dp))
                    Text(networkState, color = netColor, fontSize = 12.sp)
                }
                
                // Battery Status
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val batColor = if (batteryLevel <= 20 && !isCharging) NeonPink else Cyan
                    Text(if (batteryLevel >= 0) "$batteryLevel%" else "Unknown", color = batColor, fontSize = 12.sp)
                    Icon(
                        if (isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                        contentDescription = "Battery",
                        tint = batColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Branding
            Text(
                text = "BOSS",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 8.sp,
                style = TextStyle(shadow = Shadow(color = Cyan.copy(alpha = 0.5f), blurRadius = 20f))
            )
            Text(
                text = "SujitHero",
                color = Cyan,
                fontSize = 18.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 6.sp,
                modifier = Modifier.offset(y = (-8).dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Clock & Date
            Text(
                text = timeString,
                color = Color.White,
                fontSize = 84.sp,
                fontWeight = FontWeight.Light,
                style = TextStyle(shadow = Shadow(color = Cyan.copy(alpha = 0.3f), blurRadius = 15f))
            )
            Text(
                text = dateString,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // AI Orb & Avatar
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // State-based glowing rings
                val orbColor = when (voiceState) {
                    is VoiceState.Idle, is VoiceState.Disconnected, is VoiceState.Unavailable, is VoiceState.PermissionRequired -> Cyan.copy(alpha = 0.4f)
                    is VoiceState.Listening -> NeonPink.copy(alpha = 0.8f)
                    is VoiceState.Thinking -> Cyan.copy(alpha = 0.8f)
                    is VoiceState.Speaking -> Cyan.copy(alpha = 1.0f)
                    is VoiceState.Connecting, is VoiceState.Connected, is VoiceState.Initializing, is VoiceState.Reconnecting -> Color.Yellow.copy(alpha = 0.6f)
                    is VoiceState.Error -> NeonPink.copy(alpha = 1.0f)
                    else -> Cyan.copy(alpha = 0.2f)
                }
                
                val currentAlpha = if (voiceState is VoiceState.Idle) neonAlpha * 0.5f else neonAlpha

                Canvas(modifier = Modifier.fillMaxSize().alpha(currentAlpha)) {
                    drawCircle(
                        color = orbColor,
                        radius = size.width / 2.2f,
                        style = Stroke(width = if (voiceState is VoiceState.Listening) 8.dp.toPx() else 4.dp.toPx())
                    )
                    drawCircle(
                        color = orbColor.copy(alpha = 0.3f),
                        radius = size.width / 2f,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
                
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.wife_app_icon_v3_1789748810892),
                    contentDescription = "Profile Avatar",
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .border(2.dp, orbColor, CircleShape)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Wife AI Status
            val wifeStateText = voiceState.displayText
            
            val statusColor = if (voiceState is VoiceState.Error) NeonPink else Cyan
            
            Text(
                text = wifeStateText,
                color = statusColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            
            val diagnosticText = when (voiceState) {
                is VoiceState.Disconnected, is VoiceState.Unavailable, is VoiceState.PermissionRequired, is VoiceState.Idle -> "Gemini: Disconnected"
                is VoiceState.Connecting, is VoiceState.Initializing, is VoiceState.Reconnecting -> "Gemini: Connecting"
                is VoiceState.Connected, is VoiceState.Listening, is VoiceState.Thinking, is VoiceState.Speaking -> "Gemini: Connected"
                is VoiceState.Error -> "Gemini: Error"
                is VoiceState.Interrupted -> "Gemini: Connected" 
                else -> ""
            }
            if (diagnosticText.isNotEmpty()) {
                Text(
                    text = diagnosticText,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            if (!hasMicPermission) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.border(1.dp, NeonPink, RoundedCornerShape(12.dp))
                ) {
                    Text("Microphone Permission Required", color = NeonPink)
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Auth error
            if (authError != null) {
                Text(
                    text = authError!!,
                    color = NeonPink,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            // Lock Controls
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Cyan.copy(alpha = 0.3f), CircleShape)
                            .clickable { authenticateUser() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Fingerprint,
                            contentDescription = "Unlock",
                            tint = Cyan,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Tap to Unlock",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }
                
                // Emergency protection shortcut
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .clickable { /* TODO: Launch phone protection */ }
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Security, contentDescription = "Security", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                        Text("Phone Protection", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}
