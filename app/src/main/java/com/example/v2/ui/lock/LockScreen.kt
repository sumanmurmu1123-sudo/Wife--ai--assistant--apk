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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.v2.core.StateManager
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
    var isAuthenticating by remember { mutableStateOf(false) }
    var authSuccess by remember { mutableStateOf(false) }

    val authenticateUser = {
        if (fragmentActivity != null) {
            isAuthenticating = true
            authError = null
            
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
                            isAuthenticating = false
                        }

                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            authError = null
                            authSuccess = true
                            isAuthenticating = false
                            onUnlock()
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            authError = "Authentication failed"
                            isAuthenticating = false
                        }
                    }
                )
                biometricPrompt.authenticate(promptInfo)
            } else {
                // Device doesn't have secure auth setup, just unlock
                isAuthenticating = false
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
                    .padding(16.dp)
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = { viewModel.onMicrophoneTapped(context) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                // State-based glowing rings
                val orbColor = when (voiceState) {
                    is VoiceState.Idle, is VoiceState.Disconnected, is VoiceState.MicUnavailable, is VoiceState.VoiceUnavailable, is VoiceState.MicPermissionRequired -> Cyan.copy(alpha = 0.4f)
                    is VoiceState.Listening -> NeonPink.copy(alpha = 0.8f)
                    is VoiceState.Processing -> Cyan.copy(alpha = 0.8f)
                    is VoiceState.Speaking -> Cyan.copy(alpha = 1.0f)
                    is VoiceState.Connecting, is VoiceState.Connected, is VoiceState.Reconnecting -> Color.Yellow.copy(alpha = 0.6f)
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
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.wife_avatar_ultimate_neon_1789779678600),
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
                is VoiceState.Connecting, is VoiceState.Reconnecting -> "Gemini: Connecting"
                is VoiceState.Connected, is VoiceState.Listening, is VoiceState.Processing, is VoiceState.Speaking -> "Gemini: Online"
                is VoiceState.Idle -> "Gemini: Ready"
                is VoiceState.Disconnected -> "Gemini: Offline"
                is VoiceState.MicUnavailable, is VoiceState.VoiceUnavailable, is VoiceState.MicPermissionRequired -> "Gemini: Paused"
                is VoiceState.Error -> "Gemini: Error"
                is VoiceState.Interrupted -> "Gemini: Online" 
                else -> "Gemini: Ready"
            }
            if (diagnosticText.isNotEmpty()) {
                val appState by StateManager.state.collectAsState()
                Text(
                    text = if (appState.diagnosticSummary.isNotEmpty()) appState.diagnosticSummary else diagnosticText,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    modifier = Modifier.padding(top = 4.dp),
                    textAlign = TextAlign.Center
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
                    FuturisticFingerprintScanner(
                        isAuthenticating = isAuthenticating,
                        authSuccess = authSuccess,
                        authError = authError != null,
                        onClick = { authenticateUser() }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (isAuthenticating) "Authenticating…" else "Tap to Unlock",
                        color = when {
                            authSuccess -> Color(0xFF00FF88)
                            authError != null -> NeonPink
                            isAuthenticating -> Cyan
                            else -> Color.White.copy(alpha = 0.5f)
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
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

@Composable
fun FuturisticFingerprintScanner(
    isAuthenticating: Boolean,
    authSuccess: Boolean,
    authError: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Scanner")
    
    // Rotating rings
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )
    
    // Scan line
    val scanLinePos by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ScanLine"
    )
    
    // Pulse effect
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    // Glow intensity
    val glowIntensity by animateFloatAsState(
        targetValue = if (isAuthenticating) 1f else 0.4f,
        animationSpec = tween(500),
        label = "Glow"
    )

    val scannerColor = when {
        authSuccess -> Color(0xFF00FF88)
        authError -> NeonPink
        isAuthenticating -> Cyan
        else -> Cyan.copy(alpha = 0.6f)
    }

    Box(
        modifier = Modifier
            .size(100.dp)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        // Holographic HUD Rings
        if (isAuthenticating || authSuccess) {
            Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { rotationZ = rotation }) {
                drawCircle(
                    color = scannerColor.copy(alpha = 0.2f * glowIntensity),
                    radius = size.width / 2f,
                    style = Stroke(width = 1.dp.toPx())
                )
                
                // Arcs for holographic feel
                drawArc(
                    color = scannerColor.copy(alpha = 0.6f * glowIntensity),
                    startAngle = 0f,
                    sweepAngle = 60f,
                    useCenter = false,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = scannerColor.copy(alpha = 0.6f * glowIntensity),
                    startAngle = 180f,
                    sweepAngle = 60f,
                    useCenter = false,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            
            Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { rotationZ = -rotation * 1.5f }) {
                drawArc(
                    color = scannerColor.copy(alpha = 0.4f * glowIntensity),
                    startAngle = 90f,
                    sweepAngle = 40f,
                    useCenter = false,
                    style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = scannerColor.copy(alpha = 0.4f * glowIntensity),
                    startAngle = 270f,
                    sweepAngle = 40f,
                    useCenter = false,
                    style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // Deep glow
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            scannerColor.copy(alpha = 0.3f * glowIntensity),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        // Glass Frame
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.dp, scannerColor.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Fingerprint Icon
            Icon(
                Icons.Default.Fingerprint,
                contentDescription = null,
                tint = scannerColor.copy(alpha = if (isAuthenticating) 1f else 0.8f),
                modifier = Modifier
                    .size(36.dp)
                    .graphicsLayer { 
                        val s = if (isAuthenticating) pulseScale else 1f
                        scaleX = s
                        scaleY = s
                    }
                    .blur(if (authSuccess) 4.dp else 0.dp)
            )
            
            // Scan Line
            if (isAuthenticating) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .offset(y = ((-32).dp + (64.dp * scanLinePos)))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, scannerColor, Color.Transparent)
                            )
                        )
                        .blur(1.dp)
                )
            }
        }
        
        // Particles (Orbiting)
        if (isAuthenticating) {
            OrbitingParticles(color = scannerColor)
        }
    }
}

@Composable
fun OrbitingParticles(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "Particles")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Angle"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val radius = size.width / 2.2f
        val particleCount = 6
        for (i in 0 until particleCount) {
            val pAngle = (angle + (i * (360 / particleCount))) * (Math.PI / 180).toFloat()
            val x = size.width / 2 + radius * Math.cos(pAngle.toDouble()).toFloat()
            val y = size.height / 2 + radius * Math.sin(pAngle.toDouble()).toFloat()
            
            drawCircle(
                color = color.copy(alpha = 0.6f),
                radius = 2.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}
