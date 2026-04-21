package com.helpapp.therapy.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "daily_therapy_sessions")
data class DailySessionEntity(
    @PrimaryKey val sessionId: String,
    val timestamp: Long,
    /** ISO-8601 local date (yyyy-MM-dd) in the user's device timezone at the
     *  moment the session was created. Used to bucket sessions into a single
     *  day even across timezone shifts. */
    val localDate: String,
    val responsibilityCompleted: Boolean = false,
    val dereflectionCompleted: Boolean = false,
    val vitalityCompleted: Boolean = false,
) {
    val completionStatus: Boolean
        get() = responsibilityCompleted && dereflectionCompleted && vitalityCompleted
}
