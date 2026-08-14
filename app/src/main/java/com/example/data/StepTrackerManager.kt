package com.example.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StepTrackerManager(
    private val context: Context,
    private val repository: StepRepository
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val stepDetectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    
    private val _currentSteps = MutableStateFlow(0)
    val currentSteps: StateFlow<Int> = _currentSteps
    
    // @Volatile ensures thread safety between the background DB coroutines and UI thread
    @Volatile private var initialStepCount = -1f
    @Volatile private var lastSavedSteps = 0
    @Volatile private var lastDbSaveSteps = 0
    @Volatile private var isInitialized = false
    @Volatile private var isDatabaseLoading = false
    @Volatile private var latestTotalSteps = -1f
    
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    private val prefs = context.getSharedPreferences("step_tracker_prefs", Context.MODE_PRIVATE)
    
    // Initialize trackingDate from prefs so skipped days are handled correctly
    private var trackingDate = prefs.getString("last_sensor_date", dateFormat.format(Date())) ?: dateFormat.format(Date())
    
    private var currentSessionStartTime = 0L
    private var sessionStartTotalSteps = 0
    var lastStepTime = 0L
        private set

    init {
        coroutineScope.launch {
            val today = getCurrentDateString()
            val record = repository.getRecordForDateSync(today)
            _currentSteps.value = record?.steps ?: 0
        }
        initializePassiveTracking()
        startDateCheckLoop()
    }

    private fun startDateCheckLoop() {
        coroutineScope.launch {
            while (isActive) {
                val today = getCurrentDateString()
                if (trackingDate != today) {
                    trackingDate = today
                    isInitialized = false // Force clean rollover on next sensor event
                }
                kotlinx.coroutines.delay(60_000L) // Check every minute
            }
        }
    }

    private fun initializePassiveTracking() {
        stepSensor?.let {
            try {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        stepDetectorSensor?.let {
            try {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun reRegisterSensor() {
        try {
            sensorManager.unregisterListener(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        stepSensor?.let {
            try {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        stepDetectorSensor?.let {
            try {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking

    fun startTracking() {
        if (_isTracking.value) return
        _isTracking.value = true
        currentSessionStartTime = System.currentTimeMillis()
        sessionStartTotalSteps = _currentSteps.value
    }

    fun stopTracking() {
        if (!_isTracking.value) return
        _isTracking.value = false
        saveCurrentSession()
    }

    // FIX 1: Use Pair to track BOTH timestamp AND actual step count to defeat sensor batching
    private val recentStepData = ArrayDeque<Pair<Long, Float>>()
    var currentCadence = 100f
        private set

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
            if (isInitialized && initialStepCount != -1f) {
                _currentSteps.value += 1
            }
            return
        }
        
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSteps = event.values[0]
            val now = System.currentTimeMillis()
            
            // FIX 1 CONTINUED: Accurate Cadence Tracking based on delta steps, not delta events
            recentStepData.addLast(Pair(now, totalSteps))
            if (recentStepData.size > 5) {
                recentStepData.removeFirst() // Keep last 5 batches
            }
            if (recentStepData.size >= 2) {
                val oldest = recentStepData.first()
                val diffMs = now - oldest.first
                val diffSteps = totalSteps - oldest.second
                if (diffMs > 2000L && diffMs < 120000L && diffSteps > 0) {
                    val calculatedCadence = (diffSteps * 60000f) / diffMs
                    currentCadence = calculatedCadence.coerceIn(50f, 180f)
                }
            }
            
            latestTotalSteps = totalSteps
            val today = getCurrentDateString()

            // 1. MID-SESSION HARDWARE RESET GUARD
            // If the sensor daemon crashes and resets to 0 while the app is alive, instantly reset our baselines.
            val lastKnownSensorSteps = prefs.getFloat("last_sensor_steps", -1f)
            if (lastKnownSensorSteps != -1f && totalSteps < lastKnownSensorSteps) {
                initialStepCount = totalSteps
                lastSavedSteps = _currentSteps.value
                lastDbSaveSteps = _currentSteps.value
            }

            // 2. SYNCHRONOUS MIDNIGHT INTERCEPT
            // Instantly detect a new day on the main thread before ANY math happens
            if (trackingDate != today) {
                trackingDate = today
                isInitialized = false
            }

            if (!isInitialized) {
                isInitialized = true
                isDatabaseLoading = true
                initialStepCount = totalSteps
                
                coroutineScope.launch {
                    try {
                        val record = repository.getRecordForDateSync(today)
                        var currentDbSteps = record?.steps ?: 0
                        
                        val lastSensorSteps = prefs.getFloat("last_sensor_steps", -1f)
                        val lastSensorDate = prefs.getString("last_sensor_date", today) ?: today
                        
                        // FIX 2: Hardware Reboot & Skipped Day Logic
                        if (lastSensorSteps != -1f) {
                            val missedSteps = if (totalSteps > lastSensorSteps) {
                                (totalSteps - lastSensorSteps).toInt()
                            } else if (totalSteps < lastSensorSteps - 50) {
                                // REBOOT DETECTED: Hardware counter reset. Count new total as missed steps.
                                totalSteps.toInt()
                            } else { 0 }
                            
                            if (missedSteps > 0) {
                                if (lastSensorDate == today) {
                                    currentDbSteps += missedSteps
                                    repository.updateStepsForDate(today, currentDbSteps, currentCadence)
                                } else {
                                    val pastRecord = repository.getRecordForDateSync(lastSensorDate)
                                    val pastDbSteps = pastRecord?.steps ?: 0
                                    repository.updateStepsForDate(lastSensorDate, pastDbSteps + missedSteps, currentCadence)
                                }
                            }
                        }
                        
                        lastSavedSteps = currentDbSteps
                        lastDbSaveSteps = currentDbSteps
                        
                        // FIX 3: Immediately save prefs BEFORE coroutine finishes to stop the "1000s" loop
                        prefs.edit()
                            .putString("last_sensor_date", today)
                            .putFloat("last_sensor_steps", totalSteps)
                            .apply()
                            
                        val newSteps = (latestTotalSteps - initialStepCount).toInt().coerceAtLeast(0)
                        val totalToday = lastSavedSteps + newSteps
                        
                        if (totalToday > _currentSteps.value) {
                            _currentSteps.value = totalToday
                        }
                        
                        if (totalToday - lastDbSaveSteps >= 10 || totalToday < lastDbSaveSteps) {
                            lastDbSaveSteps = totalToday
                            saveStepsToDb(totalToday)
                            updateWidget()
                        }
                    } finally {
                        isDatabaseLoading = false
                    }
                }
                return
            }

            if (isDatabaseLoading) return
            
            val newSteps = (totalSteps - initialStepCount).toInt().coerceAtLeast(0)
            val totalToday = lastSavedSteps + newSteps
            
            if (totalToday > _currentSteps.value) {
                _currentSteps.value = totalToday
            } else if (totalToday < _currentSteps.value - 20) {
                _currentSteps.value = totalToday
            }
            
            if (totalToday - lastDbSaveSteps >= 10 || totalToday < lastDbSaveSteps) {
                lastDbSaveSteps = totalToday
                saveStepsToDb(totalToday)
                updateWidget()
            }
            
            prefs.edit()
                .putString("last_sensor_date", today)
                .putFloat("last_sensor_steps", totalSteps)
                .apply()
                
            lastStepTime = now
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun saveCurrentSession() {
        val now = System.currentTimeMillis()
        val startTime = currentSessionStartTime
        if (startTime != 0L) {
            val endTime = if (lastStepTime > startTime) lastStepTime else now
            val steps = (_currentSteps.value - sessionStartTotalSteps).coerceAtLeast(0)
            val durationMs = (endTime - startTime).coerceAtLeast(0)
            
            if (durationMs >= 1000L || steps > 0) {
                coroutineScope.launch {
                    val profile = repository.userPreferencesRepository.userProfileFlow.first()
                    val distance = repository.calculateDistance(steps, profile, currentCadence)
                    repository.addSession(WalkingSession(
                        dateString = getCurrentDateString(),
                        startTimeMs = startTime,
                        endTimeMs = endTime,
                        steps = steps,
                        distanceKm = distance
                    ))
                }
            }
        }
        currentSessionStartTime = 0L
    }

    private fun saveStepsToDb(steps: Int) {
        val today = getCurrentDateString()
        coroutineScope.launch {
            repository.updateStepsForDate(today, steps, currentCadence)
        }
    }
    
    private fun updateWidget() {
        coroutineScope.launch {
            try {
                com.example.widget.HealthWidget().updateAll(context)
                com.example.widget.TransparentHealthWidget().updateAll(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun addMockSteps(steps: Int) {
        coroutineScope.launch {
            val today = getCurrentDateString()
            val record = repository.getRecordForDateSync(today)
            val current = record?.steps ?: 0
            val newTotal = current + steps
            repository.updateStepsForDate(today, newTotal)
            
            val endTime = System.currentTimeMillis()
            val startTime = endTime - (steps * 1000L)
            val profile = repository.userPreferencesRepository.userProfileFlow.first()
            val distance = repository.calculateDistance(steps, profile)
            repository.addSession(WalkingSession(
                dateString = today,
                startTimeMs = startTime,
                endTimeMs = endTime,
                steps = steps,
                distanceKm = distance
            ))
            
            _currentSteps.value = newTotal
            lastSavedSteps = newTotal
            lastDbSaveSteps = newTotal
            initialStepCount = -1f
            isInitialized = false
            
            updateWidget()
        }
    }

    private fun getCurrentDateString(): String {
        return dateFormat.format(Date())
    }

    fun setCustomActivity(steps: Int, distance: Float, calories: Float) {
        coroutineScope.launch {
            val today = getCurrentDateString()
            repository.updateCustomDataForDate(today, steps, distance, calories)
            
            _currentSteps.value = steps
            lastSavedSteps = steps
            lastDbSaveSteps = steps
            initialStepCount = -1f
            isInitialized = false
            
            updateWidget()
        }
    }
}
