package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    var maxLife: Float,
    var color: Color,
    var size: Float
)

@Composable
fun PremiumAnimatedRing(
    progress: Float,
    modifier: Modifier = Modifier,
    trackColor: Color = Color(0xFFE0E0E0).copy(alpha = 0.4f),
    strokeWidth: Dp = 20.dp
) {
    val animatedProgress = remember { Animatable(0f) }
    
    // Pulse and milestone states
    var milestoneRipple by remember { mutableFloatStateOf(0f) }
    var milestoneGlow by remember { mutableFloatStateOf(0f) }
    
    // Continuous animations
    val infiniteTransition = rememberInfiniteTransition(label = "ring_animations")
    val gradientRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "gradient_rotation"
    )
    
    val breathingGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "breathing_glow"
    )
    
    val highlightPosition by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing, delayMillis = 2000),
            repeatMode = RepeatMode.Restart
        ), label = "highlight_position"
    )

    // Milestone detection
    val previousProgress = remember { mutableFloatStateOf(progress) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(progress) {
        if (progress > previousProgress.floatValue) {
            val oldSteps = (previousProgress.floatValue * 10000).toInt()
            val newSteps = (progress * 10000).toInt()
            
            // Basic milestone check (every 500)
            if (newSteps / 500 > oldSteps / 500) {
                scope.launch {
                    milestoneGlow = 1f
                    milestoneRipple = 0f
                    val rippleAnim = Animatable(0f)
                    val glowAnim = Animatable(1f)
                    launch {
                        rippleAnim.animateTo(1f, animationSpec = tween(800, easing = FastOutSlowInEasing))
                        milestoneRipple = rippleAnim.value
                    }
                    launch {
                        glowAnim.animateTo(0f, animationSpec = tween(1000, easing = LinearEasing))
                        milestoneGlow = glowAnim.value
                    }
                }
            }
        }
        previousProgress.floatValue = progress
        
        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    // Particles at the leading edge
    val particles = remember { mutableStateListOf<Particle>() }
    
    LaunchedEffect(Unit) {
        while(isActive) {
            delay(16) // ~60fps
            if (animatedProgress.value > 0.05f) { // Only emit if there's some progress
                // Emit new particle
                if (Random.nextFloat() > 0.3f) {
                    val colors = listOf(Color(0xFF7000FF), Color(0xFFFF007F), Color(0xFFFF8947), Color.White)
                    particles.add(
                        Particle(
                            x = 0f, // Relative to the leading edge point
                            y = 0f,
                            vx = (Random.nextFloat() - 0.5f) * 4f,
                            vy = (Random.nextFloat() - 0.5f) * 4f,
                            life = 1f,
                            maxLife = 1f,
                            color = colors.random(),
                            size = Random.nextFloat() * 6f + 2f
                        )
                    )
                }
            }
            
            // Update particles
            val iterator = particles.iterator()
            while(iterator.hasNext()) {
                val p = iterator.next()
                p.x += p.vx
                p.y += p.vy
                p.life -= 0.02f
                if (p.life <= 0f) {
                    iterator.remove()
                }
            }
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val sizeMin = minOf(size.width, size.height)
            val radius = (sizeMin - strokePx) / 2f
            val centerOffset = Offset(size.width / 2f, size.height / 2f)
            
            val sweepAngle = 270f * animatedProgress.value
            val startAngle = 135f
            
            // Track
            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
                topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                size = Size(radius * 2, radius * 2)
            )

            // Animated Gradient setup
            val sweepGradient = Brush.sweepGradient(
                0.0f to Color(0xFF7000FF),
                0.375f to Color(0xFFFF007F),
                0.75f to Color(0xFFFF8947),
                0.85f to Color(0xFFFF8947),
                1.0f to Color(0xFF7000FF),
                center = centerOffset
            )

            val arcPath = Path().apply {
                addArc(
                    oval = androidx.compose.ui.geometry.Rect(
                        centerOffset.x - radius, centerOffset.y - radius,
                        centerOffset.x + radius, centerOffset.y + radius
                    ),
                    startAngleDegrees = startAngle,
                    sweepAngleDegrees = sweepAngle
                )
            }

            // Outer glow (breathing)
            val glowWidth = strokePx * 1.5f + (strokePx * 0.5f * breathingGlow) + (strokePx * 2f * milestoneGlow)
            val glowAlpha = (0.3f * breathingGlow) + (0.5f * milestoneGlow)
            drawPath(
                path = arcPath,
                color = Color(0xFFFF007F).copy(alpha = glowAlpha),
                style = Stroke(width = glowWidth, cap = StrokeCap.Round)
            )
            
            withTransform({
                rotate(startAngle, centerOffset)
            }) {
                drawArc(
                    brush = sweepGradient,
                    startAngle = 0f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                    topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                    size = Size(radius * 2, radius * 2)
                )
            }

            // Traveling highlight
            if (highlightPosition > -0.2f && highlightPosition < 1.2f && sweepAngle > 10f) {
                val hlAngle = sweepAngle * highlightPosition.coerceIn(0f, 1f)
                val hlPath = Path().apply {
                    addArc(
                        oval = androidx.compose.ui.geometry.Rect(
                            centerOffset.x - radius, centerOffset.y - radius,
                            centerOffset.x + radius, centerOffset.y + radius
                        ),
                        startAngleDegrees = startAngle + hlAngle - 10f,
                        sweepAngleDegrees = 20f
                    )
                }
                drawPath(
                    path = hlPath,
                    color = Color.White.copy(alpha = 0.5f),
                    style = Stroke(width = strokePx * 0.8f, cap = StrokeCap.Round)
                )
            }

            // Milestone Ripple
            if (milestoneRipple > 0f) {
                drawCircle(
                    color = Color(0xFFFF007F).copy(alpha = 1f - milestoneRipple),
                    radius = radius + (strokePx * 3f * milestoneRipple),
                    center = centerOffset,
                    style = Stroke(width = strokePx * (1f - milestoneRipple))
                )
            }

            // Particles at the leading edge
            if (sweepAngle > 0f) {
                val endAngleRad = Math.toRadians((startAngle + sweepAngle).toDouble())
                val endX = centerOffset.x + radius * cos(endAngleRad).toFloat()
                val endY = centerOffset.y + radius * sin(endAngleRad).toFloat()
                
                withTransform({
                    translate(endX, endY)
                }) {
                    // Bright spot at leading edge
                    drawCircle(
                        color = Color.White,
                        radius = strokePx * 0.4f,
                        center = Offset.Zero
                    )
                    drawCircle(
                        color = Color(0xFFFF8947).copy(alpha = 0.6f),
                        radius = strokePx * 0.8f + (strokePx * 0.2f * breathingGlow),
                        center = Offset.Zero
                    )
                    
                    particles.forEach { p ->
                        drawCircle(
                            color = p.color.copy(alpha = p.life / p.maxLife),
                            radius = p.size,
                            center = Offset(p.x, p.y)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PulsingRunningIcon(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "icon_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "icon_scale"
    )
    
    val particles = remember { mutableStateListOf<Particle>() }
    LaunchedEffect(Unit) {
        while(isActive) {
            delay(100)
            if (Random.nextFloat() > 0.6f) {
                particles.add(
                    Particle(
                        x = 0f,
                        y = 0f,
                        vx = (Random.nextFloat() - 0.5f) * 2f,
                        vy = (Random.nextFloat() - 0.5f) * 2f - 1f, // Mostly upwards
                        life = 1f,
                        maxLife = 1f,
                        color = Color(0xFF7000FF).copy(alpha = 0.5f),
                        size = Random.nextFloat() * 4f + 2f
                    )
                )
            }
            
            val iterator = particles.iterator()
            while(iterator.hasNext()) {
                val p = iterator.next()
                p.x += p.vx
                p.y += p.vy
                p.life -= 0.03f
                if (p.life <= 0f) {
                    iterator.remove()
                }
            }
        }
    }
    
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            particles.forEach { p ->
                drawCircle(
                    color = p.color.copy(alpha = p.life / p.maxLife),
                    radius = p.size,
                    center = Offset(center.x + p.x * 20f, center.y + p.y * 20f)
                )
            }
        }
        androidx.compose.material3.Icon(
            imageVector = Icons.AutoMirrored.Rounded.DirectionsRun,
            contentDescription = null,
            modifier = Modifier.fillMaxSize().scale(scale),
            tint = Color(0xFF7F00FF)
        )
    }
}
