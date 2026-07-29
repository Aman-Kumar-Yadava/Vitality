package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM walking_sessions WHERE dateString = :date ORDER BY startTimeMs DESC")
    fun getSessionsForDate(date: String): Flow<List<WalkingSession>>

    @Insert
    suspend fun insertSession(session: WalkingSession)
}
