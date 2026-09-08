package com.example.ui.components

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wallpaper.TouchWallpaperService

@Composable
fun SetWallpaperButton() {
    val context = LocalContext.current
    Button(
        onClick = {
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(context, TouchWallpaperService::class.java)
                )
            }
            context.startActivity(intent)
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, Color(0xFF00F5FF)),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color(0xFF00F5FF).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
    ) {
        Text("◈ APPLY LIVE HOLOGRAM", color = Color(0xFF00F5FF), fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
    }
}
