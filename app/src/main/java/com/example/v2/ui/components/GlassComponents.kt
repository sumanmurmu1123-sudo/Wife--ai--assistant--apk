package com.example.v2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.v2.ui.theme.GlassBorder
import com.example.v2.ui.theme.GlassSurface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.MovieCreation

@Composable
fun GlassActionDock(onActionClick: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(GlassSurface)
            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ActionItem(icon = Icons.Default.MusicNote, label = "Music") { onActionClick("Music") }
        ActionItem(icon = Icons.Default.ChatBubble, label = "Vent") { onActionClick("Vent") }
        ActionItem(icon = Icons.Default.Payments, label = "Pay") { onActionClick("PAY WITH UPI") }
        ActionItem(icon = Icons.Default.MovieCreation, label = "Reels") { onActionClick("INSTAGRAM REEL") }
    }
}

@Composable
private fun ActionItem(icon: ImageVector, label: String, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun StatusStrip(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GlassSurface)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatusItem(icon = "💗", text = "In love")
        StatusItem(icon = "\uD83E\uDDE0", text = "12")
        StatusItem(icon = "\uD83D\uDD25", text = "1 day")
    }
}

@Composable
private fun StatusItem(icon: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = icon, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
