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

    suspend fun updateStepsForDate(date: String, newSteps: Int, cadenceStepsPerMin: Float = 100f) {
        val profile = userPreferencesRepository.userProfileFlow.first()
        val distance = calculateDistance(newSteps, profile, cadenceStepsPerMin)
        val activeMinutes = FitnessCalculations.calculateActiveDurationMinutes(newSteps, cadenceStepsPerMin)
        val durationHours = activeMinutes / 60f
        val paceSeconds = FitnessCalculations.calculatePaceSecondsPerKm(distance, activeMinutes)
        val speedKmh = FitnessCalculations.calculateSpeedKmh(distance, durationHours)
        val met = FitnessCalculations.determineMetFromPaceSeconds(paceSeconds, speedKmh)
        val calories = FitnessCalculations.calculateActiveCalories(met, profile.weightKg, durationHours)

        val record = DailyStepRecord(
            dateString = date,
            steps = newSteps,
            distanceKm = distance,
            caloriesBurned = calories,
            activeTimeMinutes = activeMinutes.toInt(),
            paceSecondsPerKm = paceSeconds,
            avgSpeedKmh = speedKmh,
            avgCadence = cadenceStepsPerMin.toInt().coerceAtLeast(60)
        )
        stepDao.insertOrUpdate(record)
    }
    
    fun getSessionsForDate(date: String): Flow<List<WalkingSession>> {
        return sessionDao.getSessionsForDate(date)
    }
    
    suspend fun addSession(session: WalkingSession) {
        sessionDao.insertSession(session)
    }
    
    fun calculateDistance(steps: Int, profile: UserProfile, cadenceStepsPerMin: Float = 100f): Float {
        return FitnessCalculations.calculateDistanceKm(steps, profile.heightCm, profile.gender, cadenceStepsPerMin)
    }
    
    fun calculateCalories(distanceKm: Float, profile: UserProfile, durationMinutes: Float = 0f, cadenceStepsPerMin: Float = 100f): Float {
        val minutes = if (durationMinutes > 0f) durationMinutes else (distanceKm * 12f).coerceAtLeast(1f) // default ~12 min/km
        val durationHours = minutes / 60f
        val paceSeconds = FitnessCalculations.calculatePaceSecondsPerKm(distanceKm, minutes)
        val speed = FitnessCalculations.calculateSpeedKmh(distanceKm, durationHours)
        val met = FitnessCalculations.determineMetFromPaceSeconds(paceSeconds, speed)
        return FitnessCalculations.calculateActiveCalories(met, profile.weightKg, durationHours)
    }

    suspend fun updateCustomDataForDate(date: String, newSteps: Int, newDistance: Float, newCalories: Float) {
        val durationMinutes = if (newDistance > 0f) (newDistance * 11.5f) else (newSteps / 100f)
        val paceSeconds = FitnessCalculations.calculatePaceSecondsPerKm(newDistance, durationMinutes)
        val speedKmh = FitnessCalculations.calculateSpeedKmh(newDistance, durationMinutes / 60f)
        val record = DailyStepRecord(
            dateString = date,
            steps = newSteps,
            distanceKm = newDistance,
            caloriesBurned = newCalories,
            activeTimeMinutes = durationMinutes.toInt(),
            paceSecondsPerKm = paceSeconds,
            avgSpeedKmh = speedKmh,
            avgCadence = 100
        )
        stepDao.insertOrUpdate(record)
    }
}
