package com.example.v2.instagram.presentation

import android.content.Intent
import android.net.Uri
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.v2.instagram.model.ReelState
import com.example.v2.ui.theme.Cyan
import com.example.v2.ui.theme.DarkMidnightBlue
import com.example.v2.ui.theme.NeonPink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstagramReelScreen(
    viewModel: InstagramReelViewModel,
    onClose: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        viewModel.selectVideo(uri)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .padding(top = 40.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Instagram Reel Creator",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { viewModel.reset(); onClose() }) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (uiState.state) {
                ReelState.IDLE -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = {
                                mediaPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                        ) {
                            Icon(Icons.Default.MovieCreation, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select Video from Phone")
                        }
                    }
                }

                ReelState.PREVIEW -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        VideoPlayerPreview(uri = uiState.videoUri, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.analyzeVideoWithAI() },
                            colors = ButtonDefaults.buttonColors(containerColor = Cyan),
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            Text("✨ AI Analyze & Format for Reels", color = Color.Black)
                        }
                    }
                }

                ReelState.ANALYZING -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Cyan)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Wife AI is analyzing and formatting...", color = Color.White)
                        }
                    }
                }

                ReelState.READY_TO_EXPORT, ReelState.EXPORTING, ReelState.SUCCESS -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp)
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            VideoPlayerPreview(uri = uiState.videoUri, modifier = Modifier.fillMaxSize())
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("AI Suggested Title", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        Text(uiState.suggestedTitle, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = uiState.selectedCaption,
                            onValueChange = { viewModel.updateCaption(it) },
                            label = { Text("Caption") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Cyan,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = uiState.hashtags,
                            onValueChange = { viewModel.updateHashtags(it) },
                            label = { Text("Hashtags") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Cyan,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (uiState.state == ReelState.EXPORTING) {
                            LinearProgressIndicator(
                                progress = { uiState.progress },
                                modifier = Modifier.fillMaxWidth(),
                                color = NeonPink,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Exporting... ${(uiState.progress * 100).toInt()}%", color = Color.White)
                        } else if (uiState.state == ReelState.READY_TO_EXPORT) {
                            Button(
                                onClick = {
                                    viewModel.exportAndShare { exportUri ->
                                        // Share intent
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "video/*"
                                            putExtra(Intent.EXTRA_STREAM, exportUri)
                                            putExtra(Intent.EXTRA_TEXT, "${uiState.selectedCaption}\n\n${uiState.hashtags}")
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share to Instagram Reel"))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Export & Share to Instagram")
                            }
                        } else if (uiState.state == ReelState.SUCCESS) {
                            Button(
                                onClick = { viewModel.reset(); onClose() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Cyan)
                            ) {
                                Text("Done", color = Color.Black)
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
fun VideoPlayerPreview(uri: Uri?, modifier: Modifier = Modifier) {
    if (uri == null) return
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.DarkGray)
            .border(2.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { context ->
                VideoView(context).apply {
                    setVideoURI(uri)
                    setOnPreparedListener { mp ->
                        mp.isLooping = true
                        start()
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
