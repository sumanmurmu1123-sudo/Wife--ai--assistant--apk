package com.example.v2.ui.memories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.v2.ui.theme.DarkMidnightBlue
import com.example.v2.ui.theme.GlassBorder
import com.example.v2.ui.theme.GlassSurface
import com.example.v2.voice.VoiceViewModel

data class MemoryItem(val icon: String, val text: String, val time: String)

val dummyMemories = listOf(
    MemoryItem("💗", "You like late-night conversations.", "Today"),
    MemoryItem("🎵", "Favorite music mood: chill.", "Yesterday"),
    MemoryItem("📚", "Studying Kotlin.", "2 days ago"),
    MemoryItem("🌙", "Last talked at 11:42 PM.", "2 days ago")
)

@Composable
fun WifeAssistantV2Memories(viewModel: VoiceViewModel, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkMidnightBlue)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .statusBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Memories",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Things I remember about you",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(dummyMemories) { memory ->
                    MemoryCard(memory)
                }
            }
        }
    }
}

@Composable
fun MemoryCard(memory: MemoryItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GlassSurface)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = memory.icon, fontSize = 24.sp)
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = memory.text,
                color = Color.White,
                fontSize = 15.sp,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = memory.time,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
    }
}
