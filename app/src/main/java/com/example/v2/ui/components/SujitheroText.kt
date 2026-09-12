package com.example.v2.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.v2.ui.theme.Cyan
import com.example.v2.ui.theme.DarkMidnightBlue

@OptIn(ExperimentalTextApi::class)
@Composable
fun SujitheroText(neonAlpha: Float, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "SweepPulse")
    
    // Light sweep
    val sweepProgress by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Sweep"
    )

    val sweepGradient = Brush.linearGradient(
        colors = listOf(
            Color.Transparent,
            Color.White.copy(alpha = 0.8f),
            Color.Transparent
        ),
        start = Offset(x = sweepProgress * 1000f - 200f, y = 0f),
        end = Offset(x = sweepProgress * 1000f + 200f, y = 100f)
    )

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Deep shadow / 3D depth layer
        Text(
            text = "SUJITHERO",
            color = Color(0xFF001A24), // very dark cyan
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            modifier = Modifier.offset(y = 4.dp, x = 2.dp),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        
        // Base glowing layer
        Text(
            text = "SUJITHERO",
            color = Color.White,
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            style = TextStyle(
                shadow = Shadow(
                    color = Cyan.copy(alpha = neonAlpha),
                    blurRadius = 40f,
                    offset = Offset(0f, 0f)
                )
            ),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        
        // Animated light sweep
        Text(
            text = "SUJITHERO",
            color = Color.Transparent,
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            style = TextStyle(
                brush = sweepGradient
            ),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
