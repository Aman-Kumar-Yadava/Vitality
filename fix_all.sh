#!/bin/bash
cat << 'INNEREOF' > app/src/main/java/com/example/VitalityApplication.kt
package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.StepRepository
import com.example.data.StepTrackerManager
import com.example.data.UserPreferencesRepository
import com.example.data.dataStore

class VitalityApplication : Application() {
    lateinit var database: AppDatabase
        private set
        
    lateinit var userPrefsRepository: UserPreferencesRepository
        private set
        
    lateinit var stepRepository: StepRepository
        private set

    lateinit var stepTrackerManager: StepTrackerManager
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        userPrefsRepository = UserPreferencesRepository(dataStore)
        stepRepository = StepRepository(database.stepDao(), database.sessionDao(), userPrefsRepository)
        stepTrackerManager = StepTrackerManager(this, stepRepository)
    }
}
INNEREOF

sed -i 's/OnboardingScreen(onComplete = { goal, height, weight, age, gender ->/OnboardingScreen(onComplete = { goal, height, weight, age, gender ->/' app/src/main/java/com/example/ui/RootScreen.kt
sed -i 's/OnboardingScreen { goal, height, weight, age, gender, location ->/OnboardingScreen { goal, height, weight, age, gender ->/' app/src/main/java/com/example/ui/RootScreen.kt
sed -i 's/viewModel.completeOnboarding(goal, height, weight, age, gender, location)/viewModel.completeOnboarding(goal, height, weight, age, gender)/' app/src/main/java/com/example/ui/RootScreen.kt
sed -i '/var locationStr/d' app/src/main/java/com/example/ui/SettingsScreen.kt
sed -i '/location = locationStr/d' app/src/main/java/com/example/ui/SettingsScreen.kt

cat << 'INNEREOF' > app/src/main/java/com/example/viewmodel/MainViewModel.kt
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
        
    fun completeOnboarding(goal: Int, height: Float, weight: Float, age: Int, gender: String) {
        viewModelScope.launch {
            val current = userProfile.value
            userPrefsRepository.updateProfile(
                current.copy(
                    hasCompletedOnboarding = true,
                    dailyStepGoal = goal,
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
        val intent = android.content.Intent(app, com.example.service.StepTrackingService::class.java).apply {
            action = com.example.service.StepTrackingService.ACTION_PASSIVE_START
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            app.startForegroundService(intent)
        } else {
            app.startService(intent)
        }
    }

    fun startTracking() {
        val intent = android.content.Intent(app, com.example.service.StepTrackingService::class.java).apply {
            action = com.example.service.StepTrackingService.ACTION_START
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            app.startForegroundService(intent)
        } else {
            app.startService(intent)
        }
    }

    fun stopTracking() {
        val intent = android.content.Intent(app, com.example.service.StepTrackingService::class.java).apply {
            action = com.example.service.StepTrackingService.ACTION_STOP
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            app.startForegroundService(intent)
        } else {
            app.startService(intent)
        }
    }

    fun addMockSteps() {
        stepTrackerManager.addMockSteps(500)
    }
}
INNEREOF
