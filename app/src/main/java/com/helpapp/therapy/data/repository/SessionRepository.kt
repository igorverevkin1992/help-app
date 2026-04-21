package com.helpapp.therapy.data.repository

import com.helpapp.therapy.data.db.dao.SessionDao
import com.helpapp.therapy.data.db.entities.DailySessionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor(
    private val dao: SessionDao,
) {
    fun observeRecent(): Flow<List<DailySessionEntity>> = dao.observeRecent()

    /**
     * Returns the session bucket corresponding to today in the device's
     * current timezone. Timezone shifts do not double-count a day: the same
     * localDate string always maps to one row.
     */
    suspend fun todaySession(): DailySessionEntity {
        val localDate = LocalDate.now(ZoneId.systemDefault()).format(ISO)
        val existing = dao.getByLocalDate(localDate)
        if (existing != null) return existing
        val new = DailySessionEntity(
            sessionId = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            localDate = localDate,
        )
        dao.upsert(new)
        return new
    }

    suspend fun markResponsibility(id: String) = dao.setResponsibility(id, true)
    suspend fun markDereflection(id: String) = dao.setDereflection(id, true)
    suspend fun markVitality(id: String) = dao.setVitality(id, true)

    suspend fun purgeOlderThan(retentionDays: Int): Int {
        val cutoff = System.currentTimeMillis() - retentionDays * 24L * 60L * 60L * 1000L
        return dao.purgeOlderThan(cutoff)
    }

    private companion object {
        val ISO: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    }
}
