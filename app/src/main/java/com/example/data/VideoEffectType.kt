package com.example.data

import androidx.compose.ui.graphics.Color

enum class VideoEffectType(
    val displayName: String,
    val description: String,
    val accentColor: Color
) {
    ORIGINAL("Original", "Natural video feed", Color(0xFF00F5FF)),
    CYBERPUNK("Cyberpunk Neon", "High contrast neon pink and cyan tones", Color(0xFFFF007F)),
    VINTAGE("Vintage Film", "Warm sepia tones with nostalgic grain", Color(0xFFFFB703)),
    MONOCHROME("Noir B&W", "Dramatic cinematic black & white", Color.White),
    GLITCH("RGB Glitch", "Digital chromatic aberration pulse", Color(0xFF00FF66))
}
