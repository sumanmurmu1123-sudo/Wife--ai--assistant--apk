package com.example.v2.ui.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.v2.ui.theme.Cyan
import com.example.v2.ui.theme.DarkMidnightBlue
import com.example.v2.ui.theme.GlassBorder
import com.example.v2.ui.theme.GlassSurface

data class ToolCategoryItem(
    val title: String,
    val description: String,
    val example: String,
    val icon: ImageVector,
    val isAvailable: Boolean = true
)

@Composable
fun ToolCenterScreen(
    onNavigateBack: () -> Unit
) {
    val categories = listOf(
        ToolCategoryItem("Phone Control", "Manage device settings", "\"Turn on the flashlight\"", Icons.Default.SettingsCell),
        ToolCategoryItem("Calls & SMS", "Communication", "\"Call Mom\"", Icons.Default.Phone),
        ToolCategoryItem("WhatsApp", "Messaging", "\"WhatsApp Riya\"", Icons.Default.Message),
        ToolCategoryItem("Music & Video", "Media playback", "\"Play Spotify\"", Icons.Default.PlayArrow),
        ToolCategoryItem("Screen & Camera", "Vision tasks", "\"Describe screen\"", Icons.Default.CameraAlt),
        ToolCategoryItem("Alarms & Reminders", "Time management", "\"Set timer\"", Icons.Default.Alarm),
        ToolCategoryItem("Notifications", "Read alerts", "\"Read notifications\"", Icons.Default.Notifications),
        ToolCategoryItem("Smart Home", "IoT control", "\"Turn off lights\"", Icons.Default.Home),
        ToolCategoryItem("PC ↔ Phone", "Link devices", "\"Lock my PC\"", Icons.Default.Computer),
        ToolCategoryItem("Knowledge", "Search info", "\"Search the web\"", Icons.Default.Search)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkMidnightBlue)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp, top = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onNavigateBack() }
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Tool Center",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(categories) { category ->
                ToolCategoryCard(category)
            }
        }
    }
}

@Composable
fun ToolCategoryCard(category: ToolCategoryItem) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GlassSurface)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .clickable { /* TODO: Open details */ }
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Cyan.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(category.icon, contentDescription = null, tint = Cyan)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = category.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = category.description, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = category.example, color = Cyan.copy(alpha = 0.8f), fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
    }
}
