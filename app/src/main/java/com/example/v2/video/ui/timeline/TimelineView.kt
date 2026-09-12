package com.example.v2.video.ui.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.v2.video.domain.model.TimelineModel

@Composable
fun TimelineView(timeline: TimelineModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2D2D2D))
            .padding(8.dp)
    ) {
        Text("Timeline", color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
        
        timeline.videoTracks.forEachIndexed { index, track ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Color(0xFF3D3D3D))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("V${index + 1}", color = Color.White, modifier = Modifier.width(30.dp))
                
                track.clips.forEach { clip ->
                    Box(
                        modifier = Modifier
                            .height(50.dp)
                            .width(100.dp) // Mock width
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Blue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Video", color = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
