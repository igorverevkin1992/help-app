package com.helpapp.therapy.ui.screens.vitality

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helpapp.therapy.data.db.entities.VitalityCompassEntity
import com.helpapp.therapy.data.remote.GeminiService
import com.helpapp.therapy.data.repository.ModuleRepository
import com.helpapp.therapy.data.repository.SessionRepository
import com.helpapp.therapy.data.repository.UserContextRepository
import com.helpapp.therapy.domain.ErrorMapper
import com.helpapp.therapy.domain.prompts.TaskDirectives
import com.helpapp.therapy.domain.prompts.ToolDefinitions
import com.helpapp.therapy.domain.prompts.ToolOutputs
import com.helpapp.therapy.domain.prompts.VitalityResponse
import com.helpapp.therapy.domain.safety.CrisisLevel
import com.helpapp.therapy.domain.safety.CrisisScreener
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
    val crisisTriggered: Boolean = false,
)

@HiltViewModel
class VitalityViewModel @Inject constructor(
    private val userContextRepository: UserContextRepository,
    private val gemini: GeminiService,
    private val moduleRepository: ModuleRepository,
    private val sessionRepository: SessionRepository,
    private val crisisScreener: CrisisScreener,
    private val errorMapper: ErrorMapper,
    private val savedState: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(
        VitalityUiState(
            persistedId = savedState[KEY_PERSISTED_ID],
            elapsedSeconds = savedState.get<Int>(KEY_ELAPSED) ?: 0,
        )
    )
    val state: StateFlow<VitalityUiState> = _state.asStateFlow()

    private var timerJob: Job? = null

    fun onHookChange(value: String) = _state.update { it.copy(hook = value) }
    fun clearCrisis() = _state.update { it.copy(crisisTriggered = false) }

    fun defuse() {
        val s = _state.value
        if (s.hook.isBlank() || s.loading) return
        if (crisisScreener.screen(s.hook).level == CrisisLevel.CRITICAL) {
            _state.update { it.copy(crisisTriggered = true, error = null) }
            return
        }
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val context = userContextRepository.require()
            val result = gemini.runTool(
                context = context,
                userPrompt = "Intrusive escapist / danger-seeking fantasy:\n${s.hook}",
                taskDirective = TaskDirectives.VITALITY_COMPASS,
                tool = ToolDefinitions.VITALITY_COMPASS,
            )
            result.fold(
                onSuccess = { input ->
                    runCatching { ToolOutputs.parseVitality(input) }.fold(
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
                            sessionRepository.markVitality(session.sessionId)
                            savedState[KEY_PERSISTED_ID] = id
                            _state.update { it.copy(loading = false, response = v, persistedId = id) }
                        },
                        onFailure = { t -> _state.update { it.copy(loading = false, error = errorMapper.map(t)) } },
                    )
                },
                onFailure = { e -> _state.update { it.copy(loading = false, error = errorMapper.map(e)) } },
            )
        }
    }

    fun startTimer() {
        if (_state.value.timerRunning) return
        _state.update { it.copy(timerRunning = true, elapsedSeconds = 0) }
        savedState[KEY_ELAPSED] = 0
        timerJob = viewModelScope.launch {
            while (_state.value.elapsedSeconds < _state.value.timerTargetSeconds) {
                delay(1000)
                val next = _state.value.elapsedSeconds + 1
                _state.update { it.copy(elapsedSeconds = next) }
                savedState[KEY_ELAPSED] = next
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

    private companion object {
        const val KEY_PERSISTED_ID = "vitality.persistedId"
        const val KEY_ELAPSED = "vitality.elapsed"
    }
}
