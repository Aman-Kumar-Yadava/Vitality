package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.noiseOverlay
import kotlin.math.sin

@Composable
fun PremiumAnimatedWaveCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    colors: List<Color>,
    noiseLevel: Float,
    badgeText: String? = null,
    onClick: (() -> Unit)? = null
) {
    // Wave animation states
    val infiniteTransition = rememberInfiniteTransition(label = "wave_transition")
    
    val wavePhase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "wave_phase_1"
    )
    
    val wavePhase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "wave_phase_2"
    )

    val highlightOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing, delayMillis = 1000),
            repeatMode = RepeatMode.Restart
        ), label = "wave_highlight"
    )

    // Value change reaction (ripple/amplitude increase)
    val previousValue = remember { mutableStateOf(value) }
    val waveAmplitudeAnim = remember { Animatable(15f) }
    
    LaunchedEffect(value) {
        if (value != previousValue.value) {
            waveAmplitudeAnim.animateTo(30f, tween(300, easing = FastOutSlowInEasing))
            waveAmplitudeAnim.animateTo(15f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            previousValue.value = value
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .background(Brush.linearGradient(colors))
            .noiseOverlay(noiseLevel)
    ) {
        // Fluid Wave Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val midY = height * 0.65f
            val amplitude = waveAmplitudeAnim.value

            // Back Wave (Slower, lighter)
            val path2 = Path().apply {
                moveTo(0f, height)
                lineTo(0f, midY)
                for (x in 0..width.toInt() step 10) {
                    val y = midY + sin((x / width * 2f * Math.PI.toFloat()) + wavePhase2) * (amplitude * 0.7f)
                    lineTo(x.toFloat(), y)
                }
                lineTo(width, height)
                close()
            }
            drawPath(
                path = path2,
                color = Color.White.copy(alpha = 0.15f)
            )

            // Front Wave (Faster, brighter)
            val path1 = Path().apply {
                moveTo(0f, height)
                lineTo(0f, midY + 10f)
                for (x in 0..width.toInt() step 10) {
                    val y = midY + 10f + sin((x / width * 2f * Math.PI.toFloat()) + wavePhase1) * amplitude
                    lineTo(x.toFloat(), y)
                }
                lineTo(width, height)
                close()
            }
            drawPath(
                path = path1,
                color = Color.White.copy(alpha = 0.25f)
            )

            // Glossy Highlight
            if (highlightOffset in -0.5f..1.5f) {
                val hx = width * highlightOffset
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.4f), Color.Transparent),
                        startX = hx - 50f,
                        endX = hx + 50f
                    ),
                    topLeft = Offset(hx - 50f, 0f),
                    size = Size(100f, height)
                )
            }
        }

        // Card Content
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.White.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                
                if (badgeText == null) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
            
            Column {
                if (badgeText == null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = if (badgeText != null) 24.sp else 22.sp
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                if (badgeText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
