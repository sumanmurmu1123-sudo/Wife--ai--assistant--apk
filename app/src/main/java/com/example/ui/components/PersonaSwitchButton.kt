package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CompanionPersona

@Composable
fun PersonaSwitchButton(
    currentPersona: CompanionPersona,
    onTogglePersona: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isGf = currentPersona == CompanionPersona.GIRLFRIEND

    val animatedBorderColor by animateColorAsState(
        targetValue = currentPersona.primaryColor,
        animationSpec = tween(500),
        label = "BorderColor"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF141220).copy(alpha = 0.85f))
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    listOf(animatedBorderColor, currentPersona.glowColor.copy(alpha = 0.4f))
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onTogglePersona)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = currentPersona.iconEmoji,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isGf) "Girlfriend Mode" else "Best Friend Mode",
                color = animatedBorderColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
