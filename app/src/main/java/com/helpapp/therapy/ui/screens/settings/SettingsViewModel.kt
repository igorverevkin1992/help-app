package com.helpapp.therapy.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helpapp.therapy.data.db.entities.UserContextEntity
import com.helpapp.therapy.data.prefs.ApiKeyStore
import com.helpapp.therapy.data.prefs.AppPreferences
import com.helpapp.therapy.data.repository.SessionRepository
import com.helpapp.therapy.data.repository.UserContextRepository
import com.helpapp.therapy.reminders.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val context: UserContextEntity = UserContextRepository.DEFAULT,
    val apiKey: String = "",
    val saved: Boolean = false,
    val remindersEnabled: Boolean = true,
    val reminderMorningMinutes: Int = 8 * 60,
    val reminderMiddayMinutes: Int = 13 * 60,
    val reminderEveningMinutes: Int = 21 * 60,
    val biometricGateEnabled: Boolean = true,
    val trustedContactName: String = "",
    val trustedContactPhone: String = "",
    val crisisHotline: String = "",
    val retentionDays: Int = 180,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: UserContextRepository,
    private val apiKeyStore: ApiKeyStore,
    private val preferences: AppPreferences,
    private val sessionRepository: SessionRepository,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState(apiKey = apiKeyStore.apiKey))
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observe().collect { ctx ->
                _state.update { it.copy(context = ctx) }
            }
        }
        viewModelScope.launch {
            preferences.snapshot.collect { snap ->
                _state.update {
                    it.copy(
                        remindersEnabled = snap.remindersEnabled,
                        reminderMorningMinutes = snap.reminderMorningMinutes,
                        reminderMiddayMinutes = snap.reminderMiddayMinutes,
                        reminderEveningMinutes = snap.reminderEveningMinutes,
                        biometricGateEnabled = snap.biometricGateEnabled,
                        trustedContactName = snap.trustedContactName,
                        trustedContactPhone = snap.trustedContactPhone,
                        crisisHotline = snap.crisisHotline,
                    )
                }
            }
        }
    }

    fun onBiologicalChange(v: String) = _state.update { it.copy(context = it.context.copy(biologicalTrigger = v)) }
    fun onLimitationChange(v: String) = _state.update { it.copy(context = it.context.copy(objectiveLimitation = v)) }
    fun onSocialChange(v: String) = _state.update { it.copy(context = it.context.copy(socialDuty = v)) }
    fun onGoalChange(v: String) = _state.update { it.copy(context = it.context.copy(transcendentGoal = v)) }
    fun onApiKeyChange(v: String) = _state.update { it.copy(apiKey = v) }
    fun onRemindersEnabledChange(v: Boolean) = _state.update { it.copy(remindersEnabled = v) }
    fun onMorningChange(v: Int) = _state.update { it.copy(reminderMorningMinutes = v.coerceIn(0, 23 * 60 + 59)) }
    fun onMiddayChange(v: Int) = _state.update { it.copy(reminderMiddayMinutes = v.coerceIn(0, 23 * 60 + 59)) }
    fun onEveningChange(v: Int) = _state.update { it.copy(reminderEveningMinutes = v.coerceIn(0, 23 * 60 + 59)) }
    fun onBiometricChange(v: Boolean) = _state.update { it.copy(biometricGateEnabled = v) }
    fun onTrustedNameChange(v: String) = _state.update { it.copy(trustedContactName = v) }
    fun onTrustedPhoneChange(v: String) = _state.update { it.copy(trustedContactPhone = v) }
    fun onHotlineChange(v: String) = _state.update { it.copy(crisisHotline = v) }
    fun onRetentionChange(v: Int) = _state.update { it.copy(retentionDays = v.coerceIn(30, 3650)) }

    fun save() {
        viewModelScope.launch {
            val s = _state.value
            repository.save(s.context)
            apiKeyStore.apiKey = s.apiKey
            preferences.setReminders(
                morning = s.reminderMorningMinutes,
                midday = s.reminderMiddayMinutes,
                evening = s.reminderEveningMinutes,
                enabled = s.remindersEnabled,
            )
            preferences.setBiometric(s.biometricGateEnabled, autoLockSeconds = 120)
            preferences.setTrustedContact(s.trustedContactName, s.trustedContactPhone)
            preferences.setCrisisHotline(s.crisisHotline)
            reminderScheduler.rescheduleFromPreferences()
            _state.update { it.copy(saved = true) }
        }
    }

    fun purgeOldData() {
        viewModelScope.launch {
            sessionRepository.purgeOlderThan(_state.value.retentionDays)
        }
    }
}
