package com.helpapp.therapy.ui.screens.dereflection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helpapp.therapy.data.db.entities.DereflectionEntity
import com.helpapp.therapy.data.remote.GeminiService
import com.helpapp.therapy.data.repository.ModuleRepository
import com.helpapp.therapy.data.repository.SessionRepository
import com.helpapp.therapy.data.repository.UserContextRepository
import com.helpapp.therapy.domain.ErrorMapper
import com.helpapp.therapy.domain.prompts.DereflectionResponse
import com.helpapp.therapy.domain.prompts.TaskDirectives
import com.helpapp.therapy.domain.prompts.ToolDefinitions
import com.helpapp.therapy.domain.prompts.ToolOutputs
import com.helpapp.therapy.domain.safety.CrisisLevel
import com.helpapp.therapy.domain.safety.CrisisScreener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DereflectionUiState(
    val taskInput: String = "",
    val response: DereflectionResponse? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
    val crisisTriggered: Boolean = false,
    val patternInterrupt: Boolean = false,
)

@HiltViewModel
class DereflectionViewModel @Inject constructor(
    private val userContextRepository: UserContextRepository,
    private val gemini: GeminiService,
    private val moduleRepository: ModuleRepository,
    private val sessionRepository: SessionRepository,
    private val crisisScreener: CrisisScreener,
    private val errorMapper: ErrorMapper,
) : ViewModel() {

    private val _state = MutableStateFlow(DereflectionUiState())
    val state: StateFlow<DereflectionUiState> = _state.asStateFlow()

    fun onTaskInputChange(value: String) = _state.update { it.copy(taskInput = value) }
    fun clearCrisis() = _state.update { it.copy(crisisTriggered = false) }
    fun completePatternInterrupt() = _state.update { it.copy(patternInterrupt = false) }

    fun requestPatternInterrupt() {
        val s = _state.value
        if (s.taskInput.isBlank() || s.loading) return
        if (crisisScreener.screen(s.taskInput).level == CrisisLevel.CRITICAL) {
            _state.update { it.copy(crisisTriggered = true, error = null) }
            return
        }
        _state.update { it.copy(patternInterrupt = true, error = null) }
    }

    fun generate() {
        val s = _state.value
        if (s.taskInput.isBlank() || s.loading) return
        if (crisisScreener.screen(s.taskInput).level == CrisisLevel.CRITICAL) {
            _state.update { it.copy(crisisTriggered = true, error = null) }
            return
        }
        _state.update { it.copy(loading = true, error = null, patternInterrupt = false) }
        viewModelScope.launch {
            val context = userContextRepository.require()
            val result = gemini.runTool(
                context = context,
                userPrompt = "Current operational / bureaucratic tasks:\n${s.taskInput}",
                taskDirective = TaskDirectives.DEREFLECTION,
                tool = ToolDefinitions.DEREFLECTION,
            )
            result.fold(
                onSuccess = { input ->
                    runCatching { ToolOutputs.parseDereflection(input) }.fold(
                        onSuccess = { d -> _state.update { it.copy(loading = false, response = d) } },
                        onFailure = { t -> _state.update { it.copy(loading = false, error = errorMapper.map(t)) } },
                    )
                },
                onFailure = { e -> _state.update { it.copy(loading = false, error = errorMapper.map(e)) } },
            )
        }
    }

    fun save() {
        val s = _state.value
        val r = s.response ?: return
        viewModelScope.launch {
            val session = sessionRepository.todaySession()
            val reframing = r.reframingTable.joinToString("\n") { "${it.mundane} → ${it.generativeEquivalent}" }
            moduleRepository.saveDereflection(
                DereflectionEntity(
                    sessionId = session.sessionId,
                    taskList = s.taskInput,
                    generativeReframing = reframing,
                    anchorStatement = r.anchorStatement,
                )
            )
            sessionRepository.markDereflection(session.sessionId)
            _state.update { it.copy(saved = true) }
        }
    }
}
