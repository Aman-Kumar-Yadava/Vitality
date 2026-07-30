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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
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

    var tapCount by remember { mutableIntStateOf(0) }
    var lastTapTime by remember { mutableLongStateOf(0L) }
    var showSecretDialog by remember { mutableStateOf(false) }

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
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 48.dp, bottom = 96.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().height(screenHeight - 48.dp),
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
                        colors = if (isTracking) listOf(Color(0xFFFF512F), Color(0xFFDD2476)) 
                                 else listOf(Color(0xFF6143FF), Color(0xFFF72585), Color(0xFFFF8947))
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
                                .background(btnGradient, RoundedCornerShape(32.dp))
                                .noiseOverlay(userProfile.uiNoiseLevel),
                            contentAlignment = Alignment.Center
                        ) {
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
                                    text = if (isTracking) "Pause Session" else "Start Session", 
                                    color = Color.White, 
                                    fontWeight = FontWeight.SemiBold, 
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Main Ring Card
                    GlassCard(modifier = Modifier.fillMaxWidth().weight(1f), noiseLevel = userProfile.uiNoiseLevel) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                val minDim = minOf(maxWidth, maxHeight)
                                val canvasSize = minDim * 0.8f
                                PremiumAnimatedRing(
                                    progress = progress,
                                    modifier = Modifier.size(canvasSize).align(Alignment.Center),
                                    trackColor = Color(0xFFE0E0E0).copy(alpha = 0.4f),
                                    strokeWidth = 20.dp
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                PulsingRunningIcon(modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "%,d".format(steps),
                                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                                    color = Color(0xFF1E1E1E)
                                )
                                Text(
                                    text = "/ %,d steps".format(goal),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.DarkGray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                val percent = (progress * 100).toInt()
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
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PremiumAnimatedWaveCard(
                            modifier = Modifier.weight(1f).height(130.dp),
                            title = "Distance",
                            value = String.format("%.2f", distance),
                            unit = "km",
                            icon = Icons.AutoMirrored.Rounded.DirectionsRun,
                            colors = listOf(Color(0xFF9D50BB), Color(0xFF6E48AA)),
                            noiseLevel = userProfile.uiNoiseLevel
                        )
                        PremiumAnimatedWaveCard(
                            modifier = Modifier.weight(1f).height(130.dp),
                            title = "Calories",
                            value = String.format("%.0f", calories),
                            unit = "kcal",
                            icon = Icons.Rounded.LocalFireDepartment,
                            colors = listOf(Color(0xFFFF8008), Color(0xFFFFC837)),
                            noiseLevel = userProfile.uiNoiseLevel
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(96.dp))
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
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
                SessionCard(session, userProfile.uiNoiseLevel)
                Spacer(modifier = Modifier.height(12.dp))
            }
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
fun SessionCard(session: WalkingSession, noiseLevel: Float) {
    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val startTime = dateFormat.format(Date(session.startTimeMs))
    val endTime = dateFormat.format(Date(session.endTimeMs))
    
    GlassCard(modifier = Modifier.fillMaxWidth(), noiseLevel = noiseLevel) {
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
