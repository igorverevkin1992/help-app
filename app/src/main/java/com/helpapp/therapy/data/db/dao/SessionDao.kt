package com.helpapp.therapy.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.helpapp.therapy.data.db.entities.DailySessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM daily_therapy_sessions WHERE sessionId = :id LIMIT 1")
    suspend fun get(id: String): DailySessionEntity?

    @Query("SELECT * FROM daily_therapy_sessions WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC LIMIT 1")
    suspend fun getForWindow(start: Long, end: Long): DailySessionEntity?

    @Query("SELECT * FROM daily_therapy_sessions ORDER BY timestamp DESC LIMIT 30")
    fun observeRecent(): Flow<List<DailySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: DailySessionEntity)

    @Query("UPDATE daily_therapy_sessions SET morningCompleted = :done WHERE sessionId = :id")
    suspend fun setMorning(id: String, done: Boolean)

    @Query("UPDATE daily_therapy_sessions SET middayCompleted = :done WHERE sessionId = :id")
    suspend fun setMidday(id: String, done: Boolean)

    @Query("UPDATE daily_therapy_sessions SET eveningCompleted = :done WHERE sessionId = :id")
    suspend fun setEvening(id: String, done: Boolean)
}
