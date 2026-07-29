package com.example.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.appwidget.cornerRadius
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HealthWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            androidx.compose.ui.unit.DpSize(110.dp, 110.dp), // Small 2x2
            androidx.compose.ui.unit.DpSize(250.dp, 110.dp), // Wide 4x2
            androidx.compose.ui.unit.DpSize(250.dp, 250.dp)  // Large 4x4
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = AppDatabase.getDatabase(context)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val record = db.stepDao().getRecordForDateSync(today)
        val steps = record?.steps ?: 0
        val calories = record?.caloriesBurned ?: 0f
        val distance = record?.distanceKm ?: 0f

        provideContent {
            GlanceTheme {
                val size = LocalSize.current
                val isSmall = size.width < 200.dp && size.height < 200.dp
                val isWide = size.width >= 200.dp && size.height < 200.dp

                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.surface)
                        .cornerRadius(24.dp)
                        .padding(16.dp)
                        .clickable(actionStartActivity<MainActivity>()),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSmall) {
                        SmallWidgetContent(steps)
                    } else if (isWide) {
                        WideWidgetContent(steps, calories, distance)
                    } else {
                        LargeWidgetContent(steps, calories, distance)
                    }
                }
            }
        }
    }
}

@Composable
fun SmallWidgetContent(steps: Int) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "STEPS",
            style = TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(
            text = "$steps",
            style = TextStyle(
                color = GlanceTheme.colors.primary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
fun WideWidgetContent(steps: Int, calories: Float, distance: Float) {
    Row(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = GlanceModifier.defaultWeight(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Today's Steps",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                text = "$steps",
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Column(
            modifier = GlanceModifier.defaultWeight(),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "${String.format("%.0f", calories)} kcal",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = "${String.format("%.2f", distance)} km",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
fun LargeWidgetContent(steps: Int, calories: Float, distance: Float) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Activity Dashboard",
            style = TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = GlanceModifier.height(16.dp))
        Text(
            text = "$steps Steps",
            style = TextStyle(
                color = GlanceTheme.colors.primary,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = GlanceModifier.height(24.dp))
        Row(
            modifier = GlanceModifier.fillMaxWidth()
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = "Calories",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                )
                Text(
                    text = String.format("%.0f kcal", calories),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = "Distance",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                )
                Text(
                    text = String.format("%.2f km", distance),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
        Spacer(modifier = GlanceModifier.defaultWeight())
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                text = "Log Water",
                onClick = actionStartActivity<MainActivity>(),
                modifier = GlanceModifier.defaultWeight().padding(end = 4.dp)
            )
            Button(
                text = "Add Meal",
                onClick = actionStartActivity<MainActivity>(),
                modifier = GlanceModifier.defaultWeight().padding(start = 4.dp, end = 4.dp)
            )
            Button(
                text = "Workout",
                onClick = actionStartActivity<MainActivity>(),
                modifier = GlanceModifier.defaultWeight().padding(start = 4.dp)
            )
        }
    }
}
