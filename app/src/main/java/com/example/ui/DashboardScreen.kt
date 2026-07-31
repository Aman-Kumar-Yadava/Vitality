package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.components.FireWaveBackgroundCanvas
import com.example.ui.components.PremiumAnimatedRing
import com.example.ui.components.PremiumAnimatedWaveCard
import com.example.ui.components.PulsingRunningIcon
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
fun DashboardScreen(
    viewModel: MainViewModel,
    onDistanceClick: (() -> Unit)? = null,
    onCaloriesClick: (() -> Unit)? = null
) {
    val currentSteps by viewModel.stepTrackerManager.currentSteps.collectAsStateWithLifecycle()
    val todayRecord by viewModel.todayRecord.collectAsStateWithLifecycle()
    val todaySessions by viewModel.todaySessions.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    
    val isTracking by viewModel.isTracking.collectAsStateWithLifecycle()
    val steps = currentSteps
    val stepGoal = userProfile.dailyStepGoal
    val distance = todayRecord?.distanceKm ?: 0f
    val distGoal = userProfile.dailyDistanceGoalKm
    val calories = todayRecord?.caloriesBurned ?: 0f
    val calGoal = userProfile.dailyCaloriesGoal

    val primaryMetric = userProfile.primaryProgressMetric // "Steps", "Distance", "Calories"

    val activeMinutes = if ((todayRecord?.activeTimeMinutes ?: 0) > 0) {
        todayRecord!!.activeTimeMinutes.toFloat()
    } else {
        com.example.data.FitnessCalculations.calculateActiveDurationMinutes(steps, viewModel.stepTrackerManager.currentCadence)
    }
    val paceSeconds = com.example.data.FitnessCalculations.calculatePaceSecondsPerKm(distance, activeMinutes)
    val paceStr = com.example.data.FitnessCalculations.formatPace(paceSeconds)

    val (ringValueText, ringTargetText, ringProgress) = when (primaryMetric) {
        "Distance" -> Triple(
            String.format("%.2f", distance),
            String.format("/ %.1f km", distGoal),
            if (distGoal > 0f) (distance / distGoal).coerceIn(0f, 1f) else 0f
        )
        "Calories" -> Triple(
            String.format("%.0f", calories),
            "/ %d kcal".format(calGoal),
            if (calGoal > 0) (calories / calGoal.toFloat()).coerceIn(0f, 1f) else 0f
        )
        else -> Triple(
            "%,d".format(steps),
            "/ %,d steps".format(stepGoal),
            if (stepGoal > 0) (steps.toFloat() / stepGoal.toFloat()).coerceIn(0f, 1f) else 0f
        )
    }

    var tapCount by remember { mutableIntStateOf(0) }
    var lastTapTime by remember { mutableLongStateOf(0L) }
    var showSecretDialog by remember { mutableStateOf(false) }
    var selectedSessionForSummary by remember { mutableStateOf<WalkingSession?>(null) }

    val listState = rememberLazyListState()
    var hasScrolledDown by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "arrowBounce")
    val arrowOffsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrowOffsetY"
    )

    LaunchedEffect(listState.firstVisibleItemScrollOffset, listState.firstVisibleItemIndex) {
        if (listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 25) {
            hasScrolledDown = true
        } else if (listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset <= 5) {
            hasScrolledDown = false
        }
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

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .noiseOverlay(userProfile.uiNoiseLevel)
    ) {
        val screenHeight = maxHeight
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 48.dp, bottom = 96.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(40.dp))
                    }
                    
                    Text(
                        text = "Today's Activity",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF1E1E1E),
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            val now = System.currentTimeMillis()
                            if (now - lastTapTime > 1000) tapCount = 1 else tapCount++
                            if (tapCount >= 5) {
                                showSecretDialog = true
                                tapCount = 0
                            }
                            lastTapTime = now
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Text(
                        text = "Let's keep going! You're doing great 💪",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val btnGradient = Brush.horizontalGradient(
                        colors = if (isTracking) listOf(Color(0xFFFF2A00), Color(0xFFDD2476), Color(0xFFFF8008)) 
                                 else listOf(Color(0xFFFF8008), Color(0xFFFF3D00), Color(0xFFDD2476))
                    )
                    
                    Button(
                        onClick = {
                            if (isTracking) viewModel.stopTracking() else viewModel.startTracking()
                        },
                        modifier = Modifier.fillMaxWidth(0.85f).height(56.dp),
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
                                .clip(RoundedCornerShape(32.dp))
                                .background(btnGradient)
                                .noiseOverlay(userProfile.uiNoiseLevel),
                            contentAlignment = Alignment.Center
                        ) {
                            // Animated Fire Wave Effect
                            FireWaveBackgroundCanvas(
                                modifier = Modifier.matchParentSize(),
                                fireColors = listOf(
                                    Color(0xFFFF3D00).copy(alpha = 0.40f),
                                    Color(0xFFFF9100).copy(alpha = 0.45f),
                                    Color(0xFFFFEA00).copy(alpha = 0.35f)
                                )
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isTracking) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(Color.White.copy(alpha = 0.3f), CircleShape)
                                        .padding(4.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isTracking) "Recording Session" else "Start Session", 
                                    color = Color.White, 
                                    fontWeight = FontWeight.Bold, 
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Main Ring Card
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 260.dp, max = 340.dp)
                            .padding(vertical = 4.dp),
                        noiseLevel = userProfile.uiNoiseLevel
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                val minDim = minOf(maxWidth, maxHeight)
                                val canvasSize = (minDim * 0.8f).coerceAtLeast(180.dp)
                                PremiumAnimatedRing(
                                    progress = ringProgress,
                                    modifier = Modifier.size(canvasSize).align(Alignment.Center),
                                    trackColor = Color(0xFFE0E0E0).copy(alpha = 0.4f),
                                    strokeWidth = 20.dp
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                PulsingRunningIcon(modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = ringValueText,
                                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                                    color = Color(0xFF1E1E1E)
                                )
                                Text(
                                    text = ringTargetText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.DarkGray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                val percent = (ringProgress * 100).toInt()
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFE8EAF6), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
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
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PremiumAnimatedWaveCard(
                            modifier = Modifier.weight(1f).height(125.dp),
                            title = "Distance",
                            value = String.format("%.2f", distance),
                            unit = "km",
                            icon = Icons.AutoMirrored.Rounded.DirectionsRun,
                            colors = listOf(Color(0xFF9D50BB), Color(0xFF6E48AA)),
                            noiseLevel = userProfile.uiNoiseLevel,
                            onClick = onDistanceClick
                        )
                        PremiumAnimatedWaveCard(
                            modifier = Modifier.weight(1f).height(125.dp),
                            title = "Calories",
                            value = String.format("%.0f", calories),
                            unit = "kcal",
                            icon = Icons.Rounded.LocalFireDepartment,
                            colors = listOf(Color(0xFFFF8008), Color(0xFFFFC837)),
                            noiseLevel = userProfile.uiNoiseLevel,
                            onClick = onCaloriesClick
                        )
                        PremiumAnimatedWaveCard(
                            modifier = Modifier.weight(1f).height(125.dp),
                            title = "Pace",
                            value = paceStr,
                            unit = "min/km",
                            icon = Icons.Rounded.Speed,
                            colors = listOf(Color(0xFF00B4DB), Color(0xFF0083B0)),
                            noiseLevel = userProfile.uiNoiseLevel,
                            onClick = onDistanceClick
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))

                    if (!hasScrolledDown) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 28.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Box(
                                modifier = Modifier
                                    .offset(y = arrowOffsetY.dp)
                                    .clickable { hasScrolledDown = true }
                                    .shadow(4.dp, CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFF7F00FF), Color(0xFFE100FF))
                                        ),
                                        CircleShape
                                    )
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = "Scroll down to view sessions",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            if (hasScrolledDown) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Today's Sessions",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        textAlign = TextAlign.Start,
                        color = Color(0xFF1E1E1E)
                    )
                    
                    if (todaySessions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No sessions recorded today.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
                
                items(todaySessions.size) { index ->
                    val session = todaySessions[index]
                    SessionCard(
                        session = session,
                        noiseLevel = userProfile.uiNoiseLevel,
                        onClick = { selectedSessionForSummary = session }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
        
        selectedSessionForSummary?.let { session ->
            SessionSummaryModal(
                session = session,
                userProfile = userProfile,
                onDismiss = { selectedSessionForSummary = null }
            )
        }

        if (showSecretDialog) {
            SecretDevDialog(
                onDismiss = { showSecretDialog = false },
                onSave = { s, d, c ->
                    viewModel.setCustomActivity(s, d, c)
                    showSecretDialog = false
                }
            )
        }
    }
}

@Composable
fun GlassCard(modifier: Modifier = Modifier, noiseLevel: Float = 0f, content: @Composable BoxScope.() -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().noiseOverlay(noiseLevel), content = content)
    }
}

@Composable
fun SessionCard(session: WalkingSession, noiseLevel: Float, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val startTime = dateFormat.format(Date(session.startTimeMs))
    val endTime = dateFormat.format(Date(session.endTimeMs))
    
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        noiseLevel = noiseLevel
    ) {
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
                    text = "${String.format("%.2f", session.distanceKm)} km • Tap for summary",
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
    colors: List<Color>,
    noiseLevel: Float
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
                .noiseOverlay(noiseLevel)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, size.height * 0.65f)
                    cubicTo(
                        size.width * 0.3f, size.height * 0.55f,
                        size.width * 0.7f, size.height * 0.85f,
                        size.width, size.height * 0.7f
                    )
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path, color = Color.White.copy(alpha = 0.15f))
            }
            
            Column(
                modifier = Modifier.padding(16.dp).fillMaxSize(),
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
                            .size(32.dp)
                            .background(Color.White.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = value,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
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
                }
            }
        }
    }
}

@Composable
fun SecretDevDialog(onDismiss: () -> Unit, onSave: (Int, Float, Float) -> Unit) {
    var password by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var stepsInput by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var distInput by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var calInput by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var errorMsg by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { androidx.compose.material3.Text("Developer Console") },
        text = {
            androidx.compose.foundation.layout.Column(
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                androidx.compose.material3.OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMsg = "" },
                    label = { androidx.compose.material3.Text("Password") },
                    isError = errorMsg.isNotEmpty(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword)
                )
                if (errorMsg.isNotEmpty()) {
                    androidx.compose.material3.Text(
                        errorMsg,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                    )
                }
                if (password == "4921") {
                    androidx.compose.material3.OutlinedTextField(
                        value = stepsInput,
                        onValueChange = { stepsInput = it },
                        label = { androidx.compose.material3.Text("Custom Steps") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = distInput,
                        onValueChange = { distInput = it },
                        label = { androidx.compose.material3.Text("Custom Distance (km)") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = calInput,
                        onValueChange = { calInput = it },
                        label = { androidx.compose.material3.Text("Custom Calories (kcal)") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                    )
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = {
                if (password != "4921") {
                    errorMsg = "Incorrect Password"
                } else {
                    val s = stepsInput.toIntOrNull() ?: 0
                    val d = distInput.toFloatOrNull() ?: 0f
                    val c = calInput.toFloatOrNull() ?: 0f
                    onSave(s, d, c)
                }
            }) {
                androidx.compose.material3.Text("Save")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                androidx.compose.material3.Text("Cancel")
            }
        },
        containerColor = androidx.compose.ui.graphics.Color(0xFFFDFDFD),
        titleContentColor = androidx.compose.ui.graphics.Color(0xFF1E1E1E),
        textContentColor = androidx.compose.ui.graphics.Color(0xFF1E1E1E)
    )
}

@Composable
fun SessionSummaryModal(
    session: WalkingSession,
    userProfile: com.example.data.UserProfile,
    onDismiss: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val startTime = dateFormat.format(Date(session.startTimeMs))
    val endTime = dateFormat.format(Date(session.endTimeMs))
    
    val durationSeconds = ((session.endTimeMs - session.startTimeMs) / 1000).coerceAtLeast(0)
    val mins = durationSeconds / 60
    val secs = durationSeconds % 60
    val durationText = if (mins >= 60) {
        val hrs = mins / 60
        val remMins = mins % 60
        "${hrs}h ${remMins}m ${secs}s"
    } else {
        "${mins}m ${secs}s"
    }

    // Estimate calories burned for this session
    val factor = if (userProfile.gender == "Male") 1.03f else 0.98f
    val calories = session.distanceKm * userProfile.weightKg * factor

    // Pace & Speed calculation
    val durationMinutes = durationSeconds / 60f
    val speedKmH = if (durationMinutes > 0f) (session.distanceKm / (durationMinutes / 60f)) else 0f
    val paceMinKm = if (session.distanceKm > 0f) (durationMinutes / session.distanceKm) else 0f
    val pacePaceMins = paceMinKm.toInt()
    val pacePaceSecs = ((paceMinKm - pacePaceMins) * 60).toInt().coerceIn(0, 59)
    val paceStr = if (session.distanceKm > 0f) "${pacePaceMins}'${String.format("%02d", pacePaceSecs)}\" /km" else "--"

    val goalContribution = if (userProfile.dailyStepGoal > 0) {
        ((session.steps.toFloat() / userProfile.dailyStepGoal.toFloat()) * 100f).coerceAtMost(100f)
    } else 0f

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val modalScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.82f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "modalScale"
    )

    val modalAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "modalAlpha"
    )

    val modalOffsetY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 60f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "modalOffsetY"
    )

    val animatedSteps by animateIntAsState(
        targetValue = if (isVisible) session.steps else 0,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "animatedSteps"
    )

    val infiniteGradientTransition = rememberInfiniteTransition(label = "bannerGradientTransition")
    val gradientShift by infiniteGradientTransition.animateFloat(
        initialValue = 0f,
        targetValue = 500f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientShift"
    )

    val headerGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFF8008),
            Color(0xFFFF3D00),
            Color(0xFFDD2476),
            Color(0xFFFF8008)
        ),
        start = Offset(gradientShift, 0f),
        end = Offset(gradientShift + 700f, 700f)
    )

    val doneButtonGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFF8008),
            Color(0xFFFF3D00),
            Color(0xFFDD2476)
        ),
        start = Offset(gradientShift, 0f),
        end = Offset(gradientShift + 500f, 250f)
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .graphicsLayer {
                    scaleX = modalScale
                    scaleY = modalScale
                    alpha = modalAlpha
                    translationY = modalOffsetY
                }
                .shadow(24.dp, RoundedCornerShape(32.dp))
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Banner Animated Gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerGradient)
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF00E676),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "WALKING SESSION",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = Color.White
                                    )
                                }
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Close",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Large Step Count Display with Live Animated Count Up
                        Text(
                            text = String.format("%,d", animatedSteps),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 42.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "TOTAL STEPS WALKED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                // Body content
                Column(modifier = Modifier.padding(20.dp)) {
                    // Time & Duration Card
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFF6F3FF),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.AccessTime,
                                    contentDescription = null,
                                    tint = Color(0xFFFF3D00),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$startTime - $endTime",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF1E1E1E)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Timer,
                                    contentDescription = null,
                                    tint = Color(0xFFFF3D00),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = durationText,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFFF3D00)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3 Metric Grid Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SessionMetricTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.AutoMirrored.Rounded.DirectionsRun,
                            value = String.format("%.2f km", session.distanceKm),
                            label = "Distance",
                            color = Color(0xFF00B0FF)
                        )
                        SessionMetricTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.LocalFireDepartment,
                            value = String.format("%.0f kcal", calories),
                            label = "Calories",
                            color = Color(0xFFFF3D00)
                        )
                        SessionMetricTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Speed,
                            value = String.format("%.1f km/h", speedKmH),
                            label = "Avg Speed",
                            color = Color(0xFF00E676)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Daily Goal Contribution Bar
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF9FAFC), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daily Goal Contribution",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF1E1E1E)
                            )
                            Text(
                                text = String.format("%.1f%%", goalContribution),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFF3D00)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val animRatio by animateFloatAsState(
                            targetValue = (goalContribution / 100f).coerceIn(0f, 1f),
                            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                            label = "goalContribAnim"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.08f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(animRatio)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFFFF8008), Color(0xFFDD2476))
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Pace: $paceStr",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Close Button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .shadow(8.dp, CircleShape),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    doneButtonGradient,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Done",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionMetricTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.08f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF1E1E1E),
                fontSize = 13.sp,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontSize = 10.sp
            )
        }
    }
}
