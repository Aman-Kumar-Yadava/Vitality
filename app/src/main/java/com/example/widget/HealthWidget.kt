package com.example.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
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
import com.example.data.dataStore
import kotlinx.coroutines.flow.first
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
        val prefs = com.example.data.UserPreferencesRepository(context.dataStore)
        val profile = prefs.userProfileFlow.first()
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
                        .background(ImageProvider(R.drawable.widget_gradient_bg))
                        .cornerRadius(24.dp)
                        .padding(12.dp)
                        .clickable(actionStartActivity<MainActivity>()),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSmall) {
                        SmallWidgetContent(steps, calories, distance)
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
fun SmallWidgetContent(steps: Int, calories: Float, distance: Float) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Steps Row with Icon
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_widget_steps),
                contentDescription = "Steps",
                modifier = GlanceModifier.size(16.dp)
            )
            Spacer(modifier = GlanceModifier.width(4.dp))
            Text(
                text = String.format("%,d", steps),
                style = TextStyle(
                    color = ColorProvider(day = androidx.compose.ui.graphics.Color.White, night = androidx.compose.ui.graphics.Color.White),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        
        Spacer(modifier = GlanceModifier.height(6.dp))
        
        // Calories & Distance Row with Icons instead of words for short dimension
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    provider = ImageProvider(R.drawable.ic_widget_calories),
                    contentDescription = "Calories",
                    modifier = GlanceModifier.size(13.dp)
                )
                Spacer(modifier = GlanceModifier.width(2.dp))
                Text(
                    text = String.format("%.0f", calories),
                    style = TextStyle(
                        color = ColorProvider(day = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f), night = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f)),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            
            Spacer(modifier = GlanceModifier.width(6.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    provider = ImageProvider(R.drawable.ic_widget_distance),
                    contentDescription = "Distance",
                    modifier = GlanceModifier.size(13.dp)
                )
                Spacer(modifier = GlanceModifier.width(2.dp))
                Text(
                    text = String.format("%.1fkm", distance),
                    style = TextStyle(
                        color = ColorProvider(day = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f), night = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f)),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    provider = ImageProvider(R.drawable.ic_widget_steps),
                    contentDescription = "Steps",
                    modifier = GlanceModifier.size(18.dp)
                )
                Spacer(modifier = GlanceModifier.width(6.dp))
                Text(
                    text = "Steps",
                    style = TextStyle(
                        color = ColorProvider(day = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f), night = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f)),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = String.format("%,d", steps),
                style = TextStyle(
                    color = ColorProvider(day = androidx.compose.ui.graphics.Color.White, night = androidx.compose.ui.graphics.Color.White),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Column(
            modifier = GlanceModifier.defaultWeight(),
            horizontalAlignment = Alignment.End
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    provider = ImageProvider(R.drawable.ic_widget_calories),
                    contentDescription = "Calories",
                    modifier = GlanceModifier.size(15.dp)
                )
                Spacer(modifier = GlanceModifier.width(4.dp))
                Text(
                    text = String.format("%.0f kcal", calories),
                    style = TextStyle(
                        color = ColorProvider(day = androidx.compose.ui.graphics.Color.White, night = androidx.compose.ui.graphics.Color.White),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            Spacer(modifier = GlanceModifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    provider = ImageProvider(R.drawable.ic_widget_distance),
                    contentDescription = "Distance",
                    modifier = GlanceModifier.size(15.dp)
                )
                Spacer(modifier = GlanceModifier.width(4.dp))
                Text(
                    text = String.format("%.2f km", distance),
                    style = TextStyle(
                        color = ColorProvider(day = androidx.compose.ui.graphics.Color.White, night = androidx.compose.ui.graphics.Color.White),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
fun LargeWidgetContent(steps: Int, calories: Float, distance: Float) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                provider = ImageProvider(R.drawable.ic_widget_steps),
                contentDescription = "Activity",
                modifier = GlanceModifier.size(20.dp)
            )
            Spacer(modifier = GlanceModifier.width(6.dp))
            Text(
                text = "Today's Activity",
                style = TextStyle(
                    color = ColorProvider(day = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f), night = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f)),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Spacer(modifier = GlanceModifier.height(16.dp))
        Text(
            text = String.format("%,d", steps),
            style = TextStyle(
                color = ColorProvider(day = androidx.compose.ui.graphics.Color.White, night = androidx.compose.ui.graphics.Color.White),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = "steps taken today",
            style = TextStyle(
                color = ColorProvider(day = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f), night = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)),
                fontSize = 13.sp
            )
        )
        Spacer(modifier = GlanceModifier.height(24.dp))
        Row(
            modifier = GlanceModifier.fillMaxWidth()
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_widget_calories),
                        contentDescription = "Calories",
                        modifier = GlanceModifier.size(16.dp)
                    )
                    Spacer(modifier = GlanceModifier.width(4.dp))
                    Text(
                        text = "Calories",
                        style = TextStyle(
                            color = ColorProvider(day = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f), night = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)),
                            fontSize = 13.sp
                        )
                    )
                }
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = String.format("%.0f kcal", calories),
                    style = TextStyle(
                        color = ColorProvider(day = androidx.compose.ui.graphics.Color.White, night = androidx.compose.ui.graphics.Color.White),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            Column(modifier = GlanceModifier.defaultWeight()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_widget_distance),
                        contentDescription = "Distance",
                        modifier = GlanceModifier.size(16.dp)
                    )
                    Spacer(modifier = GlanceModifier.width(4.dp))
                    Text(
                        text = "Distance",
                        style = TextStyle(
                            color = ColorProvider(day = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f), night = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)),
                            fontSize = 13.sp
                        )
                    )
                }
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = String.format("%.2f km", distance),
                    style = TextStyle(
                        color = ColorProvider(day = androidx.compose.ui.graphics.Color.White, night = androidx.compose.ui.graphics.Color.White),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
        Spacer(modifier = GlanceModifier.defaultWeight())
    }
}
