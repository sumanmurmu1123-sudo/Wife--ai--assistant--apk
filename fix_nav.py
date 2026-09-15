import re

content = """package com.example.v2.ui.components

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

enum class NavDestination {
    LOCK_SCREEN, HOME, PC, TALK, MEMORIES, SETTINGS, TOOLS
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
            
            // Microphone Button (Same as others)
            val micIcon = when (voiceState) {
                is VoiceState.PermissionRequired -> Icons.Default.MicOff
                is VoiceState.Disconnected, is VoiceState.Unavailable, is VoiceState.Idle, is VoiceState.Interrupted -> Icons.Default.WifiOff
                is VoiceState.Connecting, is VoiceState.Initializing, is VoiceState.Reconnecting -> Icons.Default.Sync
                is VoiceState.Connected -> Icons.Default.Link
                is VoiceState.Listening -> Icons.Default.Mic
                is VoiceState.Thinking -> Icons.Default.Autorenew
                is VoiceState.Speaking -> Icons.Default.GraphicEq
                is VoiceState.Error -> Icons.Default.ErrorOutline
            }

            val micTint = when (voiceState) {
                is VoiceState.Listening, is VoiceState.Speaking -> Cyan
                is VoiceState.Thinking -> Violet
                is VoiceState.Error, is VoiceState.PermissionRequired -> NeonPink
                is VoiceState.Connected -> Color.White
                else -> Color.White.copy(alpha = 0.4f)
            }

            NavItem(
                icon = micIcon,
                label = "Mic",
                isSelected = voiceState is VoiceState.Listening || voiceState is VoiceState.Speaking,
                customTint = micTint,
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
            tint = customTint ?: if (isSelected) Cyan else Color.White.copy(alpha = 0.4f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = customTint ?: if (isSelected) Cyan else Color.White.copy(alpha = 0.4f),
            fontSize = 11.sp
        )
    }
}
"""

with open('app/src/main/java/com/example/v2/ui/components/FloatingNavBar.kt', 'w', encoding='utf-8') as f:
    f.write(content)

