package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_steps")
data class DailyStepRecord(
    @PrimaryKey
    val dateString: String, // e.g. "2023-10-27"
    val steps: Int,
    val distanceKm: Float,
    val caloriesBurned: Float,
    val activeTimeMinutes: Int = 0,
    val paceSecondsPerKm: Int = 0,
    val avgSpeedKmh: Float = 0f,
    val avgCadence: Int = 100
)
