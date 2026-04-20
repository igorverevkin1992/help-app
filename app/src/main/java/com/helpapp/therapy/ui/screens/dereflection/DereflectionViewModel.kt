package com.helpapp.therapy.ui.screens.dereflection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helpapp.therapy.data.db.entities.DereflectionEntity
import com.helpapp.therapy.data.remote.ClaudeService
import com.helpapp.therapy.data.repository.ModuleRepository
import com.helpapp.therapy.data.repository.SessionRepository
import com.helpapp.therapy.data.repository.UserContextRepository
import com.helpapp.therapy.domain.prompts.DereflectionResponse
import com.helpapp.therapy.domain.prompts.LlmJson
import com.helpapp.therapy.domain.prompts.TaskDirectives
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
)

@HiltViewModel
class DereflectionViewModel @Inject constructor(
    private val userContextRepository: UserContextRepository,
    private val claude: ClaudeService,
    private val moduleRepository: ModuleRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DereflectionUiState())
    val state: StateFlow<DereflectionUiState> = _state.asStateFlow()

    fun onTaskInputChange(value: String) = _state.update { it.copy(taskInput = value) }

    fun generate() {
        val s = _state.value
        if (s.taskInput.isBlank() || s.loading) return
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val context = userContextRepository.require()
            val result = claude.ask(
                context = context,
                userPrompt = "Current operational / bureaucratic tasks:\n${s.taskInput}",
                taskDirective = TaskDirectives.DEREFLECTION,
            )
            result.fold(
                onSuccess = { raw ->
                    runCatching { LlmJson.parseDereflection(raw) }.fold(
                        onSuccess = { d -> _state.update { it.copy(loading = false, response = d) } },
                        onFailure = { t -> _state.update { it.copy(loading = false, error = "Parse error: ${t.message}") } },
                    )
                },
                onFailure = { e -> _state.update { it.copy(loading = false, error = e.message ?: "Network error") } },
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
            sessionRepository.markMidday(session.sessionId)
            _state.update { it.copy(saved = true) }
        }
    }
}
