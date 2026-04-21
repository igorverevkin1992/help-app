package com.helpapp.therapy.ui.screens.responsibility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helpapp.therapy.data.db.entities.ResponsibilityPieEntity
import com.helpapp.therapy.data.remote.GeminiService
import com.helpapp.therapy.data.repository.ModuleRepository
import com.helpapp.therapy.data.repository.SessionRepository
import com.helpapp.therapy.data.repository.UserContextRepository
import com.helpapp.therapy.domain.ErrorMapper
import com.helpapp.therapy.domain.prompts.PieResponse
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

data class ResponsibilityUiState(
    val thought: String = "",
    val guiltPre: Int = 5,
    val guiltPost: Int? = null,
    val pie: PieResponse? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
    val crisisTriggered: Boolean = false,
)

@HiltViewModel
class ResponsibilityViewModel @Inject constructor(
    private val userContextRepository: UserContextRepository,
    private val gemini: GeminiService,
    private val moduleRepository: ModuleRepository,
    private val sessionRepository: SessionRepository,
    private val crisisScreener: CrisisScreener,
    private val errorMapper: ErrorMapper,
) : ViewModel() {

    private val _state = MutableStateFlow(ResponsibilityUiState())
    val state: StateFlow<ResponsibilityUiState> = _state.asStateFlow()

    fun onThoughtChange(value: String) = _state.update { it.copy(thought = value) }
    fun onGuiltPreChange(value: Int) = _state.update { it.copy(guiltPre = value) }
    fun onGuiltPostChange(value: Int) = _state.update { it.copy(guiltPost = value) }
    fun clearCrisis() = _state.update { it.copy(crisisTriggered = false) }

    fun deconstruct() {
        val s = _state.value
        if (s.thought.isBlank() || s.loading) return
        if (crisisScreener.screen(s.thought).level == CrisisLevel.CRITICAL) {
            _state.update { it.copy(crisisTriggered = true, error = null) }
            return
        }
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val context = userContextRepository.require()
            val result = gemini.runTool(
                context = context,
                userPrompt = """
                    |Automatic self-blaming thought of the moment:
                    |"${s.thought}"
                    |
                    |Current subjective guilt score (0-10): ${s.guiltPre}
                """.trimMargin(),
                taskDirective = TaskDirectives.RESPONSIBILITY_PIE,
                tool = ToolDefinitions.RESPONSIBILITY_PIE,
            )
            result.fold(
                onSuccess = { input ->
                    runCatching { ToolOutputs.parsePie(input) }.fold(
                        onSuccess = { pie -> _state.update { it.copy(loading = false, pie = normalize(pie)) } },
                        onFailure = { t -> _state.update { it.copy(loading = false, error = errorMapper.map(t)) } },
                    )
                },
                onFailure = { e -> _state.update { it.copy(loading = false, error = errorMapper.map(e)) } },
            )
        }
    }

    fun save() {
        val s = _state.value
        val pie = s.pie ?: return
        viewModelScope.launch {
            val session = sessionRepository.todaySession()
            moduleRepository.saveResponsibilityPie(
                ResponsibilityPieEntity(
                    sessionId = session.sessionId,
                    irrationalThought = s.thought,
                    guiltScorePre = s.guiltPre,
                    guiltScorePost = s.guiltPost ?: s.guiltPre,
                    biologyWeightPct = pie.biologyPct,
                    medicalWeightPct = pie.medicalPct,
                    socialWeightPct = pie.socialPct,
                    controlWeightPct = pie.controlPct,
                    statementGenerated = pie.statement,
                )
            )
            sessionRepository.markResponsibility(session.sessionId)
            _state.update { it.copy(saved = true) }
        }
    }

    private fun normalize(p: PieResponse): PieResponse {
        val sum = p.biologyPct + p.medicalPct + p.socialPct + p.controlPct
        if (sum == 100 || sum <= 0) return p
        val scale = 100.0 / sum
        val bio = (p.biologyPct * scale).toInt()
        val med = (p.medicalPct * scale).toInt()
        val soc = (p.socialPct * scale).toInt()
        val control = 100 - bio - med - soc
        return p.copy(
            biologyPct = bio,
            medicalPct = med,
            socialPct = soc,
            controlPct = control,
        )
    }
}
