package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

data class Particle(
    var x: Float,
    var y: Float,
    val radius: Float,
    val speed: Float,
    val alpha: Float,
    val color: Color
)

@Composable
fun CyberParticles(
    modifier: Modifier = Modifier,
    particleCount: Int = 35
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ParticleFloat")
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ParticleLoop"
    )

    val particles = remember {
        val colors = listOf(
            Color(0xFFFF007F), // Neon Pink
            Color(0xFF00F5FF), // Neon Cyan
            Color(0xFF8A2BE2), // Violet
            Color(0xFFFFD700)  // Gold
        )
        List(particleCount) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 3f + 1.5f,
                speed = Random.nextFloat() * 0.2f + 0.05f,
                alpha = Random.nextFloat() * 0.6f + 0.2f,
                color = colors.random()
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            // Calculate continuous floating upward movement
            val currentY = (particle.y - animationProgress * particle.speed) % 1f
            val actualY = if (currentY < 0f) currentY + 1f else currentY

            drawCircle(
                color = particle.color.copy(alpha = particle.alpha),
                radius = particle.radius,
                center = Offset(particle.x * size.width, actualY * size.height)
            )
        }
    }
}
