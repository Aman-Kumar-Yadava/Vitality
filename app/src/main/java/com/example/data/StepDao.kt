package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {
    @Query("SELECT * FROM daily_steps ORDER BY dateString DESC")
    fun getAllRecords(): Flow<List<DailyStepRecord>>

    @Query("SELECT * FROM daily_steps WHERE dateString = :date")
    fun getRecordForDate(date: String): Flow<DailyStepRecord?>
    
    @Query("SELECT * FROM daily_steps WHERE dateString = :date")
    suspend fun getRecordForDateSync(date: String): DailyStepRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: DailyStepRecord)
    
    @Query("SELECT SUM(steps) FROM daily_steps")
    fun getTotalSteps(): Flow<Int?>
    
    @Query("SELECT SUM(distanceKm) FROM daily_steps")
    fun getTotalDistance(): Flow<Float?>
    
    @Query("SELECT SUM(caloriesBurned) FROM daily_steps")
    fun getTotalCalories(): Flow<Float?>
}
