package com.helpapp.therapy.export

import android.content.Context
import android.net.Uri
import com.helpapp.therapy.data.db.dao.AssessmentDao
import com.helpapp.therapy.data.db.dao.DereflectionDao
import com.helpapp.therapy.data.db.dao.ResponsibilityPieDao
import com.helpapp.therapy.data.db.dao.SessionDao
import com.helpapp.therapy.data.db.dao.VitalityCompassDao
import com.helpapp.therapy.data.db.entities.AssessmentEntity
import com.helpapp.therapy.data.db.entities.DailySessionEntity
import com.helpapp.therapy.data.db.entities.DereflectionEntity
import com.helpapp.therapy.data.db.entities.ResponsibilityPieEntity
import com.helpapp.therapy.data.db.entities.VitalityCompassEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Serializes all local clinical records into a single JSON document written
 * to a user-chosen location via the Storage Access Framework. The API key
 * and biographical context variables are NEVER included — the export is
 * limited to the clinical records themselves.
 */
@Singleton
class ExportService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionDao: SessionDao,
    private val pieDao: ResponsibilityPieDao,
    private val dereflectionDao: DereflectionDao,
    private val vitalityDao: VitalityCompassDao,
    private val assessmentDao: AssessmentDao,
) {
    private val json = Json { prettyPrint = true; encodeDefaults = true }

    suspend fun export(target: Uri): Long {
        val bundle = ExportBundle(
            schemaVersion = SCHEMA_VERSION,
            exportedAtMs = System.currentTimeMillis(),
            sessions = sessionDao.observeRecent().first(),
            responsibility = pieDao.getAll(),
            dereflection = dereflectionDao.getAll(),
            vitality = vitalityDao.getAll(),
            assessments = assessmentDao.getAll(),
        )
        val payload = json.encodeToString(bundle).toByteArray(Charsets.UTF_8)
        context.contentResolver.openOutputStream(target, "w")
            ?.use { it.write(payload) }
            ?: error("Unable to open the selected file for writing.")
        return payload.size.toLong()
    }

    companion object {
        const val SCHEMA_VERSION = 1
    }
}

@Serializable
data class ExportBundle(
    val schemaVersion: Int,
    val exportedAtMs: Long,
    val sessions: List<DailySessionEntity>,
    val responsibility: List<ResponsibilityPieEntity>,
    val dereflection: List<DereflectionEntity>,
    val vitality: List<VitalityCompassEntity>,
    val assessments: List<AssessmentEntity>,
)
