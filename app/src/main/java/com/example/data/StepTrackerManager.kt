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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StepTrackerManager(
    private val context: Context,
    private val repository: StepRepository
) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    
    private val _currentSteps = MutableStateFlow(0)
    val currentSteps: StateFlow<Int> = _currentSteps

    private var initialStepCount = -1f
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private var lastSavedSteps = 0

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
    }

    private fun initializePassiveTracking() {
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
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

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSteps = event.values[0]
            val now = System.currentTimeMillis()
            
            if (initialStepCount == -1f) {
                initialStepCount = totalSteps
                coroutineScope.launch {
                   val today = getCurrentDateString()
                   val record = repository.getRecordForDateSync(today)
                   lastSavedSteps = record?.steps ?: 0
                   _currentSteps.value = lastSavedSteps
                }
            } else {
                val newSteps = (totalSteps - initialStepCount).toInt()
                val totalToday = lastSavedSteps + newSteps
                _currentSteps.value = totalToday
                
                // Only save to DB and update widget periodically to avoid UI thread spam
                // e.g. every 10 steps
                if (totalToday % 10 == 0) {
                    saveStepsToDb(totalToday)
                    updateWidget()
                }
                
                lastStepTime = now
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun saveCurrentSession() {
        if (currentSessionStartTime != 0L && lastStepTime > currentSessionStartTime) {
            val steps = _currentSteps.value - sessionStartTotalSteps
            if (steps > 0) {
                coroutineScope.launch {
                    val profile = repository.userPreferencesRepository.userProfileFlow.first()
                    val distance = repository.calculateDistance(steps, profile)
                    repository.addSession(WalkingSession(
                        dateString = getCurrentDateString(),
                        startTimeMs = currentSessionStartTime,
                        endTimeMs = lastStepTime,
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
            repository.updateStepsForDate(today, steps)
        }
    }
    
    private fun updateWidget() {
        coroutineScope.launch {
            com.example.widget.HealthWidget().updateAll(context)
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
            initialStepCount = -1f
            
            updateWidget()
        }
    }

    private fun getCurrentDateString(): String {
        return dateFormat.format(Date())
    }
}
