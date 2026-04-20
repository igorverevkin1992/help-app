package com.helpapp.therapy.data.repository

import com.helpapp.therapy.data.db.dao.SessionDao
import com.helpapp.therapy.data.db.entities.DailySessionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor(
    private val dao: SessionDao,
) {
    fun observeRecent(): Flow<List<DailySessionEntity>> = dao.observeRecent()

    suspend fun todaySession(): DailySessionEntity {
        val (start, end) = todayWindow()
        val existing = dao.getForWindow(start, end)
        if (existing != null) return existing
        val new = DailySessionEntity(
            sessionId = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
        )
        dao.upsert(new)
        return new
    }

    suspend fun markMorning(id: String) = dao.setMorning(id, true)
    suspend fun markMidday(id: String) = dao.setMidday(id, true)
    suspend fun markEvening(id: String) = dao.setEvening(id, true)

    private fun todayWindow(): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val start = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        return start to end
    }
}
