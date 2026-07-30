#!/bin/bash
cat << 'INNER_EOF' >> app/src/main/java/com/example/data/StepRepository.kt

    suspend fun updateCustomDataForDate(date: String, newSteps: Int, newDistance: Float, newCalories: Float) {
        val record = DailyStepRecord(
            dateString = date,
            steps = newSteps,
            distanceKm = newDistance,
            caloriesBurned = newCalories
        )
        stepDao.insertOrUpdate(record)
    }
INNER_EOF
