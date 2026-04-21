package com.helpapp.therapy.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "module_responsibility_pie",
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
data class ResponsibilityPieEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val irrationalThought: String,
    val guiltScorePre: Int,
    val guiltScorePost: Int,
    val biologyWeightPct: Int,
    val medicalWeightPct: Int,
    val socialWeightPct: Int,
    val controlWeightPct: Int,
    val statementGenerated: String,
)
