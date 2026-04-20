package com.helpapp.therapy.ui.screens.vitality

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helpapp.therapy.data.db.entities.VitalityCompassEntity
import com.helpapp.therapy.data.remote.ClaudeService
import com.helpapp.therapy.data.repository.ModuleRepository
import com.helpapp.therapy.data.repository.SessionRepository
import com.helpapp.therapy.data.repository.UserContextRepository
import com.helpapp.therapy.domain.prompts.LlmJson
import com.helpapp.therapy.domain.prompts.TaskDirectives
import com.helpapp.therapy.domain.prompts.VitalityResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VitalityUiState(
    val hook: String = "",
    val response: VitalityResponse? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val persistedId: Long? = null,
    val timerRunning: Boolean = false,
    val elapsedSeconds: Int = 0,
    val timerTargetSeconds: Int = 300,
)

@HiltViewModel
class VitalityViewModel @Inject constructor(
    private val userContextRepository: UserContextRepository,
    private val claude: ClaudeService,
    private val moduleRepository: ModuleRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(VitalityUiState())
    val state: StateFlow<VitalityUiState> = _state.asStateFlow()

    private var timerJob: Job? = null

    fun onHookChange(value: String) = _state.update { it.copy(hook = value) }

    fun defuse() {
        val s = _state.value
        if (s.hook.isBlank() || s.loading) return
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val context = userContextRepository.require()
            val result = claude.ask(
                context = context,
                userPrompt = "Intrusive escapist / danger-seeking fantasy:\n${s.hook}",
                taskDirective = TaskDirectives.VITALITY_COMPASS,
            )
            result.fold(
                onSuccess = { raw ->
                    runCatching { LlmJson.parseVitality(raw) }.fold(
                        onSuccess = { v ->
                            val session = sessionRepository.todaySession()
                            val id = moduleRepository.saveVitality(
                                VitalityCompassEntity(
                                    sessionId = session.sessionId,
                                    cognitiveHook = s.hook,
                                    sufferingPath = v.sufferingPath.joinToString("; "),
                                    vitalityPath = v.vitalityPath.joinToString("; "),
                                )
                            )
                            sessionRepository.markEvening(session.sessionId)
                            _state.update { it.copy(loading = false, response = v, persistedId = id) }
                        },
                        onFailure = { t ->
                            _state.update { it.copy(loading = false, error = "Parse error: ${t.message}") }
                        },
                    )
                },
                onFailure = { e -> _state.update { it.copy(loading = false, error = e.message ?: "Network error") } },
            )
        }
    }

    fun startTimer() {
        if (_state.value.timerRunning) return
        _state.update { it.copy(timerRunning = true, elapsedSeconds = 0) }
        timerJob = viewModelScope.launch {
            while (_state.value.elapsedSeconds < _state.value.timerTargetSeconds) {
                delay(1000)
                _state.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
            stopTimer(persist = true)
        }
    }

    fun stopTimer(persist: Boolean = true) {
        timerJob?.cancel()
        timerJob = null
        val seconds = _state.value.elapsedSeconds
        _state.update { it.copy(timerRunning = false) }
        if (persist) {
            val id = _state.value.persistedId ?: return
            viewModelScope.launch { moduleRepository.recordVitalityAction(id, seconds) }
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
