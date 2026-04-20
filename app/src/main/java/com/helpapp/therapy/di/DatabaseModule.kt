package com.helpapp.therapy.di

import android.content.Context
import androidx.room.Room
import com.helpapp.therapy.data.db.AppDatabase
import com.helpapp.therapy.data.db.dao.AssessmentDao
import com.helpapp.therapy.data.db.dao.DereflectionDao
import com.helpapp.therapy.data.db.dao.ResponsibilityPieDao
import com.helpapp.therapy.data.db.dao.SessionDao
import com.helpapp.therapy.data.db.dao.UserContextDao
import com.helpapp.therapy.data.db.dao.VitalityCompassDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideUserContextDao(db: AppDatabase): UserContextDao = db.userContextDao()
    @Provides fun provideSessionDao(db: AppDatabase): SessionDao = db.sessionDao()
    @Provides fun providePieDao(db: AppDatabase): ResponsibilityPieDao = db.responsibilityPieDao()
    @Provides fun provideDereflectionDao(db: AppDatabase): DereflectionDao = db.dereflectionDao()
    @Provides fun provideVitalityDao(db: AppDatabase): VitalityCompassDao = db.vitalityCompassDao()
    @Provides fun provideAssessmentDao(db: AppDatabase): AssessmentDao = db.assessmentDao()
}
