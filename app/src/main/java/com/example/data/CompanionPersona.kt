package com.example.data

import androidx.compose.ui.graphics.Color

enum class CompanionPersona(
    val title: String,
    val displayName: String,
    val iconEmoji: String,
    val primaryColor: Color,
    val glowColor: Color,
    val greeting: String
) {
    GIRLFRIEND(
        title = "GIRLFRIEND",
        displayName = "Girlfriend Mode 💕",
        iconEmoji = "💖",
        primaryColor = Color(0xFFFF2A85),
        glowColor = Color(0xFFFF0055),
        greeting = "I'm all yours, sweetie! How can I pamper you today? 🥰"
    ),
    BEST_FRIEND(
        title = "BEST_FRIEND",
        displayName = "Best Friend Mode ⚡",
        iconEmoji = "🤜🤛",
        primaryColor = Color(0xFF00E5FF),
        glowColor = Color(0xFF0088FF),
        greeting = "Yo! What's the plan today, buddy? Let's get things done!"
    )
}
