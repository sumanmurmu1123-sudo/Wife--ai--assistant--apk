package com.example.data

import androidx.compose.ui.graphics.Color

enum class VoiceSlate(
    val voiceName: String,
    val displayName: String,
    val description: String,
    val tag: String,
    val themeColor: Color
) {
    AOEDE(
        voiceName = "Aoede",
        displayName = "Aoede (Sweet & Warm)",
        description = "Soft, affectionate, and romantic tone",
        tag = "Romantic Wife 💕",
        themeColor = Color(0xFFFF2A85)
    ),
    KORE(
        voiceName = "Kore",
        displayName = "Kore (Playful & Lively)",
        description = "High-energy, bubbly, and cheerful companion",
        tag = "Bestie / Playful ✨",
        themeColor = Color(0xFF00E5FF)
    ),
    CHARON(
        voiceName = "Charon",
        displayName = "Charon (Calm & Elegant)",
        description = "Mature, gentle, and deeply relaxing voice",
        tag = "Calm & Gentle 🌙",
        themeColor = Color(0xFF9D4EDD)
    ),
    PUCK(
        voiceName = "Puck",
        displayName = "Puck (Energetic Buddy)",
        description = "Friendly, witty, and straight-talking",
        tag = "Buddy / Wingman ⚡",
        themeColor = Color(0xFFFFB703)
    )
}
