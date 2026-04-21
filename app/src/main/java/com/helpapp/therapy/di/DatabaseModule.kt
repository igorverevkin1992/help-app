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
import com.helpapp.therapy.security.CryptoKeyStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        cryptoKeyStore: CryptoKeyStore,
    ): AppDatabase {
        SQLiteDatabase.loadLibs(context)
        val passphrase = cryptoKeyStore.getOrCreateDbPassphrase()
        val passBytes = SQLiteDatabase.getBytes(passphrase)
        val factory = SupportFactory(passBytes)

        return Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            .openHelperFactory(factory)
            .addMigrations(*AppDatabase.ALL_MIGRATIONS)
            .build()
    }

    @Provides fun provideUserContextDao(db: AppDatabase): UserContextDao = db.userContextDao()
    @Provides fun provideSessionDao(db: AppDatabase): SessionDao = db.sessionDao()
    @Provides fun providePieDao(db: AppDatabase): ResponsibilityPieDao = db.responsibilityPieDao()
    @Provides fun provideDereflectionDao(db: AppDatabase): DereflectionDao = db.dereflectionDao()
    @Provides fun provideVitalityDao(db: AppDatabase): VitalityCompassDao = db.vitalityCompassDao()
    @Provides fun provideAssessmentDao(db: AppDatabase): AssessmentDao = db.assessmentDao()
}
