package com.helpapp.therapy.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
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
    version = 1,
    exportSchema = false,
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
    }
}
