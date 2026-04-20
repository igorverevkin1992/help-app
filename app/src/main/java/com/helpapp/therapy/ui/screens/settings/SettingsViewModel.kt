package com.helpapp.therapy.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helpapp.therapy.data.db.entities.UserContextEntity
import com.helpapp.therapy.data.prefs.ApiKeyStore
import com.helpapp.therapy.data.repository.UserContextRepository
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
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: UserContextRepository,
    private val apiKeyStore: ApiKeyStore,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState(apiKey = apiKeyStore.apiKey))
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observe().collect { ctx ->
                _state.update { it.copy(context = ctx) }
            }
        }
    }

    fun onBiologicalChange(v: String) = _state.update { it.copy(context = it.context.copy(biologicalTrigger = v)) }
    fun onLimitationChange(v: String) = _state.update { it.copy(context = it.context.copy(objectiveLimitation = v)) }
    fun onSocialChange(v: String) = _state.update { it.copy(context = it.context.copy(socialDuty = v)) }
    fun onGoalChange(v: String) = _state.update { it.copy(context = it.context.copy(transcendentGoal = v)) }
    fun onApiKeyChange(v: String) = _state.update { it.copy(apiKey = v) }

    fun save() {
        viewModelScope.launch {
            val s = _state.value
            repository.save(s.context)
            apiKeyStore.apiKey = s.apiKey
            _state.update { it.copy(saved = true) }
        }
    }
}
