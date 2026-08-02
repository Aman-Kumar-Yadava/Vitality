package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class StepRepository(
    private val stepDao: StepDao, 
    private val sessionDao: SessionDao,
    val userPreferencesRepository: UserPreferencesRepository
) {
    private val mutex = Mutex()

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

    suspend fun updateStepsForDate(date: String, newSteps: Int, cadenceStepsPerMin: Float = 100f) = mutex.withLock {
        val profile = userPreferencesRepository.userProfileFlow.first()
        val existingRecord = stepDao.getRecordForDateSync(date)
        val currentDbSteps = existingRecord?.steps ?: 0
        
        val deltaSteps = (newSteps - currentDbSteps).coerceAtLeast(0)
        
        // If we are replacing existing total with same or fewer steps (due to some sync error), 
        // we can just return or we can force update the steps but not decrease distance. 
        // Best approach: If deltaSteps > 0, calculate delta metrics and add them.
        
        val deltaDistance = calculateDistance(deltaSteps, profile, cadenceStepsPerMin)
        val deltaActiveMinutes = FitnessCalculations.calculateActiveDurationMinutes(deltaSteps, cadenceStepsPerMin)
        val deltaDurationHours = deltaActiveMinutes / 60f
        
        val paceSeconds = FitnessCalculations.calculatePaceSecondsPerKm(deltaDistance, deltaActiveMinutes)
        val speedKmh = FitnessCalculations.calculateSpeedKmh(deltaDistance, deltaDurationHours)
        val met = FitnessCalculations.determineMetFromPaceSeconds(paceSeconds, speedKmh)
        val deltaCalories = FitnessCalculations.calculateActiveCalories(met, profile.weightKg, deltaDurationHours)

        val totalDistance = (existingRecord?.distanceKm ?: 0f) + deltaDistance
        val totalCalories = (existingRecord?.caloriesBurned ?: 0f) + deltaCalories
        val totalActiveMinutes = (existingRecord?.activeTimeMinutes ?: 0) + deltaActiveMinutes.toInt()
        
        val totalPace = if (totalDistance > 0f) FitnessCalculations.calculatePaceSecondsPerKm(totalDistance, totalActiveMinutes.toFloat()) else 0
        val totalSpeed = if (totalActiveMinutes > 0) FitnessCalculations.calculateSpeedKmh(totalDistance, totalActiveMinutes / 60f) else 0f
        
        val oldAvgCadence = existingRecord?.avgCadence?.toFloat() ?: cadenceStepsPerMin
        val totalAvgCadence: Float = if (deltaSteps > 0 && (currentDbSteps + deltaSteps) > 0) {
            ((oldAvgCadence * currentDbSteps) + (cadenceStepsPerMin * deltaSteps)) / (currentDbSteps + deltaSteps).toFloat()
        } else {
            oldAvgCadence
        }

        val record = DailyStepRecord(
            dateString = date,
            steps = newSteps,
            distanceKm = totalDistance,
            caloriesBurned = totalCalories,
            activeTimeMinutes = totalActiveMinutes,
            paceSecondsPerKm = totalPace.toInt(),
            avgSpeedKmh = totalSpeed,
            avgCadence = totalAvgCadence.toInt().coerceAtLeast(60)
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

    suspend fun updateCustomDataForDate(date: String, newSteps: Int, newDistance: Float, newCalories: Float) = mutex.withLock {
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
