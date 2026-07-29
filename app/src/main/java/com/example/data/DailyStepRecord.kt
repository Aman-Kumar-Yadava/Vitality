package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_steps")
data class DailyStepRecord(
    @PrimaryKey
    val dateString: String, // e.g. "2023-10-27"
    val steps: Int,
    val distanceKm: Float,
    val caloriesBurned: Float
)
