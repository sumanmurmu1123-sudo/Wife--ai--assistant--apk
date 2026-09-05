package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LaughterBadgeButton(
    onTriggerLaugh: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onTriggerLaugh,
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color(0xFFFFB703).copy(alpha = 0.2f))
            .border(1.dp, Color(0xFFFFB703), CircleShape)
    ) {
        Icon(
            imageVector = Icons.Default.SentimentVerySatisfied,
            contentDescription = "হাসি / Joke",
            tint = Color(0xFFFFD700),
            modifier = Modifier.size(22.dp)
        )
    }
}
