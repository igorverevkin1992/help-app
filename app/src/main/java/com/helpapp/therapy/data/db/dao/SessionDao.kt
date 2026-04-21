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

    @Query("SELECT * FROM daily_therapy_sessions WHERE localDate = :localDate LIMIT 1")
    suspend fun getByLocalDate(localDate: String): DailySessionEntity?

    @Query("SELECT * FROM daily_therapy_sessions ORDER BY timestamp DESC LIMIT 30")
    fun observeRecent(): Flow<List<DailySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: DailySessionEntity)

    @Query("UPDATE daily_therapy_sessions SET responsibilityCompleted = :done WHERE sessionId = :id")
    suspend fun setResponsibility(id: String, done: Boolean)

    @Query("UPDATE daily_therapy_sessions SET dereflectionCompleted = :done WHERE sessionId = :id")
    suspend fun setDereflection(id: String, done: Boolean)

    @Query("UPDATE daily_therapy_sessions SET vitalityCompleted = :done WHERE sessionId = :id")
    suspend fun setVitality(id: String, done: Boolean)

    @Query("DELETE FROM daily_therapy_sessions WHERE timestamp < :olderThan")
    suspend fun purgeOlderThan(olderThan: Long): Int
}
