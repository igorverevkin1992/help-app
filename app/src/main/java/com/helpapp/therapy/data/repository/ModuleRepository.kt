package com.helpapp.therapy.data.repository

import com.helpapp.therapy.data.db.dao.AssessmentDao
import com.helpapp.therapy.data.db.dao.DereflectionDao
import com.helpapp.therapy.data.db.dao.ResponsibilityPieDao
import com.helpapp.therapy.data.db.dao.VitalityCompassDao
import com.helpapp.therapy.data.db.entities.AssessmentEntity
import com.helpapp.therapy.data.db.entities.DereflectionEntity
import com.helpapp.therapy.data.db.entities.ResponsibilityPieEntity
import com.helpapp.therapy.data.db.entities.VitalityCompassEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModuleRepository @Inject constructor(
    private val pieDao: ResponsibilityPieDao,
    private val dereflectionDao: DereflectionDao,
    private val vitalityDao: VitalityCompassDao,
    private val assessmentDao: AssessmentDao,
) {
    suspend fun saveResponsibilityPie(entity: ResponsibilityPieEntity) = pieDao.insert(entity)
    fun observeResponsibilityPies(): Flow<List<ResponsibilityPieEntity>> = pieDao.observeRecent()

    suspend fun saveDereflection(entity: DereflectionEntity) = dereflectionDao.insert(entity)
    fun observeDereflections(): Flow<List<DereflectionEntity>> = dereflectionDao.observeRecent()

    suspend fun saveVitality(entity: VitalityCompassEntity): Long = vitalityDao.insert(entity)
    suspend fun recordVitalityAction(id: Long, seconds: Int) = vitalityDao.recordAction(id, seconds)
    fun observeVitality(): Flow<List<VitalityCompassEntity>> = vitalityDao.observeRecent()

    suspend fun saveAssessment(entity: AssessmentEntity): Long = assessmentDao.insert(entity)
    fun observeAssessments(): Flow<List<AssessmentEntity>> = assessmentDao.observeAll()
    suspend fun latestAssessment(): AssessmentEntity? = assessmentDao.latest()
}
