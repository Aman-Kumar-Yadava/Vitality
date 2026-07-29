package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class StepRepository(
    private val stepDao: StepDao, 
    private val sessionDao: SessionDao,
    val userPreferencesRepository: UserPreferencesRepository
) {
    val allRecords: Flow<List<DailyStepRecord>> = stepDao.getAllRecords()
    val totalSteps: Flow<Int?> = stepDao.getTotalSteps()
    val totalDistance: Flow<Float?> = stepDao.getTotalDistance()
    val totalCalories: Flow<Float?> = stepDao.getTotalCalories()

    fun getRecordForDate(date: String): Flow<DailyStepRecord?> {
        return stepDao.getRecordForDate(date)
    }

    suspend fun getRecordForDateSync(date: String): DailyStepRecord? {
        return stepDao.getRecordForDateSync(date)
    }

    suspend fun updateStepsForDate(date: String, newSteps: Int) {
        val existing = stepDao.getRecordForDateSync(date)
        val profile = userPreferencesRepository.userProfileFlow.first()
        val distance = calculateDistance(newSteps, profile)
        val calories = calculateCalories(distance, profile)
        val record = DailyStepRecord(
            dateString = date,
            steps = newSteps,
            distanceKm = distance,
            caloriesBurned = calories
        )
        stepDao.insertOrUpdate(record)
    }
    
    fun getSessionsForDate(date: String): Flow<List<WalkingSession>> {
        return sessionDao.getSessionsForDate(date)
    }
    
    suspend fun addSession(session: WalkingSession) {
        sessionDao.insertSession(session)
    }
    
    fun calculateDistance(steps: Int, profile: UserProfile): Float {
        val strideLengthMeters = if (profile.gender == "Male") {
            profile.heightCm * 0.00415f
        } else {
            profile.heightCm * 0.00413f
        }
        return (steps * strideLengthMeters) / 1000f
    }
    
    fun calculateCalories(distanceKm: Float, profile: UserProfile): Float {
        // Approximate: distance (km) * weight (kg) * factor
        val factor = if (profile.gender == "Male") 1.03f else 0.98f
        return distanceKm * profile.weightKg * factor
    }
}
