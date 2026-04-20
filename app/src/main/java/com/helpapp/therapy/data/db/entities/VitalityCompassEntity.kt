package com.helpapp.therapy.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "module_vitality_compass",
    foreignKeys = [
        ForeignKey(
            entity = DailySessionEntity::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("sessionId")],
)
data class VitalityCompassEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val cognitiveHook: String,
    val sufferingPath: String,
    val vitalityPath: String,
    val actionTimeSpentSec: Int = 0,
)
