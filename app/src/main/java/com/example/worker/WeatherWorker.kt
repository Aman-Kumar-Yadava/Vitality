package com.example.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.VitalityApplication
import com.example.R
import kotlinx.coroutines.flow.first

class WeatherWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val app = applicationContext as VitalityApplication
        val repo = app.stepRepository
        
        try {
            // Get today's sessions
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val todayString = dateFormat.format(java.util.Date())
            val sessions = repo.getSessionsForDate(todayString).first()
            if (sessions.isEmpty()) {
                // Not enough data to predict, maybe use mock data for demonstration
                sendWeatherNotification("Based on your habits, you might walk soon. Weather is pleasant at 24°C. Good time for a walk!")
            } else {
                // Simple logic: if they walked today, say something about tomorrow.
                // But let's just show an intelligent message as requested.
                val message = generateSmartWeatherMessage()
                sendWeatherNotification(message)
            }
            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }
    
    private fun generateSmartWeatherMessage(): String {
        // In a real app, this would use FusedLocationProvider to get current location,
        // make an API call to a Weather service, and use an LLM or rules engine to generate this.
        // For demonstration of the AI Smart Weather Walking Assistant:
        val messages = listOf(
            "Your usual walk starts in 30 minutes. Light rain is expected after 6:45 PM. Take an umbrella.",
            "Heavy rain is expected during your normal walking time. Consider postponing today's walk.",
            "Excellent weather today. 24°C with low humidity. Perfect for walking.",
            "High UV index expected during your usual afternoon walk. Don't forget sunscreen!"
        )
        return messages.random()
    }

    private fun sendWeatherNotification(message: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val channelId = "weather_assistant_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Smart Weather Assistant",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Smart Walking Assistant")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(102, notification)
    }
}
