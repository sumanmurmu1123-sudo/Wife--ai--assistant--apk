package com.example.v2.ui.memories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppDatabase
import com.example.data.memory.MemoryEntity
import com.example.v2.ui.theme.DarkMidnightBlue
import com.example.v2.ui.theme.GlassBorder
import com.example.v2.ui.theme.GlassSurface
import com.example.v2.ui.theme.Cyan
import com.example.v2.voice.VoiceViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifeAssistantV2Memories(viewModel: VoiceViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val dao = remember { AppDatabase.getDatabase(context).memoryDao() }
    val memories by dao.getAllMemories().collectAsState(initial = emptyList())
    
    var showDialog by remember { mutableStateOf(false) }
    var newMemoryText by remember { mutableStateOf("") }
    var newMemoryIcon by remember { mutableStateOf("💗") }

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
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
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
                }
                
                IconButton(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassSurface)
                        .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Memory", tint = Cyan)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (memories.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No memories yet. Add one!", color = Color.White.copy(alpha = 0.5f))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(memories) { memory ->
                        MemoryCard(memory)
                    }
                }
            }
        }
        
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                containerColor = DarkMidnightBlue,
                title = { Text("New Memory", color = Color.White) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = newMemoryIcon,
                            onValueChange = { newMemoryIcon = it },
                            label = { Text("Emoji/Icon", color = Color.White.copy(alpha = 0.5f)) },
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = GlassSurface,
                                unfocusedContainerColor = GlassSurface
                            ),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = newMemoryText,
                            onValueChange = { newMemoryText = it },
                            label = { Text("Memory text", color = Color.White.copy(alpha = 0.5f)) },
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = GlassSurface,
                                unfocusedContainerColor = GlassSurface
                            ),
                            maxLines = 3
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newMemoryText.isNotBlank()) {
                            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            val timeStr = dateFormat.format(Date())
                            coroutineScope.launch {
                                dao.insertMemory(
                                    MemoryEntity(
                                        icon = newMemoryIcon.ifBlank { "💭" },
                                        text = newMemoryText,
                                        time = timeStr
                                    )
                                )
                                showDialog = false
                                newMemoryText = ""
                                newMemoryIcon = "💗"
                            }
                        }
                    }) {
                        Text("Save", color = Cyan)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.5f))
                    }
                }
            )
        }
    }
}

@Composable
fun MemoryCard(memory: MemoryEntity) {
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
