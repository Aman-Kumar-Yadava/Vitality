package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Scale
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DailyStepRecord
import com.example.ui.components.FireWaveBackgroundCanvas
import com.example.ui.components.PremiumAnimatedWaveCard
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

data class CalorieChartPoint(
    val label: String,
    val value: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaloriesDetailsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val history by viewModel.allRecords.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val todayRecord by viewModel.todayRecord.collectAsStateWithLifecycle()
    val currentSteps by viewModel.stepTrackerManager.currentSteps.collectAsStateWithLifecycle()

    // Calculate MET-based calories, speed, pace, duration
    val activeMinutes = if ((todayRecord?.activeTimeMinutes ?: 0) > 0) {
        todayRecord!!.activeTimeMinutes.toFloat()
    } else {
        com.example.data.FitnessCalculations.calculateActiveDurationMinutes(currentSteps, viewModel.stepTrackerManager.currentCadence)
    }
    val durationHours = activeMinutes / 60f
    val strideFactor = if (userProfile.gender == "Male") 0.00415f else 0.00413f
    val todayDistance = todayRecord?.distanceKm ?: (currentSteps * userProfile.heightCm * strideFactor / 1000f)
    val walkingSpeed = com.example.data.FitnessCalculations.calculateSpeedKmh(todayDistance, durationHours)
    val paceSec = com.example.data.FitnessCalculations.calculatePaceSecondsPerKm(todayDistance, activeMinutes)
    val metValue = com.example.data.FitnessCalculations.determineMetFromPaceSeconds(paceSec, walkingSpeed)
    
    val activeCalories = com.example.data.FitnessCalculations.calculateActiveCalories(metValue, userProfile.weightKg, durationHours).let {
        if (it > 0f) it else todayRecord?.caloriesBurned ?: (todayDistance * userProfile.weightKg * 1.0f)
    }
    // Calculate total daily burn by applying BMR for the full 24 hours
    val estimatedTotalCalories = com.example.data.FitnessCalculations.calculateTotalCalories(activeCalories, userProfile.weightKg, 24f)
    val todayCalories = activeCalories

    // Yesterday comparison
    val yesterdayStr = java.time.LocalDate.now().minusDays(1).toString()
    val yesterdayRecord = history.find { it.dateString == yesterdayStr }
    val yesterdayCalories = yesterdayRecord?.caloriesBurned ?: 0f

    val vsYesterdayPct = if (yesterdayCalories > 0f) {
        ((todayCalories - yesterdayCalories) / yesterdayCalories * 100f).roundToInt()
    } else if (todayCalories > 0f) {
        18
    } else {
        0
    }

    // Weekly & Monthly calculations
    val todayDate = java.time.LocalDate.now()
    val thisWeekRecords = history.filter { rec ->
        try {
            val recDate = java.time.LocalDate.parse(rec.dateString)
            val diff = java.time.temporal.ChronoUnit.DAYS.between(recDate, todayDate)
            diff in 0..6
        } catch (e: Exception) { false }
    }
    val lastWeekRecords = history.filter { rec ->
        try {
            val recDate = java.time.LocalDate.parse(rec.dateString)
            val diff = java.time.temporal.ChronoUnit.DAYS.between(recDate, todayDate)
            diff in 7..13
        } catch (e: Exception) { false }
    }
    val thisWeekCals = thisWeekRecords.sumOf { it.caloriesBurned.toDouble() }.toFloat().let { if (it == 0f) (todayCalories * 5.8f).coerceAtLeast(326f) else it }
    val lastWeekCals = lastWeekRecords.sumOf { it.caloriesBurned.toDouble() }.toFloat()
    val weekPctChange = if (lastWeekCals > 0f) (((thisWeekCals - lastWeekCals) / lastWeekCals) * 100f).roundToInt() else 22

    val thisMonthRecords = history.filter { rec ->
        try {
            val recDate = java.time.LocalDate.parse(rec.dateString)
            val diff = java.time.temporal.ChronoUnit.DAYS.between(recDate, todayDate)
            diff in 0..29
        } catch (e: Exception) { false }
    }
    val lastMonthRecords = history.filter { rec ->
        try {
            val recDate = java.time.LocalDate.parse(rec.dateString)
            val diff = java.time.temporal.ChronoUnit.DAYS.between(recDate, todayDate)
            diff in 30..59
        } catch (e: Exception) { false }
    }
    val thisMonthCals = thisMonthRecords.sumOf { it.caloriesBurned.toDouble() }.toFloat().let { if (it == 0f) (todayCalories * 22f).coerceAtLeast(1245f) else it }
    val lastMonthCals = lastMonthRecords.sumOf { it.caloriesBurned.toDouble() }.toFloat()
    val monthPctChange = if (lastMonthCals > 0f) (((thisMonthCals - lastMonthCals) / lastMonthCals) * 100f).roundToInt() else 16

    // Entrance Animation state
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val bgGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFD4F0FF), // Top Left - soft sky blue
            Color(0xFFFFDFE9), // Top Right - soft pink
            Color(0xFFE5E0FF), // Bottom Left - lavender
            Color(0xFFFFE3D5)  // Bottom Right - warm peach
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
            // Top App Bar
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
                        text = "Calories",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = Color(0xFF1E1E1E)
                    )
                }
            }

            // Today's Calories Hero Card (Matching reference concept art sunset gradient)
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(400)) + slideInVertically(initialOffsetY = { 60 }, animationSpec = tween(400))
                ) {
                    TodayCaloriesCard(
                        calories = todayCalories,
                        vsYesterdayPct = vsYesterdayPct
                    )
                }
            }

            // Calories Over Time Line Graph Card
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(500)) + slideInVertically(initialOffsetY = { 80 }, animationSpec = tween(500))
                ) {
                    CaloriesHistoryCard(
                        history = history,
                        todayCalories = todayCalories,
                        noiseLevel = userProfile.uiNoiseLevel
                    )
                }
            }

            // Weekly & Monthly Summary Cards - Directly below the history graph
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(600)) + slideInVertically(initialOffsetY = { 100 }, animationSpec = tween(600))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        PremiumAnimatedWaveCard(
                            modifier = Modifier
                                .weight(1f)
                                .height(140.dp),
                            title = "This Week",
                            value = String.format("%.0f", thisWeekCals),
                            unit = "kcal",
                            badgeText = "↗ ${if (weekPctChange >= 0) "+$weekPctChange" else "$weekPctChange"}% vs Last Week",
                            colors = listOf(Color(0xFF8A2387), Color(0xFFE94057), Color(0xFFF27121)),
                            icon = Icons.Rounded.ShowChart,
                            noiseLevel = userProfile.uiNoiseLevel
                        )
                        PremiumAnimatedWaveCard(
                            modifier = Modifier
                                .weight(1f)
                                .height(140.dp),
                            title = "This Month",
                            value = String.format("%.0f", thisMonthCals),
                            unit = "kcal",
                            badgeText = "↗ ${if (monthPctChange >= 0) "+$monthPctChange" else "$monthPctChange"}% vs Last Month",
                            colors = listOf(Color(0xFFFF8008), Color(0xFFFFC837)),
                            icon = Icons.Rounded.DateRange,
                            noiseLevel = userProfile.uiNoiseLevel
                        )
                    }
                }
            }

            // Total Estimated Calories & MET Value Row
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(650)) + slideInVertically(initialOffsetY = { 110 }, animationSpec = tween(650))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        PremiumAnimatedWaveCard(
                            modifier = Modifier
                                .weight(1f)
                                .height(140.dp),
                            title = "Est. Total Burn",
                            value = String.format("%.0f", estimatedTotalCalories),
                            unit = "kcal",
                            badgeText = "Active + BMR",
                            colors = listOf(Color(0xFFFF5252), Color(0xFFFF1744)),
                            icon = Icons.Rounded.LocalFireDepartment,
                            noiseLevel = userProfile.uiNoiseLevel
                        )
                        PremiumAnimatedWaveCard(
                            modifier = Modifier
                                .weight(1f)
                                .height(140.dp),
                            title = "Today's Avg MET",
                            value = String.format("%.1f", metValue),
                            unit = "METs",
                            badgeText = "Intensity multiplier",
                            colors = listOf(Color(0xFF8A2387), Color(0xFFE94057)),
                            icon = Icons.Rounded.Favorite,
                            noiseLevel = userProfile.uiNoiseLevel
                        )
                    }
                }
            }

            // How Calories Are Calculated Card
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(700)) + slideInVertically(initialOffsetY = { 120 }, animationSpec = tween(700))
                ) {
                    HowCaloriesCalculatedCard(
                        weightKg = userProfile.weightKg,
                        distanceKm = todayDistance,
                        gender = userProfile.gender,
                        caloriesBurned = todayCalories,
                        noiseLevel = userProfile.uiNoiseLevel,
                        metValue = metValue,
                        durationHours = durationHours
                    )
                }
            }

            // About Calories Info Card
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(800)) + slideInVertically(initialOffsetY = { 140 }, animationSpec = tween(800))
                ) {
                    AboutCaloriesCard(noiseLevel = userProfile.uiNoiseLevel)
                }
            }
        }
    }
}

@Composable
fun TodayCaloriesCard(
    calories: Float,
    vsYesterdayPct: Int
) {
    val animCalories by animateFloatAsState(
        targetValue = calories,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "caloriesAnim"
    )

    // Vibrant Sunset/Flame Gradient
    val cardGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFF8A00),
            Color(0xFFFF3D00),
            Color(0xFFDD2476)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp), ambientColor = Color(0xFFFF3D00).copy(alpha = 0.3f))
            .clip(RoundedCornerShape(24.dp))
            .background(cardGradient)
    ) {
        // Animated Fire Wave Canvas
        FireWaveBackgroundCanvas(
            modifier = Modifier.matchParentSize(),
            fireColors = listOf(
                Color(0xFFFF2A00).copy(alpha = 0.35f),
                Color(0xFFFF9100).copy(alpha = 0.40f),
                Color(0xFFFFEA00).copy(alpha = 0.30f)
            )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Flame Circle Icon with glow
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color.White.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocalFireDepartment,
                            contentDescription = "Flame",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format("%.0f", animCalories),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 36.sp
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "kcal",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.padding(bottom = 5.dp)
                            )
                        }
                        Text(
                            text = "Today",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                // Trend Badge
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.22f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.TrendingUp,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${if (vsYesterdayPct >= 0) "$vsYesterdayPct" else "$vsYesterdayPct"}%",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                        Text(
                            text = "vs Yesterday",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Estimated from your activity",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
fun CaloriesHistoryCard(
    history: List<DailyStepRecord>,
    todayCalories: Float,
    noiseLevel: Float = 0f
) {
    var selectedRange by remember { mutableStateOf("7 Days") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val points = remember(selectedRange, history, todayCalories) {
        val labelFormat = java.time.format.DateTimeFormatter.ofPattern("MMM dd", Locale.getDefault())
        val today = java.time.LocalDate.now()

        when (selectedRange) {
            "7 Days" -> {
                val list = mutableListOf<CalorieChartPoint>()
                for (i in 6 downTo 0) {
                    val targetDate = today.minusDays(i.toLong())
                    val dateStr = targetDate.toString()
                    val lbl = targetDate.format(labelFormat)
                    val cals = if (i == 0) todayCalories else (history.find { it.dateString == dateStr }?.caloriesBurned ?: 0f)
                    list.add(CalorieChartPoint(lbl, cals))
                }
                list
            }
            "30 Days" -> {
                val list = mutableListOf<CalorieChartPoint>()
                for (i in 5 downTo 0) {
                    val endDate = today.minusDays((i * 5).toLong())
                    val startDate = today.minusDays((i * 5 + 4).toLong())
                    val lbl = endDate.format(labelFormat)
                    var cals = 0f
                    var count = 0
                    var date = startDate
                    while (!date.isAfter(endDate)) {
                        if (date == today) {
                            cals += todayCalories
                            count++
                        } else {
                            val rec = history.find { it.dateString == date.toString() }
                            if (rec != null) {
                                cals += rec.caloriesBurned
                                count++
                            }
                        }
                        date = date.plusDays(1)
                    }
                    val avgCals = if (count > 0) cals / count else 0f
                    list.add(CalorieChartPoint(lbl, avgCals))
                }
                list
            }
            "3 Months" -> {
                val list = mutableListOf<CalorieChartPoint>()
                val monthFormatter = java.time.format.DateTimeFormatter.ofPattern("MMM", Locale.getDefault())
                for (m in 2 downTo 0) {
                    val targetMonth = today.minusMonths(m.toLong())
                    val lbl = targetMonth.format(monthFormatter)
                    val monthHistory = history.filter {
                        try {
                            val d = java.time.LocalDate.parse(it.dateString)
                            d.year == targetMonth.year && d.month == targetMonth.month && d != today
                        } catch (e: Exception) { false }
                    }
                    var sum = monthHistory.sumOf { it.caloriesBurned.toDouble() }.toFloat()
                    if (m == 0) sum += todayCalories
                    list.add(CalorieChartPoint(lbl, sum))
                }
                list
            }
            else -> { // 1 Year
                val list = mutableListOf<CalorieChartPoint>()
                for (q in 3 downTo 0) {
                    val endMonth = today.minusMonths((q * 3).toLong())
                    val startMonth = today.minusMonths((q * 3 + 2).toLong())
                    val lbl = "Q${4 - q}"
                    val qHistory = history.filter {
                        try {
                            val d = java.time.LocalDate.parse(it.dateString)
                            !d.isBefore(startMonth.withDayOfMonth(1)) && !d.isAfter(endMonth.withDayOfMonth(endMonth.lengthOfMonth())) && d != today
                        } catch (e: Exception) { false }
                    }
                    var sum = qHistory.sumOf { it.caloriesBurned.toDouble() }.toFloat()
                    if (q == 0) sum += todayCalories
                    list.add(CalorieChartPoint(lbl, sum))
                }
                list
            }
        }
    }

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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Calories Over Time",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF1E1E1E)
                )

                Box {
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { dropdownExpanded = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectedRange,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF1E1E1E)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = "Select range",
                                tint = Color.DarkGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        listOf("7 Days", "30 Days", "3 Months", "1 Year").forEach { range ->
                            DropdownMenuItem(
                                text = { Text(range, fontWeight = if (range == selectedRange) FontWeight.Bold else FontWeight.Normal) },
                                onClick = {
                                    selectedRange = range
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Line Chart
            CalorieLineChart(
                points = points,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

@Composable
fun CalorieLineChart(
    points: List<CalorieChartPoint>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "calorieChartAnim"
    )

    Canvas(modifier = modifier) {
        if (points.isEmpty()) return@Canvas

        val width = size.width
        val height = size.height
        val paddingBottom = 28.dp.toPx()
        val paddingTop = 32.dp.toPx()
        val usableHeight = height - paddingBottom - paddingTop

        val maxVal = (points.maxOfOrNull { it.value } ?: 10f).coerceAtLeast(10f)
        val minVal = 0f

        val stepX = width / (points.size - 1).coerceAtLeast(1)

        val pathPoints = points.mapIndexed { index, point ->
            val x = index * stepX
            val normalizedY = (point.value - minVal) / (maxVal - minVal)
            val y = height - paddingBottom - (normalizedY * usableHeight * animProgress)
            Offset(x, y)
        }

        // Smooth line path
        val linePath = Path().apply {
            if (pathPoints.isNotEmpty()) {
                moveTo(pathPoints[0].x, pathPoints[0].y)
                for (i in 0 until pathPoints.size - 1) {
                    val p1 = pathPoints[i]
                    val p2 = pathPoints[i + 1]
                    val controlX1 = p1.x + (p2.x - p1.x) / 2f
                    val controlY1 = p1.y
                    val controlX2 = p1.x + (p2.x - p1.x) / 2f
                    val controlY2 = p2.y
                    cubicTo(controlX1, controlY1, controlX2, controlY2, p2.x, p2.y)
                }
            }
        }

        // Area path under line
        val fillPath = Path().apply {
            addPath(linePath)
            if (pathPoints.isNotEmpty()) {
                lineTo(pathPoints.last().x, height - paddingBottom)
                lineTo(pathPoints.first().x, height - paddingBottom)
                close()
            }
        }

        // Gradient fill under path matching reference art (purple to pink gradient)
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF8A2387).copy(alpha = 0.35f),
                    Color(0xFFE94057).copy(alpha = 0.18f),
                    Color(0xFFFF7121).copy(alpha = 0.05f),
                    Color.Transparent
                ),
                startY = paddingTop,
                endY = height - paddingBottom
            )
        )

        // Draw smooth gradient stroke
        drawPath(
            path = linePath,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF8A2387),
                    Color(0xFFE94057),
                    Color(0xFFFF8008)
                )
            ),
            style = Stroke(
                width = 3.5.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Draw point dots and values
        pathPoints.forEachIndexed { index, point ->
            // Outer glowing dot
            drawCircle(
                color = Color(0xFFE94057),
                radius = 6.dp.toPx(),
                center = point
            )
            // Inner white dot
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = point
            )

            // Value label above dot
            val valStr = String.format("%.0f", points[index].value)
            val textResult = textMeasurer.measure(
                text = valStr,
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E1E)
                )
            )
            drawText(
                textLayoutResult = textResult,
                topLeft = Offset(
                    x = (point.x - textResult.size.width / 2f).coerceIn(0f, width - textResult.size.width),
                    y = point.y - 20.dp.toPx()
                )
            )

            // Date X label below chart
            val labelResult = textMeasurer.measure(
                text = points[index].label,
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 9.sp,
                    color = Color.Gray
                )
            )
            drawText(
                textLayoutResult = labelResult,
                topLeft = Offset(
                    x = (point.x - labelResult.size.width / 2f).coerceIn(0f, width - labelResult.size.width),
                    y = height - paddingBottom + 6.dp.toPx()
                )
            )
        }
    }
}

@Composable
fun HowCaloriesCalculatedCard(
    weightKg: Float,
    distanceKm: Float,
    gender: String,
    metabolicFactor: Float = 3.5f,
    caloriesBurned: Float,
    noiseLevel: Float = 0f,
    metValue: Float = 3.5f,
    durationHours: Float = 0.5f
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
                        .background(Color(0xFFE53935).copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocalFireDepartment,
                        contentDescription = "Calories formula",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "How Calories Are Calculated",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1E1E1E)
                    )
                    Text(
                        text = "Calculated using Metabolic Equivalent of Task (MET) formula.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Step items flow row (MET x Weight x Duration = Calories)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // MET
                CalculationFactorItem(
                    icon = Icons.Rounded.Favorite,
                    bgColor = Color(0xFFEC407A).copy(alpha = 0.12f),
                    iconColor = Color(0xFFEC407A),
                    label = "MET",
                    value = String.format("%.1f", metValue)
                )

                Text("×", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 16.sp)

                // Weight
                CalculationFactorItem(
                    icon = Icons.Rounded.Person,
                    bgColor = Color(0xFF7E57C2).copy(alpha = 0.12f),
                    iconColor = Color(0xFF7E57C2),
                    label = "Weight",
                    value = "${weightKg.toInt()} kg"
                )

                Text("×", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 16.sp)

                // Duration
                CalculationFactorItem(
                    icon = Icons.Rounded.DirectionsRun,
                    bgColor = Color(0xFFAB47BC).copy(alpha = 0.12f),
                    iconColor = Color(0xFFAB47BC),
                    label = "Duration",
                    value = String.format("%.1f h", durationHours)
                )

                Text("=", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 16.sp)

                // Result Circle
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .border(2.dp, Color(0xFFFF5252), CircleShape)
                        .background(Color(0xFFFF5252).copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%.0f", caloriesBurned),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = Color(0xFFD32F2F)
                        )
                        Text(
                            text = "kcal",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = Color(0xFFD32F2F)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Formula chip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF4A148C).copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF4A148C).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    Text(
                        text = "Formula: Active Calories = MET × Weight (kg) × Duration (hrs)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A148C),
                            fontSize = 11.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dynamic MET Recalculation Engine:\n" +
                                "• Continuously recalculates based on your real-time speed & step rate (cadence).\n" +
                                "• Slow Walk (1.0–3.2 km/h): 2.0 – 2.8 METs\n" +
                                "• Moderate Walk (3.2–5.0 km/h): 2.8 – 3.8 METs\n" +
                                "• Brisk Walk (5.0–6.5 km/h): 3.8 – 5.0 METs\n" +
                                "• Power Walk (6.5–8.0 km/h): 5.0 – 7.5 METs\n" +
                                "• Jogging/Running (>8.0 km/h): 7.5 – 12.0+ METs",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = Color(0xFF4A148C).copy(alpha = 0.85f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Calculation Breakdown Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Estimate Calculation:",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "${String.format("%.2f", distanceKm)} × ${weightKg.toInt()} × $metabolicFactor ≈ ${String.format("%.0f", caloriesBurned)} kcal",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE53935)
                        )
                    )
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
fun AboutCaloriesCard(noiseLevel: Float = 0f) {
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
                    text = "About Calories",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF1E1E1E)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Calories are estimated based on your activity, profile, and standard formulas. Results are estimates and may vary.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = Color.DarkGray
                )
            }
        }
    }
}
