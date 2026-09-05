package com.example.ui.components

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Icon(imageVector = Icons.Default.Wallpaper, contentDescription = null, tint = Color.Black)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Apply Touch Live Wallpaper", color = Color.Black)
    }
}
