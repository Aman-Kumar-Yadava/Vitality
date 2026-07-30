package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import kotlin.math.sin

@Composable
fun FireWaveBackgroundCanvas(
    modifier: Modifier = Modifier.fillMaxSize(),
    fireColors: List<Color> = listOf(
        Color(0xFFFF3D00).copy(alpha = 0.30f), // Deep Flame Red-Orange
        Color(0xFFFF9100).copy(alpha = 0.40f), // Bright Flame Orange
        Color(0xFFFFEA00).copy(alpha = 0.35f)  // Golden Yellow Highlight
    )
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fire_wave_transition")

    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "fire_phase_1"
    )

    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "fire_phase_2"
    )

    val flicker by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "fire_flicker"
    )

    val sparkProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "fire_spark"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val midY = height * 0.48f
        val amplitude = 18f * flicker

        // Layer 1: Back Deep Flame Wave
        val path1 = Path().apply {
            moveTo(0f, height)
            lineTo(0f, midY + 12f)
            for (x in 0..width.toInt() step 8) {
                val wave = sin((x / width * 3f * Math.PI.toFloat()) + phase1) * amplitude * 1.15f
                lineTo(x.toFloat(), midY + 12f + wave)
            }
            lineTo(width, height)
            close()
        }
        drawPath(path = path1, color = fireColors[0])

        // Layer 2: Middle Vibrant Flame Wave
        val path2 = Path().apply {
            moveTo(0f, height)
            lineTo(0f, midY)
            for (x in 0..width.toInt() step 8) {
                val wave = sin((x / width * 2.5f * Math.PI.toFloat()) - phase2) * amplitude
                lineTo(x.toFloat(), midY + wave)
            }
            lineTo(width, height)
            close()
        }
        drawPath(path = path2, color = fireColors[1])

        // Layer 3: Front Crest Golden Flame Wave
        val path3 = Path().apply {
            moveTo(0f, height)
            lineTo(0f, midY + 20f)
            for (x in 0..width.toInt() step 8) {
                val wave = sin((x / width * 4f * Math.PI.toFloat()) + phase1 * 1.4f) * (amplitude * 0.75f)
                lineTo(x.toFloat(), midY + 20f + wave)
            }
            lineTo(width, height)
            close()
        }
        drawPath(path = path3, color = fireColors[2])

        // Layer 4: Floating Ember Particles / Sparks
        val numSparks = 12
        for (i in 0 until numSparks) {
            val sparkX = (width * ((i * 0.09f + 0.04f) % 1.0f)) + sin((sparkProgress * 6.28f) + i) * 14f
            val baseProgress = (sparkProgress + (i * 0.08f)) % 1.0f
            val sparkY = height * (1.0f - baseProgress * 0.9f)
            val alpha = (1.0f - baseProgress) * 0.85f
            val radius = (1.8f + (i % 3) * 0.8f) * (1.0f - baseProgress * 0.25f)

            drawCircle(
                color = Color.White.copy(alpha = alpha.coerceIn(0f, 1f)),
                radius = radius,
                center = Offset(sparkX, sparkY)
            )
        }
    }
}
