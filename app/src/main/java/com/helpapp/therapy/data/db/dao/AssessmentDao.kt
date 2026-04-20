package com.helpapp.therapy.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.helpapp.therapy.data.db.entities.AssessmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssessmentDao {
    @Insert
    suspend fun insert(entity: AssessmentEntity): Long

    @Query("SELECT * FROM psychometric_assessments ORDER BY timestamp ASC")
    fun observeAll(): Flow<List<AssessmentEntity>>

    @Query("SELECT * FROM psychometric_assessments ORDER BY timestamp DESC LIMIT 1")
    suspend fun latest(): AssessmentEntity?
}
