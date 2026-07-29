package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DailyStepRecord
import com.example.viewmodel.MainViewModel

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .noiseOverlay(userProfile.uiNoiseLevel)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 48.dp, bottom = 120.dp)
        ) {
            item {
                Text(
                    text = "Your Trends",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color(0xFF1E1E1E),
                    modifier = Modifier.padding(bottom = 32.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            
            item {
                LifetimeStatsCard(totalSteps, totalDistance, totalCalories)
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            if (history.isNotEmpty()) {
                val last7Days = history.take(7).reversed()
                item {
                    BarChart(last7Days)
                    Spacer(modifier = Modifier.height(32.dp))
                }
                
                item {
                    Text(
                        text = "Past Records",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
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
fun LifetimeStatsCard(totalSteps: Int, totalDistance: Float, totalCalories: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF4CA1AF), Color(0xFFC4E0E5))
                    )
                )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Lifetime Activity",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LifetimeStatColumn("$totalSteps", "Steps")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "${String.format("%.1f", totalDistance)} km",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "${String.format("%.0f", totalCalories)} kcal",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LifetimeStatColumn(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

@Composable
fun BarChart(records: List<DailyStepRecord>) {
    val maxSteps = (records.maxOfOrNull { it.steps } ?: 10000).coerceAtLeast(100)
    val gridColor = Color.LightGray.copy(alpha = 0.5f)
    
    val barGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF7F00FF), Color(0xFFFF007F))
    )
    
    GlassCard(modifier = Modifier.fillMaxWidth().height(280.dp)) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Text(
                text = "Past 7 Days",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E1E),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 24.dp, top = 16.dp)) {
                    val barWidth = size.width / (records.size * 2)
                    val spacing = barWidth
                    
                    // Draw horizontal grid lines
                    val stepsCount = 4
                    for (i in 0..stepsCount) {
                        val y = size.height - (size.height * i / stepsCount)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    
                    records.forEachIndexed { index, record ->
                        val x = index * (barWidth + spacing) + spacing / 2
                        val heightRatio = record.steps.toFloat() / maxSteps.toFloat()
                        val barHeight = size.height * heightRatio
                        val y = size.height - barHeight
                        
                        drawRoundRect(
                            brush = barGradient,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    records.forEach { record ->
                        val dateParts = record.dateString.split("-")
                        val dayStr = if (dateParts.size == 3) dateParts[2] else record.dateString.takeLast(2)
                        
                        Text(
                            text = dayStr,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.DarkGray,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
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
