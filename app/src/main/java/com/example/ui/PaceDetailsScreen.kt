package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DailyStepRecord
import com.example.data.FitnessCalculations
import com.example.ui.components.PremiumAnimatedWaveCard
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaceDetailsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val todayRecord by viewModel.todayRecord.collectAsStateWithLifecycle()
    val historyRecords by viewModel.allRecords.collectAsStateWithLifecycle()
    val currentSteps by viewModel.stepTrackerManager.currentSteps.collectAsStateWithLifecycle()

    val currentCadence = viewModel.stepTrackerManager.currentCadence

    // Metrics calculation
    val strideFactor = if (userProfile.gender == "Male") 0.00415f else 0.00413f
    val todayDistance = todayRecord?.distanceKm ?: (currentSteps * userProfile.heightCm * strideFactor / 1000f)
    val activeMinutes = if ((todayRecord?.activeTimeMinutes ?: 0) > 0) {
        todayRecord!!.activeTimeMinutes.toFloat()
    } else {
        FitnessCalculations.calculateActiveDurationMinutes(currentSteps, currentCadence)
    }
    val durationHours = activeMinutes / 60f
    val walkingSpeed = FitnessCalculations.calculateSpeedKmh(todayDistance, durationHours)
    val paceSec = FitnessCalculations.calculatePaceSecondsPerKm(todayDistance, activeMinutes)
    val paceFormatted = FitnessCalculations.formatPace(paceSec)

    // Pace status description
    val paceCategory = when {
        paceSec <= 0 -> "Standing / Resting"
        paceSec < 500 -> "Very Fast / Running Pace"
        paceSec < 600 -> "Fast Power Walk"
        paceSec < 720 -> "Brisk Walk"
        paceSec < 900 -> "Normal Walking Pace"
        else -> "Casual Stroll"
    }

    var selectedTimeRange by remember { mutableStateOf("Week") } // Day, Week, Month, Year
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val bgGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFD4F0FF), // Top Left - light blue
            Color(0xFFFFDFE9), // Top Right - light pink
            Color(0xFFE5E0FF), // Bottom Left - soft purple
            Color(0xFFFFE3D5)  // Bottom Right - Peach
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .noiseOverlay(userProfile.uiNoiseLevel)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 48.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color.White.copy(alpha = 0.6f), CircleShape)
                            .border(1.dp, Color.White, CircleShape)
                            .clip(CircleShape)
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1E1E1E),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = "Pace & Speed",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = Color(0xFF1E1E1E)
                    )

                    Spacer(modifier = Modifier.size(42.dp))
                }
            }
            // Hero Pace Card
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(500)) + slideInVertically(initialOffsetY = { 80 }, animationSpec = tween(500))
                ) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF00B4DB), Color(0xFF0083B0), Color(0xFF7F00FF))
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .padding(22.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "CURRENT PACE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.2.sp
                                        ),
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                    Surface(
                                        color = Color.White.copy(alpha = 0.25f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = paceCategory,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text(
                                            text = paceFormatted,
                                            style = MaterialTheme.typography.headlineLarge.copy(
                                                fontWeight = FontWeight.Black,
                                                fontSize = 44.sp
                                            ),
                                            color = Color.White
                                        )
                                        Text(
                                            text = "min/km average pace",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = String.format("%.1f", walkingSpeed),
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 28.sp
                                            ),
                                            color = Color.White
                                        )
                                        Text(
                                            text = "km/h speed",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Metrics Grid (2x2)
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(600)) + slideInVertically(initialOffsetY = { 100 }, animationSpec = tween(600))
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            PremiumAnimatedWaveCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(135.dp),
                                title = "Avg Pace",
                                value = paceFormatted,
                                unit = "min/km",
                                badgeText = "Pace = Time / Dist",
                                colors = listOf(Color(0xFF00B4DB), Color(0xFF0083B0)),
                                icon = Icons.Rounded.Speed,
                                noiseLevel = userProfile.uiNoiseLevel
                            )
                            PremiumAnimatedWaveCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(135.dp),
                                title = "Walking Speed",
                                value = String.format("%.1f", walkingSpeed),
                                unit = "km/h",
                                badgeText = "Speed = Dist / Time",
                                colors = listOf(Color(0xFF7F00FF), Color(0xFFE100FF)),
                                icon = Icons.Rounded.DirectionsRun,
                                noiseLevel = userProfile.uiNoiseLevel
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            PremiumAnimatedWaveCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(135.dp),
                                title = "Active Time",
                                value = "${activeMinutes.toInt()}",
                                unit = "mins",
                                badgeText = "Est. duration",
                                colors = listOf(Color(0xFF11998E), Color(0xFF38EF7D)),
                                icon = Icons.Rounded.Timer,
                                noiseLevel = userProfile.uiNoiseLevel
                            )
                            PremiumAnimatedWaveCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(135.dp),
                                title = "Total Distance",
                                value = String.format("%.2f", todayDistance),
                                unit = "km",
                                badgeText = "Today's walk",
                                colors = listOf(Color(0xFFFF8008), Color(0xFFFFC837)),
                                icon = Icons.Rounded.DateRange,
                                noiseLevel = userProfile.uiNoiseLevel
                            )
                        }
                    }
                }
            }

            // Pace & Speed Formula Breakdown Card
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(650)) + slideInVertically(initialOffsetY = { 110 }, animationSpec = tween(650))
                ) {
                    HowPaceCalculatedCard(
                        activeMinutes = activeMinutes,
                        distanceKm = todayDistance,
                        paceSec = paceSec,
                        walkingSpeed = walkingSpeed,
                        currentCadence = currentCadence.toInt(),
                        noiseLevel = userProfile.uiNoiseLevel
                    )
                }
            }

            // About Pace & Speed Card
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(700)) + slideInVertically(initialOffsetY = { 120 }, animationSpec = tween(700))
                ) {
                    AboutPaceCard(noiseLevel = userProfile.uiNoiseLevel)
                }
            }
        }
    }
}

@Composable
fun HowPaceCalculatedCard(
    activeMinutes: Float,
    distanceKm: Float,
    paceSec: Int,
    walkingSpeed: Float,
    currentCadence: Int,
    noiseLevel: Float = 0f
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        noiseLevel = noiseLevel
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF0083B0).copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Speed,
                        contentDescription = "Pace formula",
                        tint = Color(0xFF0083B0),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "How Pace & Speed Are Calculated",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1E1E1E)
                    )
                    Text(
                        text = "Calculated using active duration, distance, and cadence.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Step items flow row (Time ÷ Distance = Pace)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CalculationFactorItem(
                    icon = Icons.Rounded.Timer,
                    bgColor = Color(0xFF11998E).copy(alpha = 0.12f),
                    iconColor = Color(0xFF11998E),
                    label = "Time",
                    value = "${activeMinutes.toInt()} min"
                )

                Text("÷", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 16.sp)

                CalculationFactorItem(
                    icon = Icons.Rounded.DirectionsRun,
                    bgColor = Color(0xFF7F00FF).copy(alpha = 0.12f),
                    iconColor = Color(0xFF7F00FF),
                    label = "Distance",
                    value = String.format("%.2f km", distanceKm)
                )

                Text("=", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 16.sp)

                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .border(2.dp, Color(0xFF0083B0), CircleShape)
                        .background(Color(0xFF0083B0).copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = FitnessCalculations.formatPace(paceSec),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = Color(0xFF005670)
                        )
                        Text(
                            text = "min/km",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                            color = Color(0xFF005670)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0083B0).copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF0083B0).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    Text(
                        text = "Formula: Pace = Active Time (min) ÷ Distance (km)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF005670),
                            fontSize = 11.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Speed Formula: Speed (km/h) = Distance (km) ÷ Duration (hrs)\n\n" +
                                "Cadence Intensity Reference:\n" +
                                "• <80 spm: Casual Stroll (Slow)\n" +
                                "• 80–110 spm: Moderate Walking Pace\n" +
                                "• 110–130 spm: Brisk Walking\n" +
                                "• >130 spm: Fast Power Walk",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = Color(0xFF005670).copy(alpha = 0.85f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column {
                        Text("Walking Speed", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(String.format("%.1f km/h", walkingSpeed), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1E1E1E))
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column {
                        Text("Current Cadence", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("$currentCadence spm", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1E1E1E))
                    }
                }
            }
        }
    }
}

@Composable
private fun CalculationFactorItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    bgColor: Color,
    iconColor: Color,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(bgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
            color = Color(0xFF1E1E1E)
        )
    }
}

@Composable
fun AboutPaceCard(noiseLevel: Float = 0f) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        noiseLevel = noiseLevel
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF673AB7).copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = "Info",
                    tint = Color(0xFF673AB7),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "About Pace",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF1E1E1E)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Pace and speed reflect your movement intensity. A brisk walking pace (10–12 min/km or 5.0–6.0 km/h) yields maximum cardiovascular health benefits.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = Color.DarkGray
                )
            }
        }
    }
}
