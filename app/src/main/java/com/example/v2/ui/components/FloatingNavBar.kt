package com.example.v2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.v2.ui.theme.Cyan
import com.example.v2.ui.theme.GlassBorder
import com.example.v2.ui.theme.GlassSurface
import com.example.v2.ui.theme.NeonPink

enum class NavDestination {
    HOME, PC, TALK, MEMORIES, SETTINGS
}

@Composable
fun FloatingNavBar(
    currentDestination: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(GlassSurface)
                .border(1.dp, GlassBorder, RoundedCornerShape(32.dp))
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(Icons.Default.Home, "Home", currentDestination == NavDestination.HOME) { onNavigate(NavDestination.HOME) }
            NavItem(Icons.Default.Computer, "PC", currentDestination == NavDestination.PC) { onNavigate(NavDestination.PC) }
            
            // Spacer for center TALK button
            Spacer(modifier = Modifier.width(48.dp))
            
            NavItem(Icons.Default.History, "Memories", currentDestination == NavDestination.MEMORIES) { onNavigate(NavDestination.MEMORIES) }
            NavItem(Icons.Default.Settings, "Settings", currentDestination == NavDestination.SETTINGS) { onNavigate(NavDestination.SETTINGS) }
        }
        
        // Center Talk Button (Floating above)
        Box(
            modifier = Modifier
                .offset(y = (-16).dp)
                .size(64.dp)
                .shadow(8.dp, CircleShape, spotColor = NeonPink)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(listOf(Color(0xFF2A2035), Color(0xFF100A1A)))
                )
                .border(2.dp, Brush.sweepGradient(listOf(NeonPink, Cyan, NeonPink)), CircleShape)
                .clickable { onNavigate(NavDestination.TALK) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Talk",
                tint = if (currentDestination == NavDestination.TALK) NeonPink else Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Cyan else Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (isSelected) Cyan else Color.White.copy(alpha = 0.5f),
            fontSize = 10.sp
        )
    }
}
