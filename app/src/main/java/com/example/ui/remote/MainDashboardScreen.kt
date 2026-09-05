package com.example.ui.remote

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    isConnected: Boolean = true,
    batteryPercent: Int = 85,
    isCharging: Boolean = true,
    pingMs: Int = 42,
    onMicClick: () -> Unit = {},
    onMouseMove: (Float, Float) -> Unit = { _, _ -> },
    onMouseLeftClick: () -> Unit = {},
    onMouseRightClick: () -> Unit = {},
    onVolumeChange: (Float) -> Unit = {},
    onMacroTrigger: (String) -> Unit = {},
    onClose: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("AI Remote Controller", color = Color.White, fontSize = 20.sp) 
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // কানেকশন চিপ
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (isConnected) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = if (isConnected) "CONNECTED" else "OFFLINE",
                            color = if (isConnected) Color(0xFF10B981) else Color(0xFFEF4444),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F172A), Color(0xFF020617))
                    )
                )
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ১. সিস্টেম হেলথ কার্ড (ব্যাটারি ও পিং)
                SystemHealthCard(
                    batteryLevel = batteryPercent,
                    isCharging = isCharging,
                    pingMs = pingMs
                )

                // ২. ভার্চুয়াল ট্র্যাকপ্যাড (মাউস মুভমেন্ট ও ক্লিক)
                VirtualTrackpadCard(
                    onMove = onMouseMove,
                    onLeftClick = onMouseLeftClick,
                    onRightClick = onMouseRightClick
                )

                // ৩. পিসি ভলিউম স্লাইডার
                VolumeControlCard(
                    onVolumeChange = onVolumeChange
                )

                // ৪. কুইক ম্যাক্রো একশন
                MacroActionButtons(
                    onWorkModeTrigger = { onMacroTrigger("WORK_MODE") },
                    onKillChromeTrigger = { onMacroTrigger("CLEANUP_MODE") }
                )

                // ৫. মিডিয়া কন্ট্রোল কার্ড
                MediaPlayerCard(
                    isPlaying = true,
                    onPlayPauseToggle = { onMacroTrigger("MEDIA_PLAY_PAUSE") },
                    onVolumeUp = { onMacroTrigger("VOL_UP") },
                    onVolumeDown = { onMacroTrigger("VOL_DOWN") },
                    onNext = { onMacroTrigger("NEXT_TRACK") }
                )
            }

            // ৬. ফ্লোটিং ভয়েস বাটন (নিচে ডানপাশে)
            FloatingActionButton(
                onClick = onMicClick,
                containerColor = Color(0xFF38BDF8),
                contentColor = Color.Black,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Voice Command")
            }
        }
    }
}
