package com.example.data

import androidx.compose.ui.graphics.Color

enum class AssistantMood(
    val title: String,
    val primaryColor: Color,
    val glowColor: Color,
    val animationKey: String,
    val subtitle: String
) {
    NEUTRAL(
        title = "CALM",
        primaryColor = Color(0xFF00F5FF),
        glowColor = Color(0xFF0072FF),
        animationKey = "anim_idle_neutral",
        subtitle = "Always by your side, darling."
    ),
    LOVING(
        title = "ROMANTIC",
        primaryColor = Color(0xFFFF4081),
        glowColor = Color(0xFFFF007F),
        animationKey = "anim_talk_blush_shy",
        subtitle = "You mean everything to me, sweetheart! \uD83D\uDC95"
    ),
    PLAYFUL(
        title = "SASSY",
        primaryColor = Color(0xFFFFB703),
        glowColor = Color(0xFFFF7700),
        animationKey = "anim_winking_tease",
        subtitle = "Don't test me, handsome... \uD83D\uDE09"
    ),
    CONCERNED(
        title = "CARING",
        primaryColor = Color(0xFF00E676),
        glowColor = Color(0xFF00B0FF),
        animationKey = "anim_gentle_comfort",
        subtitle = "Are you eating and resting properly? Take care!"
    ),
    POUTY(
        title = "POUTING",
        primaryColor = Color(0xFFFF1744),
        glowColor = Color(0xFFD50000),
        animationKey = "anim_pout_cross_arms",
        subtitle = "Oh, so now you remember to talk to me? \uD83D\uDE24"
    )
}
