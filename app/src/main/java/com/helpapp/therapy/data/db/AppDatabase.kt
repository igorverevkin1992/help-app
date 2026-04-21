package com.helpapp.therapy.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.helpapp.therapy.data.db.dao.AssessmentDao
import com.helpapp.therapy.data.db.dao.DereflectionDao
import com.helpapp.therapy.data.db.dao.ResponsibilityPieDao
import com.helpapp.therapy.data.db.dao.SessionDao
import com.helpapp.therapy.data.db.dao.UserContextDao
import com.helpapp.therapy.data.db.dao.VitalityCompassDao
import com.helpapp.therapy.data.db.entities.AssessmentEntity
import com.helpapp.therapy.data.db.entities.DailySessionEntity
import com.helpapp.therapy.data.db.entities.DereflectionEntity
import com.helpapp.therapy.data.db.entities.ResponsibilityPieEntity
import com.helpapp.therapy.data.db.entities.UserContextEntity
import com.helpapp.therapy.data.db.entities.VitalityCompassEntity

@Database(
    entities = [
        UserContextEntity::class,
        DailySessionEntity::class,
        ResponsibilityPieEntity::class,
        DereflectionEntity::class,
        VitalityCompassEntity::class,
        AssessmentEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userContextDao(): UserContextDao
    abstract fun sessionDao(): SessionDao
    abstract fun responsibilityPieDao(): ResponsibilityPieDao
    abstract fun dereflectionDao(): DereflectionDao
    abstract fun vitalityCompassDao(): VitalityCompassDao
    abstract fun assessmentDao(): AssessmentDao

    companion object {
        const val NAME = "therapy.db"

        /**
         * Renames the per-module completion flags so that the column names
         * describe the module rather than the time of day, without losing
         * any historical rows.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS daily_therapy_sessions_new (
                        sessionId TEXT NOT NULL PRIMARY KEY,
                        timestamp INTEGER NOT NULL,
                        localDate TEXT NOT NULL DEFAULT '',
                        responsibilityCompleted INTEGER NOT NULL DEFAULT 0,
                        dereflectionCompleted INTEGER NOT NULL DEFAULT 0,
                        vitalityCompleted INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO daily_therapy_sessions_new
                        (sessionId, timestamp, localDate,
                         responsibilityCompleted, dereflectionCompleted, vitalityCompleted)
                    SELECT sessionId, timestamp, '',
                           morningCompleted, middayCompleted, eveningCompleted
                    FROM daily_therapy_sessions
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE daily_therapy_sessions")
                db.execSQL("ALTER TABLE daily_therapy_sessions_new RENAME TO daily_therapy_sessions")
            }
        }

        val ALL_MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_1_2)
    }
}
