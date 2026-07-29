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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = StepRepository(database.stepDao(), database.sessionDao())
    val stepTrackerManager = StepTrackerManager(application, repository)

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
        
    fun startTracking() {
        stepTrackerManager.startTracking()
    }
    
    fun stopTracking() {
        stepTrackerManager.stopTracking()
    }
    
    fun addMockSteps() {
        stepTrackerManager.addMockSteps(500)
    }
}
