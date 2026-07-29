#!/bin/bash
cat << 'THEMEOF' > app/src/main/java/com/example/ui/DashboardScreen.kt
package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.WalkingSession
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val currentSteps by viewModel.stepTrackerManager.currentSteps.collectAsStateWithLifecycle()
    val todayRecord by viewModel.todayRecord.collectAsStateWithLifecycle()
    val todaySessions by viewModel.todaySessions.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    
    val isTracking by viewModel.isTracking.collectAsStateWithLifecycle()
    val steps = currentSteps
    val goal = userProfile.dailyStepGoal
    val progress = (steps.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
    
    val distance = todayRecord?.distanceKm ?: 0f
    val calories = todayRecord?.caloriesBurned ?: 0f

    val currentLocation = userProfile.location

    var tapCount by remember { mutableIntStateOf(0) }
    var lastTapTime by remember { mutableLongStateOf(0L) }

    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE3F2FD), // Sky blue
            Color(0xFFFCE4EC), // Light pink
            Color(0xFFF3E5F5), // Lavender
            Color(0xFFFFF3E0), // Peach
            Color(0xFFE3F2FD)  // Sky blue at bottom
        )
    )

    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 48.dp, bottom = 120.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Location Chip
                    Row(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = "Location",
                            modifier = Modifier.size(16.dp),
                            tint = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = currentLocation.ifEmpty { "Location" },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                    }
                    
                    // Settings icon is handled in MainScreen, so we can leave empty space here or add a notification icon if we wanted, but we'll leave it to MainScreen.
                }
                
                Text(
                    text = "Today's Activity",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color(0xFF1E1E1E),
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        val now = System.currentTimeMillis()
                        if (now - lastTapTime > 1000) tapCount = 1 else tapCount++
                        if (tapCount >= 5) {
                            viewModel.addMockSteps()
                            tapCount = 0
                        }
                        lastTapTime = now
                    }
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Let's keep going! You're doing great \uD83D\uDCAA", // Muscle emoji
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                val btnGradient = Brush.horizontalGradient(
                    colors = if (isTracking) listOf(Color(0xFFFF512F), Color(0xFFDD2476)) 
                             else listOf(Color(0xFF7F00FF), Color(0xFFFF007F), Color(0xFFFF8C00))
                )
                
                Button(
                    onClick = {
                        if (isTracking) viewModel.stopTracking() else viewModel.startTracking()
                    },
                    modifier = Modifier.fillMaxWidth(0.8f).height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 4.dp
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(btnGradient, RoundedCornerShape(32.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isTracking) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color.White.copy(alpha = 0.3f), CircleShape)
                                    .padding(4.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (isTracking) "Pause Workout" else "Start Workout", 
                                color = Color.White, 
                                fontWeight = FontWeight.SemiBold, 
                                fontSize = 18.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            item {
                // Main Ring Card
                GlassCard(modifier = Modifier.fillMaxWidth().height(320.dp)) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        val ringGradient = Brush.sweepGradient(
                            0.0f to Color(0xFF7F00FF),
                            0.5f to Color(0xFFFF007F),
                            1.0f to Color(0xFFFF8C00)
                        )
                        val trackColor = Color(0xFFE0E0E0).copy(alpha = 0.5f)
                        
                        Canvas(modifier = Modifier.size(240.dp)) {
                            drawArc(
                                color = trackColor,
                                startAngle = 135f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
                            )
                            drawArc(
                                brush = ringGradient,
                                startAngle = 135f,
                                sweepAngle = 270f * progress,
                                useCenter = false,
                                style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.DirectionsRun,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color(0xFF7F00FF)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$steps",
                                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Black),
                                color = Color(0xFF1E1E1E)
                            )
                            Text(
                                text = "/ $goal steps",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            val percent = (progress * 100).toInt()
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE8EAF6), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "★ $percent% of Goal",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF5C6BC0)
                                )
                            }
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f).height(160.dp),
                        title = "Distance",
                        value = String.format("%.2f", distance),
                        unit = "km",
                        icon = Icons.AutoMirrored.Rounded.DirectionsRun,
                        colors = listOf(Color(0xFF9D50BB), Color(0xFF6E48AA))
                    )
                    StatCard(
                        modifier = Modifier.weight(1f).height(160.dp),
                        title = "Calories",
                        value = String.format("%.0f", calories),
                        unit = "kcal",
                        icon = Icons.Rounded.LocalFireDepartment,
                        colors = listOf(Color(0xFFFF8008), Color(0xFFFFC837))
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
                if (todaySessions.isNotEmpty()) {
                    Text(
                        text = "Today's Sessions",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        textAlign = TextAlign.Start,
                        color = Color(0xFF1E1E1E)
                    )
                }
            }
            
            items(todaySessions.size) { index ->
                val session = todaySessions[index]
                SessionCard(session)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun GlassCard(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), content = content)
    }
}

@Composable
fun SessionCard(session: WalkingSession) {
    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val startTime = dateFormat.format(Date(session.startTimeMs))
    val endTime = dateFormat.format(Date(session.endTimeMs))
    
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "$startTime - $endTime",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF1E1E1E)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${String.format("%.2f", session.distanceKm)} km",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
            }
            
            Text(
                text = "${session.steps} steps",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = Color(0xFF7F00FF)
            )
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    colors: List<Color>
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(colors = colors))
        ) {
            // Subtle wave decoration at bottom
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, size.height * 0.7f)
                    cubicTo(
                        size.width * 0.3f, size.height * 0.6f,
                        size.width * 0.7f, size.height * 0.9f,
                        size.width, size.height * 0.75f
                    )
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path, color = Color.White.copy(alpha = 0.2f))
            }
            
            Column(
                modifier = Modifier.padding(20.dp).fillMaxSize(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    
                    // Arrow
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = value,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = unit,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
THEMEOF
