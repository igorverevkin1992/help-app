package com.helpapp.therapy.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Weekly MIDS + SSFS snapshot. MIDS cut-off for clinically significant moral
 * injury is 27 — the app tracks movement below this threshold as core KPI.
 */
@Entity(tableName = "psychometric_assessments")
data class AssessmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val midsTotalScore: Int,
    val ssfsSffaScore: Int,
    val ssfsSfbScore: Int,
    val relocationMetrics: Int = 0,
)
