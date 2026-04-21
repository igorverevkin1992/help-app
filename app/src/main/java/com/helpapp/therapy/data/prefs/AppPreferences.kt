package com.helpapp.therapy.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "therapy_prefs")

data class AppPreferencesSnapshot(
    val onboardingCompleted: Boolean,
    val consentAccepted: Boolean,
    val trustedContactName: String,
    val trustedContactPhone: String,
    val crisisHotline: String,
    val reminderMorningMinutes: Int,
    val reminderMiddayMinutes: Int,
    val reminderEveningMinutes: Int,
    val remindersEnabled: Boolean,
    val biometricGateEnabled: Boolean,
    val autoLockSeconds: Int,
    val offlineOnlyMode: Boolean,
)

/**
 * Non-sensitive user preferences. Clinical data and secrets never land here.
 */
@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val store = context.dataStore

    val snapshot: Flow<AppPreferencesSnapshot> = store.data.map { prefs ->
        AppPreferencesSnapshot(
            onboardingCompleted = prefs[KEY_ONBOARDING] ?: false,
            consentAccepted = prefs[KEY_CONSENT] ?: false,
            trustedContactName = prefs[KEY_CONTACT_NAME].orEmpty(),
            trustedContactPhone = prefs[KEY_CONTACT_PHONE].orEmpty(),
            crisisHotline = prefs[KEY_CRISIS_HOTLINE].orEmpty(),
            reminderMorningMinutes = prefs[KEY_REMIND_MORNING] ?: DEFAULT_MORNING,
            reminderMiddayMinutes = prefs[KEY_REMIND_MIDDAY] ?: DEFAULT_MIDDAY,
            reminderEveningMinutes = prefs[KEY_REMIND_EVENING] ?: DEFAULT_EVENING,
            remindersEnabled = prefs[KEY_REMINDERS_ON] ?: true,
            biometricGateEnabled = prefs[KEY_BIOMETRIC] ?: true,
            autoLockSeconds = prefs[KEY_AUTOLOCK] ?: DEFAULT_AUTOLOCK,
            offlineOnlyMode = prefs[KEY_OFFLINE_ONLY] ?: false,
        )
    }

    suspend fun setOnboardingCompleted(value: Boolean) {
        store.edit { it[KEY_ONBOARDING] = value }
    }

    suspend fun setConsent(value: Boolean) {
        store.edit { it[KEY_CONSENT] = value }
    }

    suspend fun setTrustedContact(name: String, phone: String) {
        store.edit {
            it[KEY_CONTACT_NAME] = name
            it[KEY_CONTACT_PHONE] = phone
        }
    }

    suspend fun setCrisisHotline(value: String) {
        store.edit { it[KEY_CRISIS_HOTLINE] = value }
    }

    suspend fun setReminders(morning: Int, midday: Int, evening: Int, enabled: Boolean) {
        store.edit {
            it[KEY_REMIND_MORNING] = morning
            it[KEY_REMIND_MIDDAY] = midday
            it[KEY_REMIND_EVENING] = evening
            it[KEY_REMINDERS_ON] = enabled
        }
    }

    suspend fun setBiometric(enabled: Boolean, autoLockSeconds: Int) {
        store.edit {
            it[KEY_BIOMETRIC] = enabled
            it[KEY_AUTOLOCK] = autoLockSeconds
        }
    }

    suspend fun setOfflineOnly(value: Boolean) {
        store.edit { it[KEY_OFFLINE_ONLY] = value }
    }

    private companion object {
        val KEY_ONBOARDING = booleanPreferencesKey("onboarding_completed")
        val KEY_CONSENT = booleanPreferencesKey("consent_accepted")
        val KEY_CONTACT_NAME = stringPreferencesKey("trusted_contact_name")
        val KEY_CONTACT_PHONE = stringPreferencesKey("trusted_contact_phone")
        val KEY_CRISIS_HOTLINE = stringPreferencesKey("crisis_hotline")
        val KEY_REMIND_MORNING = intPreferencesKey("remind_morning")
        val KEY_REMIND_MIDDAY = intPreferencesKey("remind_midday")
        val KEY_REMIND_EVENING = intPreferencesKey("remind_evening")
        val KEY_REMINDERS_ON = booleanPreferencesKey("reminders_enabled")
        val KEY_BIOMETRIC = booleanPreferencesKey("biometric_enabled")
        val KEY_AUTOLOCK = intPreferencesKey("autolock_seconds")
        val KEY_OFFLINE_ONLY = booleanPreferencesKey("offline_only_mode")

        const val DEFAULT_MORNING = 8 * 60
        const val DEFAULT_MIDDAY = 13 * 60
        const val DEFAULT_EVENING = 21 * 60
        const val DEFAULT_AUTOLOCK = 120
    }
}
