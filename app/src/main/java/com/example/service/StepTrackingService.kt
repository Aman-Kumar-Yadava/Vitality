package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.R
import com.example.VitalityApplication
import com.example.data.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class StepTrackingService : Service(), TextToSpeech.OnInitListener {

    private val CHANNEL_ID = "StepTrackingServiceChannel"
    private val NOTIFICATION_ID = 1

    private lateinit var app: VitalityApplication
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private var notificationBuilder: NotificationCompat.Builder? = null
    private var currentUserProfile = UserProfile()

    private lateinit var tts: TextToSpeech
    private var isTtsInitialized = false
    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    
    private var lastAnnouncedSteps = 0
    private var lastAnnouncedDistanceKm = 0f
    private var lastAnnouncedCalories = 0f
    
    private var lastNotifiedSteps = -1
    private var lastNotifiedTime = 0L

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PASSIVE_START = "ACTION_PASSIVE_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"
    }

    override fun onCreate() {
        super.onCreate()
        app = application as VitalityApplication
        
        tts = TextToSpeech(this, this)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        serviceScope.launch {
            app.userPrefsRepository.userProfileFlow.collectLatest { profile ->
                currentUserProfile = profile
                updateTtsSettings()
            }
        }
        startService()
    }

    private fun startService() {
        createNotificationChannel()
        
        val notificationIntent = Intent(this, com.example.MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = Intent(this, StepTrackingService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent, PendingIntent.FLAG_IMMUTABLE
        )

        notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Step Tracker")
            .setContentText("Active in background")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .addAction(0, "Stop", stopPendingIntent)
            
        val notification = notificationBuilder!!.build()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceCompat.startForeground(
                this, 
                NOTIFICATION_ID, 
                notification, 
                ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        
        // Update notification UI regularly (for time display)
        serviceScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000L) // every second
                val steps = app.stepTrackerManager.currentSteps.value
                val distance = app.stepRepository.calculateDistance(steps, currentUserProfile)
                val calories = app.stepRepository.calculateCalories(distance, currentUserProfile)
                
                // Only update notification every 5 seconds OR if steps changed
                val now = System.currentTimeMillis()
                if (steps != lastNotifiedSteps || (now - lastNotifiedTime) > 5000L) {
                    updateNotification(steps, distance, calories)
                    lastNotifiedSteps = steps
                    lastNotifiedTime = now
                }
                
                checkMilestones(steps, distance, calories)
            }
        }
    }

    private fun updateNotification(steps: Int, distance: Float, calories: Float) {
        val builder = notificationBuilder ?: return
        
        // Calculate time
        val sessionStart = app.stepTrackerManager.lastStepTime
        var timeStr = "00:00"
        if (sessionStart > 0 && app.stepTrackerManager.isTracking.value) {
            val elapsed = System.currentTimeMillis() - sessionStart
            val seconds = (elapsed / 1000) % 60
            val minutes = (elapsed / (1000 * 60)) % 60
            val hours = (elapsed / (1000 * 60 * 60))
            if (hours > 0) {
                timeStr = String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                timeStr = String.format("%02d:%02d", minutes, seconds)
            }
        }
        
        val content = "Steps: $steps | Distance: ${String.format("%.2f", distance)} km | Calories: ${calories.toInt()} kcal"
        
        // Add progress bar if goal is set
        val goal = currentUserProfile.dailyStepGoal
        if (goal > 0) {
            builder.setProgress(goal, steps, false)
        }
        
        builder.setContentTitle(if (app.stepTrackerManager.isTracking.value) "Active Workout ($timeStr)" else "Step Tracker")
        builder.setContentText(content)
        builder.setStyle(NotificationCompat.BigTextStyle().bigText(content))
        
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun checkMilestones(steps: Int, distance: Float, calories: Float) {
        if (!app.stepTrackerManager.isTracking.value) return
        if (!isTtsInitialized || !currentUserProfile.announcementsEnabled) return
        
        // Check Milestones
        val stepsInterval = currentUserProfile.announceStepsInterval
        if (stepsInterval > 0 && steps - lastAnnouncedSteps >= stepsInterval) {
            val count = (steps / stepsInterval) * stepsInterval
            announce("You have reached $count steps!")
            lastAnnouncedSteps = count
        }
            
        val distanceInterval = currentUserProfile.announceDistanceIntervalKm
        if (distanceInterval > 0f && distance - lastAnnouncedDistanceKm >= distanceInterval) {
            val count = ((distance / distanceInterval).toInt()) * distanceInterval
            announce("You have travelled ${String.format("%.1f", count)} kilometers!")
            lastAnnouncedDistanceKm = count.toFloat()
        }
            
        val calInterval = currentUserProfile.announceCaloriesInterval
        if (calInterval > 0f && calories - lastAnnouncedCalories >= calInterval) {
            val count = ((calories / calInterval).toInt()) * calInterval
            announce("You have burned ${count.toInt()} calories!")
            lastAnnouncedCalories = count.toFloat()
        }
    }

    private fun updateTtsSettings() {
        if (!isTtsInitialized) return
        tts.setSpeechRate(currentUserProfile.speechRate)
        tts.setPitch(currentUserProfile.pitch)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_PASSIVE_START) {
            // Just keep the service alive for passive tracking
        } else if (intent == null || intent.action == ACTION_START) {
            app.stepTrackerManager.startTracking()
            if (isTtsInitialized && currentUserProfile.announcementsEnabled && intent?.action == ACTION_START) {
                announce("Workout started")
            }
        } else if (intent.action == ACTION_PAUSE) {
            app.stepTrackerManager.stopTracking()
            if (isTtsInitialized && currentUserProfile.announcementsEnabled) {
                announce("Workout paused")
            }
        } else if (intent.action == ACTION_RESUME) {
            app.stepTrackerManager.startTracking()
            if (isTtsInitialized && currentUserProfile.announcementsEnabled) {
                announce("Workout resumed")
            }
        } else if (intent.action == ACTION_STOP) {
            app.stepTrackerManager.stopTracking()
            if (isTtsInitialized && currentUserProfile.announcementsEnabled) {
                announce("Workout stopped")
            }
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
        return START_STICKY
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsInitialized = true
                updateTtsSettings()
            }
                
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                    
                override fun onDone(utteranceId: String?) {
                    abandonAudioFocus()
                }
                    
                override fun onError(utteranceId: String?) {
                    abandonAudioFocus()
                }
            })
        }
    }
        
    private fun requestAudioFocus(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
                    
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(audioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener { }
                .build()
                    
            val result = audioManager.requestAudioFocus(audioFocusRequest!!)
            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            val result = audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            )
            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }
        
    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let {
                audioManager.abandonAudioFocusRequest(it)
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }

    private fun announce(text: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val filter = notificationManager.currentInterruptionFilter
            if (filter == NotificationManager.INTERRUPTION_FILTER_NONE || filter == NotificationManager.INTERRUPTION_FILTER_ALARMS) {
                // Respect Do Not Disturb (Total Silence or Alarms Only)
                return
            }
        }
            
        if (requestAudioFocus()) {
            val params = android.os.Bundle()
            params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
            tts.speak(text, TextToSpeech.QUEUE_ADD, params, "announce_${System.currentTimeMillis()}")
        } else {
            // Even if we don't get focus, we can try to speak
            tts.speak(text, TextToSpeech.QUEUE_ADD, null, "announce_${System.currentTimeMillis()}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        if (isTtsInitialized) {
            tts.stop()
            tts.shutdown()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Step Tracking Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            serviceChannel.setSound(null, null)
            serviceChannel.setShowBadge(false)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}
