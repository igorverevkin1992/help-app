package com.helpapp.therapy.data.repository

import com.helpapp.therapy.data.db.dao.UserContextDao
import com.helpapp.therapy.data.db.entities.UserContextEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserContextRepository @Inject constructor(
    private val dao: UserContextDao,
) {
    fun observe(): Flow<UserContextEntity> =
        dao.observe().map { it ?: DEFAULT }

    suspend fun require(): UserContextEntity = dao.get() ?: DEFAULT

    suspend fun save(context: UserContextEntity) =
        dao.upsert(context.copy(id = UserContextEntity.SINGLETON_ID))

    companion object {
        /** Generalist placeholder values. The whole point of the system is
         *  that these are swappable state variables, not hardcoded content. */
        val DEFAULT = UserContextEntity(
            biologicalTrigger = "Involuntary amygdala survival reflex under existential threat",
            objectiveLimitation = "Objectively verified physical limitation outside of voluntary control",
            socialDuty = "Quantifiable duty toward dependents whose welfare the user underwrites",
            transcendentGoal = "Generative macro-projects that build structure against entropy",
        )
    }
}
