package com.example.data

import kotlinx.coroutines.flow.Flow

class StepRepository(private val stepDao: StepDao, private val sessionDao: SessionDao) {
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
        val distance = calculateDistance(newSteps)
        val calories = calculateCalories(newSteps)
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
    
    // Simple formulas for estimation
    private fun calculateDistance(steps: Int): Float {
        // Assume average stride length of 0.762 meters
        return (steps * 0.762f) / 1000f
    }
    
    private fun calculateCalories(steps: Int): Float {
        // Assume ~0.04 calories per step
        return steps * 0.04f
    }
}
