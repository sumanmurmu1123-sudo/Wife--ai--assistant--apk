package com.example.v2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.v2.voice.VoiceState
import com.example.v2.ui.theme.Cyan
import com.example.v2.ui.theme.GlassBorder
import com.example.v2.ui.theme.GlassSurface
import com.example.v2.ui.theme.NeonPink
import com.example.v2.ui.theme.Violet
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.getValue
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale

enum class NavDestination {
    LOCK_SCREEN, HOME, PC, TALK, MEMORIES, SETTINGS, TOOLS, PROFILE
}

@Composable
fun FloatingNavBar(
    currentDestination: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    voiceState: VoiceState,
    audioLevel: Float = 0f,
    onMicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Dark transparent glass navigation bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(GlassSurface.copy(alpha = 0.4f)) // more translucent
                .border(1.dp, GlassBorder.copy(alpha = 0.3f), RoundedCornerShape(32.dp))
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = currentDestination == NavDestination.HOME,
                modifier = Modifier.weight(1f)
            ) { onNavigate(NavDestination.HOME) }
            
            NavItem(
                icon = Icons.Default.Computer,
                label = "PC",
                isSelected = currentDestination == NavDestination.PC,
                modifier = Modifier.weight(1f)
            ) { onNavigate(NavDestination.PC) }
            
            val voiceIcon = when (voiceState) {
                is VoiceState.PermissionRequired -> Icons.Default.MicOff
                is VoiceState.Disconnected, is VoiceState.Unavailable, is VoiceState.Idle, is VoiceState.Interrupted -> Icons.Default.Mic
                is VoiceState.Connecting, is VoiceState.Initializing, is VoiceState.Reconnecting -> Icons.Default.Sync
                is VoiceState.Connected -> Icons.Default.Mic
                is VoiceState.Listening -> Icons.Default.Mic
                is VoiceState.Thinking -> Icons.Default.Autorenew
                is VoiceState.Speaking -> Icons.Default.GraphicEq
                is VoiceState.Error -> Icons.Default.ErrorOutline
            }

            val voiceTint = when (voiceState) {
                is VoiceState.Listening, is VoiceState.Speaking -> Cyan
                is VoiceState.Thinking -> Violet
                is VoiceState.Error, is VoiceState.PermissionRequired -> NeonPink
                is VoiceState.Connected -> Cyan
                else -> Color.Gray
            }
            
            val voiceLabel = when (voiceState) {
                is VoiceState.Listening -> "Listening..."
                is VoiceState.Thinking -> "Thinking..."
                is VoiceState.Speaking -> "Speaking..."
                is VoiceState.Connecting, is VoiceState.Initializing, is VoiceState.Reconnecting -> "Connecting"
                is VoiceState.Error -> "Retry"
                else -> "Voice"
            }

            NavItem(
                icon = voiceIcon,
                label = voiceLabel,
                isSelected = voiceState !is VoiceState.Idle && voiceState !is VoiceState.Disconnected && voiceState !is VoiceState.Unavailable,
                customTint = voiceTint,
                modifier = Modifier.weight(1f)
            ) { onMicClick() }
            
            NavItem(
                icon = Icons.Default.History,
                label = "Memories",
                isSelected = currentDestination == NavDestination.MEMORIES,
                modifier = Modifier.weight(1f)
            ) { onNavigate(NavDestination.MEMORIES) }
            
            NavItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                isSelected = currentDestination == NavDestination.SETTINGS,
                modifier = Modifier.weight(1f)
            ) { onNavigate(NavDestination.SETTINGS) }
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    customTint: Color? = null,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = tween(300),
        label = "NavScale"
    )
    val color by animateColorAsState(
        targetValue = customTint ?: if (isSelected) Cyan else Color.Gray,
        animationSpec = tween(300),
        label = "NavColor"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp).graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            modifier = Modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
        )
    }
}

@OptIn(com.google.accompanist.permissions.ExperimentalPermissionsApi::class)
@Composable
fun FloatingMicButton(
    voiceState: VoiceState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val permissionState = com.google.accompanist.permissions.rememberPermissionState(
        android.Manifest.permission.RECORD_AUDIO
    )
    
    val isActive = voiceState is VoiceState.Listening || voiceState is VoiceState.Thinking || voiceState is VoiceState.Speaking || voiceState is VoiceState.Connecting
    val isError = voiceState is VoiceState.Error || voiceState is VoiceState.PermissionRequired
    
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "MicGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = if (isActive) 0.3f else 0.0f,
        targetValue = if (isActive) 0.7f else 0.0f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = tween(1000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "MicGlowAlpha"
    )

    val iconTint = when {
        isActive -> Cyan
        isError -> NeonPink
        else -> Violet
    }

    val iconVector = when (voiceState) {
        is VoiceState.PermissionRequired -> Icons.Default.MicOff
        is VoiceState.Disconnected, is VoiceState.Unavailable, is VoiceState.Idle, is VoiceState.Interrupted -> Icons.Default.Mic
        is VoiceState.Connecting, is VoiceState.Initializing, is VoiceState.Reconnecting -> Icons.Default.Sync
        is VoiceState.Connected -> Icons.Default.Mic
        is VoiceState.Listening -> Icons.Default.Mic
        is VoiceState.Thinking -> Icons.Default.Autorenew
        is VoiceState.Speaking -> Icons.Default.GraphicEq
        is VoiceState.Error -> Icons.Default.ErrorOutline
    }

    Box(
        modifier = modifier
            .size(72.dp)
            .clip(androidx.compose.foundation.shape.CircleShape)
            .clickable {
                if (!permissionState.status.isGranted) {
                    permissionState.launchPermissionRequest()
                    // If the user already denied it and it's permanently denied, launchPermissionRequest() does nothing.
                    // So we can fallback to checking if it was already requested. 
                    // For simplicity, we can also prompt them with a Toast, but since they asked for a clear path:
                    if (!permissionState.status.shouldShowRationale && voiceState is VoiceState.PermissionRequired) {
                        android.widget.Toast.makeText(context, "Microphone permission is required. Please enable it in Settings.", android.widget.Toast.LENGTH_LONG).show()
                        val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = android.net.Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                } else {
                    onClick()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Subtle breathing glow when active
        if (isActive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(Cyan.copy(alpha = glowAlpha), Color.Transparent)
                        )
                    )
            )
        }
        
        // Premium glass surface
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(GlassSurface)
                .border(1.dp, GlassBorder, androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = "Mic",
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
