package com.helpapp.therapy.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_therapy_sessions")
data class DailySessionEntity(
    @PrimaryKey val sessionId: String,
    val timestamp: Long,
    val morningCompleted: Boolean = false,
    val middayCompleted: Boolean = false,
    val eveningCompleted: Boolean = false,
) {
    val completionStatus: Boolean
        get() = morningCompleted && middayCompleted && eveningCompleted
}
