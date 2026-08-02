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

    private var initialStepCount = -1f
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private var trackingDate = dateFormat.format(Date())
    private var lastSavedSteps = 0
    private var lastDbSaveSteps = 0
    private var isInitialized = false
    private var isDatabaseLoading = false
    private var latestTotalSteps = -1f

    private val prefs = context.getSharedPreferences("step_tracker_prefs", Context.MODE_PRIVATE)

    private var currentSessionStartTime = 0L
    private var sessionStartTotalSteps = 0
    var lastStepTime = 0L
        private set

    init {
        // Load initial steps for today
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
                    isInitialized = false
                    val record = repository.getRecordForDateSync(today)
                    val dbSteps = record?.steps ?: 0
                    lastSavedSteps = dbSteps
                    lastDbSaveSteps = dbSteps
                    _currentSteps.value = dbSteps
                    
                    updateWidget()
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

    private val recentStepTimes = ArrayDeque<Long>()
    var currentCadence = 100f
        private set

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
            // Instant feedback for UI jump bug
            if (isInitialized && initialStepCount != -1f) {
                _currentSteps.value += 1
            }
            return
        }

        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSteps = event.values[0]
            val now = System.currentTimeMillis()
            
            // Track timestamp for cadence calculation
            recentStepTimes.addLast(now)
            if (recentStepTimes.size > 20) {
                recentStepTimes.removeFirst()
            }
            if (recentStepTimes.size >= 3) {
                val oldest = recentStepTimes.first()
                val diffMs = now - oldest
                if (diffMs > 2000L && diffMs < 120000L) {
                    val stepsInWindow = recentStepTimes.size - 1
                    val calculatedCadence = (stepsInWindow * 60000f) / diffMs
                    currentCadence = calculatedCadence.coerceIn(50f, 180f)
                }
            }
            
            latestTotalSteps = totalSteps
            val today = getCurrentDateString()
            if (trackingDate != today) {
                // Day changed, check if we missed steps before resetting
                val lastSensorSteps = prefs.getFloat("last_sensor_steps", -1f)
                if (lastSensorSteps != -1f && totalSteps > lastSensorSteps) {
                    val missedSteps = (totalSteps - lastSensorSteps).toInt()
                    val yesterday = trackingDate
                    coroutineScope.launch {
                        val record = repository.getRecordForDateSync(yesterday)
                        val prevDbSteps = record?.steps ?: 0
                        repository.updateStepsForDate(yesterday, prevDbSteps + missedSteps, currentCadence)
                    }
                    prefs.edit().putFloat("last_sensor_steps", totalSteps).apply()
                }

                // Reset the tracking logic for the new day
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
                        
                        // Recover missed steps if the service was killed or day rolled over
                        val lastSensorSteps = prefs.getFloat("last_sensor_steps", -1f)
                        
                        if (lastSensorSteps != -1f && totalSteps > lastSensorSteps) {
                            val missedSteps = (totalSteps - lastSensorSteps).toInt()
                            currentDbSteps += missedSteps
                            repository.updateStepsForDate(today, currentDbSteps, currentCadence)
                        }
                        
                        lastSavedSteps = currentDbSteps
                        lastDbSaveSteps = currentDbSteps
                        
                        val newSteps = (latestTotalSteps - initialStepCount).toInt()
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
            
            val newSteps = (totalSteps - initialStepCount).toInt()
            val totalToday = lastSavedSteps + newSteps
            
            // Sync with detector's optimistic counting
            if (totalToday > _currentSteps.value) {
                _currentSteps.value = totalToday
            } else if (totalToday < _currentSteps.value - 20) { 
                // If detector overcounted significantly, reset to counter truth
                _currentSteps.value = totalToday
            }
            
            // Only save to DB and update widget periodically to avoid UI thread spam
            // e.g. every 10 steps
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
            
            // Save session if duration is at least 1 second or steps > 0
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
            val startTime = endTime - (steps * 1000L) // fake duration
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
