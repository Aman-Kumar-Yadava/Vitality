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
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
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

data class ChartPointData(
    val label: String,
    val value: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistanceDetailsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onPaceClick: (() -> Unit)? = null
) {
    val history by viewModel.allRecords.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val todayRecord by viewModel.todayRecord.collectAsStateWithLifecycle()
    val currentSteps by viewModel.stepTrackerManager.currentSteps.collectAsStateWithLifecycle()

    // Calculate current today distance
    val todayDistance = todayRecord?.distanceKm ?: (currentSteps * (if (userProfile.gender == "Male") userProfile.heightCm * 0.00415f else userProfile.heightCm * 0.00413f) / 1000f)

    // Calculate yesterday distance for comparison
    val yesterdayStr = java.time.LocalDate.now().minusDays(1).toString()
    val yesterdayRecord = history.find { it.dateString == yesterdayStr }
    val yesterdayDistance = yesterdayRecord?.distanceKm ?: 0f

    val vsYesterdayPct = if (yesterdayDistance > 0f) {
        ((todayDistance - yesterdayDistance) / yesterdayDistance * 100f).roundToInt()
    } else if (todayDistance > 0f) {
        100
    } else {
        0
    }

    val activeMinutes = if ((todayRecord?.activeTimeMinutes ?: 0) > 0) {
        todayRecord!!.activeTimeMinutes.toFloat()
    } else {
        com.example.data.FitnessCalculations.calculateActiveDurationMinutes(currentSteps, viewModel.stepTrackerManager.currentCadence)
    }
    val durationHours = activeMinutes / 60f
    val walkingSpeed = com.example.data.FitnessCalculations.calculateSpeedKmh(todayDistance, durationHours)
    val avgPaceSec = com.example.data.FitnessCalculations.calculatePaceSecondsPerKm(todayDistance, activeMinutes)
    val avgPaceFormatted = com.example.data.FitnessCalculations.formatPaceWithUnit(avgPaceSec)
    val currentCadence = viewModel.stepTrackerManager.currentCadence
    val adaptiveStrideMeters = com.example.data.FitnessCalculations.calculateAdaptiveStrideMeters(
        userProfile.heightCm, userProfile.gender, currentCadence
    )
    val todayAvgStrideMeters = if (currentSteps > 0) {
        (todayDistance * 1000f) / currentSteps
    } else {
        adaptiveStrideMeters
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
    val thisWeekDist = thisWeekRecords.sumOf { it.distanceKm.toDouble() }.toFloat().let { if (it == 0f) todayDistance else it }
    val lastWeekDist = lastWeekRecords.sumOf { it.distanceKm.toDouble() }.toFloat()
    val weekPctChange = if (lastWeekDist > 0f) (((thisWeekDist - lastWeekDist) / lastWeekDist) * 100f).roundToInt() else 18

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
    val thisMonthDist = thisMonthRecords.sumOf { it.distanceKm.toDouble() }.toFloat().let { if (it == 0f) todayDistance * 3.5f else it }
    val lastMonthDist = lastMonthRecords.sumOf { it.distanceKm.toDouble() }.toFloat()
    val monthPctChange = if (lastMonthDist > 0f) (((thisMonthDist - lastMonthDist) / lastMonthDist) * 100f).roundToInt() else 25

    // Entrance Animation state
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
                        text = "Distance",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = Color(0xFF1E1E1E)
                    )
                }
            }

            // Today's Distance Hero Card
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(400)) + slideInVertically(initialOffsetY = { 60 }, animationSpec = tween(400))
                ) {
                    TodayDistanceCard(
                        distanceKm = todayDistance,
                        vsYesterdayPct = vsYesterdayPct
                    )
                }
            }

            // Distance History Card
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(500)) + slideInVertically(initialOffsetY = { 80 }, animationSpec = tween(500))
                ) {
                    DistanceHistoryCard(
                        history = history,
                        todayDistance = todayDistance,
                        noiseLevel = userProfile.uiNoiseLevel
                    )
                }
            }

            // Summary Grid Cards (This Week & This Month) - Directly below the history graph
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
                            value = String.format("%.2f", thisWeekDist),
                            unit = "km",
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
                            value = String.format("%.2f", thisMonthDist),
                            unit = "km",
                            badgeText = "↗ ${if (monthPctChange >= 0) "+$monthPctChange" else "$monthPctChange"}% vs Last Month",
                            colors = listOf(Color(0xFFFF8008), Color(0xFFFFC837)),
                            icon = Icons.Rounded.DateRange,
                            noiseLevel = userProfile.uiNoiseLevel
                        )
                    }
                }
            }

            // Estimated Stride Length Card
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(700)) + slideInVertically(initialOffsetY = { 120 }, animationSpec = tween(700))
                ) {
                    StrideLengthCard(
                        strideMeters = todayAvgStrideMeters,
                        heightCm = userProfile.heightCm,
                        gender = userProfile.gender,
                        formulaStr = "Base Stride = Height × ${if (userProfile.gender == "Female") "0.413" else "0.415"}",
                        noiseLevel = userProfile.uiNoiseLevel,
                        userProfile = userProfile
                    )
                }
            }

            // Small Information Card
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(800)) + slideInVertically(initialOffsetY = { 140 }, animationSpec = tween(800))
                ) {
                    AboutDistanceCard(noiseLevel = userProfile.uiNoiseLevel)
                }
            }
        }
    }
}

@Composable
fun TodayDistanceCard(
    distanceKm: Float,
    vsYesterdayPct: Int
) {
    val animDistance by animateFloatAsState(
        targetValue = distanceKm,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "distanceAnim"
    )

    // Vibrant Fire/Magma Gradient
    val cardGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF8000FF), // Deep Violet-Indigo
            Color(0xFFE00060), // Vibrant Burning Rose
            Color(0xFFFF5000)  // Fire Orange
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp), ambientColor = Color(0xFFE00060).copy(alpha = 0.3f))
            .clip(RoundedCornerShape(24.dp))
            .background(cardGradient)
    ) {
        // Animated Fire/Flame Wave Background
        FireWaveBackgroundCanvas(
            modifier = Modifier.matchParentSize(),
            fireColors = listOf(
                Color(0xFFFF3D00).copy(alpha = 0.35f), // Red-Orange
                Color(0xFFFF9100).copy(alpha = 0.40f), // Flame Orange
                Color(0xFFFFEA00).copy(alpha = 0.30f)  // Golden Spark
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
                    // Location/Distance Circle Icon with glow container
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color.White.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = "Location",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format("%.2f", animDistance),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 36.sp
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "km",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.padding(bottom = 5.dp)
                            )
                        }
                        Text(
                            text = "Today's Distance",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                // Comparison Badge
                Box(
                    modifier = Modifier
                        .background(
                            Color.White.copy(alpha = 0.22f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.TrendingUp,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${if (vsYesterdayPct >= 0) "+$vsYesterdayPct" else "$vsYesterdayPct"}%",
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

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Estimated from your step activity & stride length",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
fun DistanceHistoryCard(
    history: List<DailyStepRecord>,
    todayDistance: Float,
    noiseLevel: Float = 0f
) {
    var selectedRange by remember { mutableStateOf("7 Days") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Generate chart data for the selected timeframe
    val points = remember(selectedRange, history, todayDistance) {
        val labelFormat = java.time.format.DateTimeFormatter.ofPattern("MMM dd", Locale.getDefault())
        val today = java.time.LocalDate.now()

        when (selectedRange) {
            "7 Days" -> {
                val list = mutableListOf<ChartPointData>()
                for (i in 6 downTo 0) {
                    val targetDate = today.minusDays(i.toLong())
                    val dateStr = targetDate.toString()
                    val lbl = targetDate.format(labelFormat)
                    val dist = if (i == 0) todayDistance else (history.find { it.dateString == dateStr }?.distanceKm ?: 0f)
                    list.add(ChartPointData(lbl, dist))
                }
                list
            }
            "30 Days" -> {
                val list = mutableListOf<ChartPointData>()
                for (i in 5 downTo 0) {
                    val endDate = today.minusDays((i * 5).toLong())
                    val startDate = today.minusDays((i * 5 + 4).toLong())
                    val lbl = endDate.format(labelFormat)
                    var distSum = 0f
                    var count = 0
                    var date = startDate
                    while (!date.isAfter(endDate)) {
                        if (date == today) {
                            distSum += todayDistance
                            count++
                        } else {
                            val rec = history.find { it.dateString == date.toString() }
                            if (rec != null) {
                                distSum += rec.distanceKm
                                count++
                            }
                        }
                        date = date.plusDays(1)
                    }
                    val avgDist = if (count > 0) distSum / count else 0f
                    list.add(ChartPointData(lbl, avgDist))
                }
                list
            }
            "3 Months" -> {
                val list = mutableListOf<ChartPointData>()
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
                    var sum = monthHistory.sumOf { it.distanceKm.toDouble() }.toFloat()
                    if (m == 0) sum += todayDistance
                    list.add(ChartPointData(lbl, sum))
                }
                list
            }
            else -> { // 1 Year
                val list = mutableListOf<ChartPointData>()
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
                    var sum = qHistory.sumOf { it.distanceKm.toDouble() }.toFloat()
                    if (q == 0) sum += todayDistance
                    list.add(ChartPointData(lbl, sum))
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
                    text = "Distance Over Time",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF1E1E1E)
                )

                // Dropdown selector
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
            DistanceLineChart(
                points = points,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

@Composable
fun DistanceLineChart(
    points: List<ChartPointData>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "chartAnim"
    )

    Canvas(modifier = modifier) {
        if (points.isEmpty()) return@Canvas

        val width = size.width
        val height = size.height
        val paddingBottom = 28.dp.toPx()
        val paddingTop = 32.dp.toPx()
        val usableHeight = height - paddingBottom - paddingTop

        val maxVal = (points.maxOfOrNull { it.value } ?: 1f).coerceAtLeast(0.5f)
        val minVal = 0f

        val stepX = width / (points.size - 1).coerceAtLeast(1)

        val pathPoints = points.mapIndexed { index, point ->
            val x = index * stepX
            val normalizedY = (point.value - minVal) / (maxVal - minVal)
            val y = height - paddingBottom - (normalizedY * usableHeight * animProgress)
            Offset(x, y)
        }

        // Build smooth line path
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

        // Build filled area under line
        val fillPath = Path().apply {
            addPath(linePath)
            if (pathPoints.isNotEmpty()) {
                lineTo(pathPoints.last().x, height - paddingBottom)
                lineTo(pathPoints.first().x, height - paddingBottom)
                close()
            }
        }

        // Draw soft gradient fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF7F00FF).copy(alpha = 0.35f),
                    Color(0xFFFF007F).copy(alpha = 0.15f),
                    Color.Transparent
                ),
                startY = paddingTop,
                endY = height - paddingBottom
            )
        )

        // Draw line stroke
        drawPath(
            path = linePath,
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF7F00FF), Color(0xFFFF007F), Color(0xFFFF8947))
            ),
            style = Stroke(
                width = 3.5.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Draw dots and value labels
        pathPoints.forEachIndexed { index, point ->
            // Glowing outer ring
            drawCircle(
                color = Color(0xFFFF007F),
                radius = 6.dp.toPx(),
                center = point
            )
            // Center white dot
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = point
            )

            // Value text above dot
            val valStr = String.format("%.2f", points[index].value)
            val textResult = textMeasurer.measure(
                text = valStr,
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E1E)
                )
            )
            drawText(
                textLayoutResult = textResult,
                topLeft = Offset(
                    x = (point.x - textResult.size.width / 2f).coerceIn(0f, width - textResult.size.width),
                    y = point.y - 18.dp.toPx()
                )
            )

            // X label below graph
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
fun StrideLengthCard(
    strideMeters: Float,
    heightCm: Float,
    gender: String,
    formulaStr: String,
    noiseLevel: Float = 0f,
    userProfile: com.example.data.UserProfile? = null
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF9D50BB), Color(0xFF6E48AA))),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DirectionsWalk,
                            contentDescription = "Stride Length",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format("%.2f", strideMeters),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp
                                ),
                                color = Color(0xFF1E1E1E)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "m",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF555555),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        Text(
                            text = "Today's Avg Stride Length",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                            color = Color.Gray
                        )
                    }
                }

                // Decorative Shoe Icon Badge
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color(0xFF8E24AA).copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DirectionsRun,
                        contentDescription = null,
                        tint = Color(0xFF8E24AA),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.Black.copy(alpha = 0.06f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // How it's calculated
            Text(
                text = "How it's calculated",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6A1B9A)
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Formula Chip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF6A1B9A).copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF6A1B9A).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    Text(
                        text = "Base Stride = Height × ${if (gender == "Female") "0.413" else "0.415"}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A148C)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dynamic Cadence Adjustments:\n" +
                                "• <80 spm: Stride × 0.95 (Slow)\n" +
                                "• 80–110 spm: Stride × 1.00 (Normal)\n" +
                                "• 110–130 spm: Stride × 1.05 (Fast)\n" +
                                "• >130 spm: Stride × 1.10 (Very Fast)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.5.sp,
                            color = Color(0xFF4A148C).copy(alpha = 0.85f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                        Text("Your Height", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("${heightCm.toInt()} cm", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1E1E1E))
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
                        Text("Gender", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(gender, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1E1E1E))
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    unit: String,
    badgeText: String,
    gradient: List<Color>,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Box(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.05f))
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(gradient))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

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

@Composable
fun AboutDistanceCard(noiseLevel: Float = 0f) {
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
                    text = "About Distance",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF1E1E1E)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Distance is calculated using your steps and estimated stride length. Results are estimates.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = Color.DarkGray
                )
            }
        }
    }
}
