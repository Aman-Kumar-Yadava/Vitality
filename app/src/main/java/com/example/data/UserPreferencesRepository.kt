package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

data class UserProfile(
    val hasCompletedOnboarding: Boolean = false,
    val userName: String = "User",
    val dailyStepGoal: Int = 10000,
    val dailyDistanceGoalKm: Float = 8.0f,
    val dailyCaloriesGoal: Int = 500,
    val heightCm: Float = 170f,
    val weightKg: Float = 70f,
    val age: Int = 25,
    val gender: String = "Male",
    
    // Tracking & Goals Preference
    val primaryProgressMetric: String = "Steps", // "Steps", "Distance", "Calories"
    
    // Voice Announcement Settings
    val announcementsEnabled: Boolean = true,
    val announceStepsInterval: Int = 1000,
    val announceDistanceIntervalKm: Float = 1.0f,
    val announceCaloriesEnabled: Boolean = true,
    val announceCaloriesInterval: Float = 100f,
    val speechRate: Float = 1.0f,
    val pitch: Float = 1.0f,
    
    // Widget Settings
    val widgetOpacity: Float = 1.0f,
    
    // UI Settings
    val uiNoiseLevel: Float = 0.0f,
    val pillMenuNoiseLevel: Float = 0.0f
)

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {
    
    private object PreferencesKeys {
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val USER_NAME = stringPreferencesKey("user_name")
        val DAILY_STEP_GOAL = intPreferencesKey("daily_step_goal")
        val DAILY_DISTANCE_GOAL = floatPreferencesKey("daily_distance_goal")
        val DAILY_CALORIES_GOAL = intPreferencesKey("daily_calories_goal")
        val HEIGHT_CM = floatPreferencesKey("height_cm")
        val WEIGHT_KG = floatPreferencesKey("weight_kg")
        val AGE = intPreferencesKey("age")
        val GENDER = stringPreferencesKey("gender")
        
        val PRIMARY_PROGRESS_METRIC = stringPreferencesKey("primary_progress_metric")
        
        val ANNOUNCEMENTS_ENABLED = booleanPreferencesKey("announcements_enabled")
        val ANNOUNCE_STEPS_INTERVAL = intPreferencesKey("announce_steps_interval")
        val ANNOUNCE_DISTANCE_INTERVAL = floatPreferencesKey("announce_distance_interval")
        val ANNOUNCE_CALORIES_ENABLED = booleanPreferencesKey("announce_calories_enabled")
        val ANNOUNCE_CALORIES_INTERVAL = floatPreferencesKey("announce_calories_interval")
        val SPEECH_RATE = floatPreferencesKey("speech_rate")
        val PITCH = floatPreferencesKey("pitch")
        
        val WIDGET_OPACITY = floatPreferencesKey("widget_opacity")
        val UI_NOISE_LEVEL = floatPreferencesKey("ui_noise_level")
        val PILL_MENU_NOISE_LEVEL = floatPreferencesKey("pill_menu_noise_level")
    }
    
    val userProfileFlow: Flow<UserProfile> = dataStore.data.map { preferences ->
        UserProfile(
            hasCompletedOnboarding = preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] ?: false,
            userName = preferences[PreferencesKeys.USER_NAME] ?: "User",
            dailyStepGoal = preferences[PreferencesKeys.DAILY_STEP_GOAL] ?: 10000,
            dailyDistanceGoalKm = preferences[PreferencesKeys.DAILY_DISTANCE_GOAL] ?: 8.0f,
            dailyCaloriesGoal = preferences[PreferencesKeys.DAILY_CALORIES_GOAL] ?: 500,
            heightCm = preferences[PreferencesKeys.HEIGHT_CM] ?: 170f,
            weightKg = preferences[PreferencesKeys.WEIGHT_KG] ?: 70f,
            age = preferences[PreferencesKeys.AGE] ?: 25,
            gender = preferences[PreferencesKeys.GENDER] ?: "Male",
            
            primaryProgressMetric = preferences[PreferencesKeys.PRIMARY_PROGRESS_METRIC] ?: "Steps",
            
            announcementsEnabled = preferences[PreferencesKeys.ANNOUNCEMENTS_ENABLED] ?: true,
            announceStepsInterval = preferences[PreferencesKeys.ANNOUNCE_STEPS_INTERVAL] ?: 1000,
            announceDistanceIntervalKm = preferences[PreferencesKeys.ANNOUNCE_DISTANCE_INTERVAL] ?: 1.0f,
            announceCaloriesEnabled = preferences[PreferencesKeys.ANNOUNCE_CALORIES_ENABLED] ?: true,
            announceCaloriesInterval = preferences[PreferencesKeys.ANNOUNCE_CALORIES_INTERVAL] ?: 100f,
            speechRate = preferences[PreferencesKeys.SPEECH_RATE] ?: 1.0f,
            pitch = preferences[PreferencesKeys.PITCH] ?: 1.0f,
            widgetOpacity = preferences[PreferencesKeys.WIDGET_OPACITY] ?: 1.0f,
            uiNoiseLevel = preferences[PreferencesKeys.UI_NOISE_LEVEL] ?: 0.0f,
            pillMenuNoiseLevel = preferences[PreferencesKeys.PILL_MENU_NOISE_LEVEL] ?: 0.0f
        )
    }
    
    suspend fun updateProfile(profile: UserProfile) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] = profile.hasCompletedOnboarding
            preferences[PreferencesKeys.USER_NAME] = profile.userName
            preferences[PreferencesKeys.DAILY_STEP_GOAL] = profile.dailyStepGoal
            preferences[PreferencesKeys.DAILY_DISTANCE_GOAL] = profile.dailyDistanceGoalKm
            preferences[PreferencesKeys.DAILY_CALORIES_GOAL] = profile.dailyCaloriesGoal
            preferences[PreferencesKeys.HEIGHT_CM] = profile.heightCm
            preferences[PreferencesKeys.WEIGHT_KG] = profile.weightKg
            preferences[PreferencesKeys.AGE] = profile.age
            preferences[PreferencesKeys.GENDER] = profile.gender
            
            preferences[PreferencesKeys.PRIMARY_PROGRESS_METRIC] = profile.primaryProgressMetric
            
            preferences[PreferencesKeys.ANNOUNCEMENTS_ENABLED] = profile.announcementsEnabled
            preferences[PreferencesKeys.ANNOUNCE_STEPS_INTERVAL] = profile.announceStepsInterval
            preferences[PreferencesKeys.ANNOUNCE_DISTANCE_INTERVAL] = profile.announceDistanceIntervalKm
            preferences[PreferencesKeys.ANNOUNCE_CALORIES_ENABLED] = profile.announceCaloriesEnabled
            preferences[PreferencesKeys.ANNOUNCE_CALORIES_INTERVAL] = profile.announceCaloriesInterval
            preferences[PreferencesKeys.SPEECH_RATE] = profile.speechRate
            preferences[PreferencesKeys.PITCH] = profile.pitch
            preferences[PreferencesKeys.WIDGET_OPACITY] = profile.widgetOpacity
            preferences[PreferencesKeys.UI_NOISE_LEVEL] = profile.uiNoiseLevel
            preferences[PreferencesKeys.PILL_MENU_NOISE_LEVEL] = profile.pillMenuNoiseLevel
        }
    }
    
    suspend fun completeOnboarding() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] = true
        }
    }
}
