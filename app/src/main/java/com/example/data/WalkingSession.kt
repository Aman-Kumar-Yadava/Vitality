package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "walking_sessions")
data class WalkingSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateString: String,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val steps: Int,
    val distanceKm: Float
)
