package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DailyStepRecord
import com.example.data.StepRepository
import com.example.data.StepTrackerManager
import com.example.data.WalkingSession
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.data.UserPreferencesRepository
import com.example.data.UserProfile
import com.example.data.dataStore

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as com.example.VitalityApplication
    private val userPrefsRepository = app.userPrefsRepository
    private val database = app.database
    private val repository = app.stepRepository
    val stepTrackerManager = app.stepTrackerManager

    val userProfile: StateFlow<UserProfile> = userPrefsRepository.userProfileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())
        
    fun completeOnboarding(
        userName: String,
        stepGoal: Int,
        distGoalKm: Float,
        calGoal: Int,
        height: Float,
        weight: Float,
        age: Int,
        gender: String
    ) {
        viewModelScope.launch {
            val current = userProfile.value
            userPrefsRepository.updateProfile(
                current.copy(
                    hasCompletedOnboarding = true,
                    userName = userName,
                    dailyStepGoal = stepGoal,
                    dailyDistanceGoalKm = distGoalKm,
                    dailyCaloriesGoal = calGoal,
                    heightCm = height,
                    weightKg = weight,
                    age = age,
                    gender = gender
                )
            )
        }
    }
    
    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            userPrefsRepository.updateProfile(profile)
        }
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val todayString = dateFormat.format(Date())

    val todayRecord: StateFlow<DailyStepRecord?> = repository.getRecordForDate(todayString)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todaySessions: StateFlow<List<WalkingSession>> = repository.getSessionsForDate(todayString)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecords: StateFlow<List<DailyStepRecord>> = repository.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSteps: StateFlow<Int?> = repository.totalSteps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalDistance: StateFlow<Float?> = repository.totalDistance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val totalCalories: StateFlow<Float?> = repository.totalCalories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)
        
    val isTracking: StateFlow<Boolean> = stepTrackerManager.isTracking

    fun startPassiveTracking() {
        try {
            val intent = android.content.Intent(app, com.example.service.StepTrackingService::class.java).apply {
                action = com.example.service.StepTrackingService.ACTION_PASSIVE_START
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                app.startForegroundService(intent)
            } else {
                app.startService(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startTracking() {
        try {
            val intent = android.content.Intent(app, com.example.service.StepTrackingService::class.java).apply {
                action = com.example.service.StepTrackingService.ACTION_START
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                app.startForegroundService(intent)
            } else {
                app.startService(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopTracking() {
        try {
            val intent = android.content.Intent(app, com.example.service.StepTrackingService::class.java).apply {
                action = com.example.service.StepTrackingService.ACTION_STOP
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                app.startForegroundService(intent)
            } else {
                app.startService(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addMockSteps() {
        stepTrackerManager.addMockSteps(500)
    }

    fun setCustomActivity(steps: Int, distance: Float, calories: Float) {
        stepTrackerManager.setCustomActivity(steps, distance, calories)
    }
}
