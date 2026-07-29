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
