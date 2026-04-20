package com.helpapp.therapy.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.helpapp.therapy.data.db.entities.DereflectionEntity
import com.helpapp.therapy.data.db.entities.ResponsibilityPieEntity
import com.helpapp.therapy.data.db.entities.VitalityCompassEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ResponsibilityPieDao {
    @Insert
    suspend fun insert(entity: ResponsibilityPieEntity): Long

    @Query("SELECT * FROM module_responsibility_pie ORDER BY timestamp DESC LIMIT 60")
    fun observeRecent(): Flow<List<ResponsibilityPieEntity>>
}

@Dao
interface DereflectionDao {
    @Insert
    suspend fun insert(entity: DereflectionEntity): Long

    @Query("SELECT * FROM module_dereflection ORDER BY timestamp DESC LIMIT 30")
    fun observeRecent(): Flow<List<DereflectionEntity>>
}

@Dao
interface VitalityCompassDao {
    @Insert
    suspend fun insert(entity: VitalityCompassEntity): Long

    @Query("UPDATE module_vitality_compass SET actionTimeSpentSec = :seconds WHERE id = :id")
    suspend fun recordAction(id: Long, seconds: Int)

    @Query("SELECT * FROM module_vitality_compass ORDER BY timestamp DESC LIMIT 60")
    fun observeRecent(): Flow<List<VitalityCompassEntity>>
}
