package com.helpapp.therapy.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Abstract user context. Holds the state variables dynamically injected into
 * the system prompt. Specific biographical details never leak into code — only
 * the values of these fields are swappable.
 */
@Entity(tableName = "user_context_variables")
data class UserContextEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val biologicalTrigger: String,
    val objectiveLimitation: String,
    val socialDuty: String,
    val transcendentGoal: String,
    val updatedAt: Long = System.currentTimeMillis(),
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}
