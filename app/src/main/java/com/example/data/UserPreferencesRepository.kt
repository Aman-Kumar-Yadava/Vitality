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
    val dailyStepGoal: Int = 10000,
    val heightCm: Float = 170f,
    val weightKg: Float = 70f,
    val age: Int = 25,
    val gender: String = "Male",
    val location: String = "San Francisco",
    
    // Voice Announcement Settings
    val announcementsEnabled: Boolean = true,
    val announceStepsInterval: Int = 1000,
    val announceDistanceIntervalKm: Float = 1.0f,
    val announceCaloriesInterval: Float = 100f,
    val speechRate: Float = 1.0f,
    val pitch: Float = 1.0f,
    
    // Widget Settings
    val widgetOpacity: Float = 1.0f
)

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {
    
    private object PreferencesKeys {
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val DAILY_STEP_GOAL = intPreferencesKey("daily_step_goal")
        val HEIGHT_CM = floatPreferencesKey("height_cm")
        val WEIGHT_KG = floatPreferencesKey("weight_kg")
        val AGE = intPreferencesKey("age")
        val GENDER = stringPreferencesKey("gender")
        val LOCATION = stringPreferencesKey("location")
        
        val ANNOUNCEMENTS_ENABLED = booleanPreferencesKey("announcements_enabled")
        val ANNOUNCE_STEPS_INTERVAL = intPreferencesKey("announce_steps_interval")
        val ANNOUNCE_DISTANCE_INTERVAL = floatPreferencesKey("announce_distance_interval")
        val ANNOUNCE_CALORIES_INTERVAL = floatPreferencesKey("announce_calories_interval")
        val SPEECH_RATE = floatPreferencesKey("speech_rate")
        val PITCH = floatPreferencesKey("pitch")
        
        val WIDGET_OPACITY = floatPreferencesKey("widget_opacity")
    }
    
    val userProfileFlow: Flow<UserProfile> = dataStore.data.map { preferences ->
        UserProfile(
            hasCompletedOnboarding = preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] ?: false,
            dailyStepGoal = preferences[PreferencesKeys.DAILY_STEP_GOAL] ?: 10000,
            heightCm = preferences[PreferencesKeys.HEIGHT_CM] ?: 170f,
            weightKg = preferences[PreferencesKeys.WEIGHT_KG] ?: 70f,
            age = preferences[PreferencesKeys.AGE] ?: 25,
            gender = preferences[PreferencesKeys.GENDER] ?: "Male",
            location = preferences[PreferencesKeys.LOCATION] ?: "San Francisco",
            
            announcementsEnabled = preferences[PreferencesKeys.ANNOUNCEMENTS_ENABLED] ?: true,
            announceStepsInterval = preferences[PreferencesKeys.ANNOUNCE_STEPS_INTERVAL] ?: 1000,
            announceDistanceIntervalKm = preferences[PreferencesKeys.ANNOUNCE_DISTANCE_INTERVAL] ?: 1.0f,
            announceCaloriesInterval = preferences[PreferencesKeys.ANNOUNCE_CALORIES_INTERVAL] ?: 100f,
            speechRate = preferences[PreferencesKeys.SPEECH_RATE] ?: 1.0f,
            pitch = preferences[PreferencesKeys.PITCH] ?: 1.0f,
            widgetOpacity = preferences[PreferencesKeys.WIDGET_OPACITY] ?: 1.0f
        )
    }
    
    suspend fun updateProfile(profile: UserProfile) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] = profile.hasCompletedOnboarding
            preferences[PreferencesKeys.DAILY_STEP_GOAL] = profile.dailyStepGoal
            preferences[PreferencesKeys.HEIGHT_CM] = profile.heightCm
            preferences[PreferencesKeys.WEIGHT_KG] = profile.weightKg
            preferences[PreferencesKeys.AGE] = profile.age
            preferences[PreferencesKeys.GENDER] = profile.gender
            preferences[PreferencesKeys.LOCATION] = profile.location
            
            preferences[PreferencesKeys.ANNOUNCEMENTS_ENABLED] = profile.announcementsEnabled
            preferences[PreferencesKeys.ANNOUNCE_STEPS_INTERVAL] = profile.announceStepsInterval
            preferences[PreferencesKeys.ANNOUNCE_DISTANCE_INTERVAL] = profile.announceDistanceIntervalKm
            preferences[PreferencesKeys.ANNOUNCE_CALORIES_INTERVAL] = profile.announceCaloriesInterval
            preferences[PreferencesKeys.SPEECH_RATE] = profile.speechRate
            preferences[PreferencesKeys.PITCH] = profile.pitch
            preferences[PreferencesKeys.WIDGET_OPACITY] = profile.widgetOpacity
        }
    }
    
    suspend fun completeOnboarding() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] = true
        }
    }
}
