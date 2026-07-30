package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DailyStepRecord
import com.example.viewmodel.MainViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HistoryScreen(viewModel: MainViewModel) {
    val history by viewModel.allRecords.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    
    val totalSteps = history.sumOf { it.steps }
    val totalDistance = history.map { it.distanceKm }.sum()
    val totalCalories = history.map { it.caloriesBurned }.sum()

    val bgGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFD4F0FF),
            Color(0xFFFFDFE9),
            Color(0xFFE5E0FF),
            Color(0xFFFFE3D5)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    var selectedMetric by remember { mutableStateOf("Steps") } // "Steps", "Distance", "Calories"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .noiseOverlay(userProfile.uiNoiseLevel)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 44.dp, bottom = 120.dp)
        ) {
            item {
                Text(
                    text = "Activity Trends",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color(0xFF1E1E1E),
                    modifier = Modifier.padding(bottom = 24.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            
            item {
                BuffedLifetimeStatsCard(totalSteps, totalDistance, totalCalories)
                Spacer(modifier = Modifier.height(28.dp))
            }
            
            item {
                MetricGraphCard(
                    history = history,
                    selectedMetric = selectedMetric,
                    onMetricSelected = { selectedMetric = it }
                )
                Spacer(modifier = Modifier.height(28.dp))
            }

            if (history.isNotEmpty()) {
                item {
                    Text(
                        text = "Past Records",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                        textAlign = TextAlign.Start,
                        color = Color(0xFF1E1E1E)
                    )
                }
                
                items(history.size) { index ->
                    HistoryItemCard(history[index])
                    Spacer(modifier = Modifier.height(12.dp))
                }
            } else {
                item {
                    Text(
                        text = "No history available yet. Start walking!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(32.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun BuffedLifetimeStatsCard(totalSteps: Int, totalDistance: Float, totalCalories: Float) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF2C1466),
                            Color(0xFF4A00E0),
                            Color(0xFF8E2DE2)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Timeline,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LIFETIME ACHIEVEMENTS",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = Color.White.copy(alpha = 0.95f)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
                
                // 3 Stat Grid Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LifetimeStatTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.DirectionsWalk,
                        value = String.format("%,d", totalSteps),
                        label = "Total Steps",
                        accentColor = Color(0xFF00E676)
                    )
                    LifetimeStatTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.AutoMirrored.Rounded.DirectionsRun,
                        value = String.format("%.1f km", totalDistance),
                        label = "Distance",
                        accentColor = Color(0xFF00B0FF)
                    )
                    LifetimeStatTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.LocalFireDepartment,
                        value = String.format("%.0f kcal", totalCalories),
                        label = "Calories",
                        accentColor = Color(0xFFFF3D00)
                    )
                }
            }
        }
    }
}

@Composable
private fun LifetimeStatTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    accentColor: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(accentColor.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = Color.White,
                fontSize = 15.sp,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp
            )
        }
    }
}

data class GraphBarItem(
    val mainLabel: String,
    val subLabel: String,
    val steps: Float,
    val distanceKm: Float,
    val caloriesBurned: Float
)

@Composable
fun MetricGraphCard(
    history: List<DailyStepRecord>,
    selectedMetric: String,
    onMetricSelected: (String) -> Unit
) {
    var selectedTimeframe by remember { mutableStateOf("Weekly") } // "Weekly", "Monthly", "Yearly"

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
            Text(
                text = "Graph Trends",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF1E1E1E)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Timeframe Selector Row (Weekly, Monthly, Yearly) - Horizontal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Weekly", "Monthly", "Yearly").forEach { tf ->
                    val isSel = selectedTimeframe.equals(tf, ignoreCase = true)
                    Surface(
                        onClick = { selectedTimeframe = tf },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSel) Color(0xFF2C1466) else Color.Black.copy(alpha = 0.05f),
                        contentColor = if (isSel) Color.White else Color.DarkGray,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
                        ) {
                            Text(
                                text = tf,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Metric Selector Row (Steps, Distance, Calories) - Horizontal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Steps", "Distance", "Calories").forEach { metric ->
                    val isSel = selectedMetric.equals(metric, ignoreCase = true)
                    Surface(
                        onClick = { onMetricSelected(metric) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSel) Color(0xFF7F00FF) else Color.Black.copy(alpha = 0.05f),
                        contentColor = if (isSel) Color.White else Color.DarkGray,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
                        ) {
                            Text(
                                text = metric,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Compute bars data based on history and selectedTimeframe
            val barItems = remember(history, selectedTimeframe) {
                buildGraphData(history, selectedTimeframe)
            }

            val maxVal = when (selectedMetric) {
                "Distance" -> (barItems.maxOfOrNull { it.distanceKm } ?: 5f).coerceAtLeast(1f)
                "Calories" -> (barItems.maxOfOrNull { it.caloriesBurned } ?: 500f).coerceAtLeast(100f)
                else -> (barItems.maxOfOrNull { it.steps } ?: 10000f).coerceAtLeast(100f)
            }

            val barGradients = when (selectedMetric) {
                "Distance" -> listOf(Color(0xFF00B0FF), Color(0xFF00E5FF))
                "Calories" -> listOf(Color(0xFFFF3D00), Color(0xFFFF9100))
                else -> listOf(Color(0xFF7F00FF), Color(0xFFE100FF))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                barItems.forEach { item ->
                    val rawVal = when (selectedMetric) {
                        "Distance" -> item.distanceKm
                        "Calories" -> item.caloriesBurned
                        else -> item.steps
                    }

                    val ratio = (rawVal / maxVal).coerceIn(0.08f, 1f)
                    val animRatio by animateFloatAsState(
                        targetValue = ratio,
                        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                        label = "BarHeight"
                    )

                    val valDisplay = when (selectedMetric) {
                        "Distance" -> String.format("%.1f", item.distanceKm)
                        "Calories" -> String.format("%.0f", item.caloriesBurned)
                        else -> if (item.steps >= 1000) String.format("%.1fk", item.steps / 1000f) else "${item.steps.toInt()}"
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // Value label on top of bar
                        Text(
                            text = valDisplay,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF1E1E1E),
                            fontSize = if (barItems.size > 8) 8.sp else 10.sp,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Animated Bar
                        Box(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .fillMaxHeight(animRatio)
                                .width(if (barItems.size > 8) 14.dp else 22.dp)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(Brush.verticalGradient(barGradients))
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Label & sublabel
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = item.mainLabel,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF1E1E1E),
                                fontSize = if (barItems.size > 8) 9.sp else 11.sp,
                                maxLines = 1
                            )
                            if (item.subLabel.isNotBlank() && barItems.size <= 8) {
                                Text(
                                    text = item.subLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray,
                                    fontSize = 8.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun buildGraphData(history: List<DailyStepRecord>, timeframe: String): List<GraphBarItem> {
    val today = LocalDate.now()
    return when (timeframe) {
        "Monthly" -> {
            // Past 4 weeks
            (3 downTo 0).map { weekOffset ->
                val endDay = today.minusDays((weekOffset * 7).toLong())
                val startDay = endDay.minusDays(6)
                
                val matching = history.filter { rec ->
                    try {
                        val parts = rec.dateString.split("-")
                        if (parts.size == 3) {
                            val d = LocalDate.of(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
                            !d.isBefore(startDay) && !d.isAfter(endDay)
                        } else false
                    } catch (e: Exception) { false }
                }
                
                val weekNum = 4 - weekOffset
                val startStr = "${startDay.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)} ${startDay.dayOfMonth}"
                val endStr = "${endDay.dayOfMonth}"
                
                GraphBarItem(
                    mainLabel = "W$weekNum",
                    subLabel = "$startStr-$endStr",
                    steps = matching.sumOf { it.steps }.toFloat(),
                    distanceKm = matching.fold(0f) { acc, r -> acc + r.distanceKm },
                    caloriesBurned = matching.fold(0f) { acc, r -> acc + r.caloriesBurned }
                )
            }
        }
        "Yearly" -> {
            // Past 12 months
            (11 downTo 0).map { monthOffset ->
                val targetMonthDate = today.minusMonths(monthOffset.toLong())
                val targetYear = targetMonthDate.year
                val targetMonthVal = targetMonthDate.monthValue
                
                val matching = history.filter { rec ->
                    try {
                        val parts = rec.dateString.split("-")
                        if (parts.size == 3) {
                            parts[0].toInt() == targetYear && parts[1].toInt() == targetMonthVal
                        } else false
                    } catch (e: Exception) { false }
                }
                
                val monthName = targetMonthDate.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                GraphBarItem(
                    mainLabel = monthName,
                    subLabel = targetYear.toString().takeLast(2),
                    steps = matching.sumOf { it.steps }.toFloat(),
                    distanceKm = matching.fold(0f) { acc, r -> acc + r.distanceKm },
                    caloriesBurned = matching.fold(0f) { acc, r -> acc + r.caloriesBurned }
                )
            }
        }
        else -> { // "Weekly"
            val last7 = history.take(7).reversed()
            if (last7.isNotEmpty()) {
                last7.map { record ->
                    val (dayOfWeek, dateLabel) = formatGraphDate(record.dateString)
                    GraphBarItem(
                        mainLabel = dayOfWeek.ifBlank { dateLabel },
                        subLabel = dateLabel,
                        steps = record.steps.toFloat(),
                        distanceKm = record.distanceKm,
                        caloriesBurned = record.caloriesBurned
                    )
                }
            } else {
                (6 downTo 0).map { dayOffset ->
                    val date = today.minusDays(dayOffset.toLong())
                    val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                    val monthDay = "${date.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)} ${date.dayOfMonth}"
                    GraphBarItem(
                        mainLabel = dayOfWeek,
                        subLabel = monthDay,
                        steps = 0f,
                        distanceKm = 0f,
                        caloriesBurned = 0f
                    )
                }
            }
        }
    }
}

private fun formatGraphDate(dateStr: String): Pair<String, String> {
    return try {
        val parts = dateStr.split("-")
        if (parts.size == 3) {
            val yr = parts[0].toInt()
            val mo = parts[1].toInt()
            val dy = parts[2].toInt()
            val localDate = LocalDate.of(yr, mo, dy)
            val dayOfWeek = localDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
            val monthName = localDate.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
            Pair(dayOfWeek, "$monthName $dy")
        } else {
            Pair("", dateStr)
        }
    } catch (e: Exception) {
        Pair("", dateStr)
    }
}

@Composable
fun HistoryItemCard(record: DailyStepRecord) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = record.dateString,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF1E1E1E)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        text = "${String.format("%.1f", record.distanceKm)} km",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${String.format("%.0f", record.caloriesBurned)} kcal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                }
            }
            
            Text(
                text = "${record.steps}",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = Color(0xFF7F00FF)
            )
        }
    }
}
