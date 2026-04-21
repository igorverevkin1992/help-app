package com.helpapp.therapy.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helpapp.therapy.data.db.entities.UserContextEntity
import com.helpapp.therapy.data.prefs.ApiKeyStore
import com.helpapp.therapy.data.prefs.AppPreferences
import com.helpapp.therapy.data.repository.UserContextRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class OnboardingStep { Consent, ApiKey, Context, TrustedContact, Done }

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.Consent,
    val consentAccepted: Boolean = false,
    val apiKey: String = "",
    val context: UserContextEntity = UserContextRepository.DEFAULT,
    val trustedContactName: String = "",
    val trustedContactPhone: String = "",
    val crisisHotline: String = "",
    val saving: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val apiKeyStore: ApiKeyStore,
    private val preferences: AppPreferences,
    private val userContextRepository: UserContextRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    fun setConsent(value: Boolean) = _state.update { it.copy(consentAccepted = value) }
    fun setApiKey(value: String) = _state.update { it.copy(apiKey = value) }
    fun setBiological(v: String) = _state.update { it.copy(context = it.context.copy(biologicalTrigger = v)) }
    fun setLimitation(v: String) = _state.update { it.copy(context = it.context.copy(objectiveLimitation = v)) }
    fun setSocial(v: String) = _state.update { it.copy(context = it.context.copy(socialDuty = v)) }
    fun setGoal(v: String) = _state.update { it.copy(context = it.context.copy(transcendentGoal = v)) }
    fun setTrustedName(v: String) = _state.update { it.copy(trustedContactName = v) }
    fun setTrustedPhone(v: String) = _state.update { it.copy(trustedContactPhone = v) }
    fun setHotline(v: String) = _state.update { it.copy(crisisHotline = v) }

    fun next() {
        val s = _state.value
        val nextStep = when (s.step) {
            OnboardingStep.Consent -> {
                if (!s.consentAccepted) {
                    _state.update { it.copy(error = "You must accept the terms to continue.") }
                    return
                }
                OnboardingStep.ApiKey
            }
            OnboardingStep.ApiKey -> {
                if (s.apiKey.isBlank()) {
                    _state.update { it.copy(error = "Enter your Claude API key.") }
                    return
                }
                OnboardingStep.Context
            }
            OnboardingStep.Context -> {
                if (s.context.biologicalTrigger.isBlank() ||
                    s.context.objectiveLimitation.isBlank() ||
                    s.context.socialDuty.isBlank() ||
                    s.context.transcendentGoal.isBlank()
                ) {
                    _state.update { it.copy(error = "All four context fields are required.") }
                    return
                }
                OnboardingStep.TrustedContact
            }
            OnboardingStep.TrustedContact -> {
                finalize()
                return
            }
            OnboardingStep.Done -> return
        }
        _state.update { it.copy(step = nextStep, error = null) }
    }

    fun back() {
        val s = _state.value
        val prev = when (s.step) {
            OnboardingStep.ApiKey -> OnboardingStep.Consent
            OnboardingStep.Context -> OnboardingStep.ApiKey
            OnboardingStep.TrustedContact -> OnboardingStep.Context
            else -> return
        }
        _state.update { it.copy(step = prev, error = null) }
    }

    private fun finalize() {
        val s = _state.value
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            userContextRepository.save(s.context)
            apiKeyStore.apiKey = s.apiKey
            preferences.setConsent(true)
            preferences.setTrustedContact(s.trustedContactName, s.trustedContactPhone)
            if (s.crisisHotline.isNotBlank()) preferences.setCrisisHotline(s.crisisHotline)
            preferences.setOnboardingCompleted(true)
            _state.update { it.copy(saving = false, step = OnboardingStep.Done) }
        }
    }
}
