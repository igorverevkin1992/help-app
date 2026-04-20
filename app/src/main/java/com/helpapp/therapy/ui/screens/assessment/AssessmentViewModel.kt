package com.helpapp.therapy.ui.screens.assessment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helpapp.therapy.data.db.entities.AssessmentEntity
import com.helpapp.therapy.data.repository.ModuleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MIDS Part Two — 18 items, 0..4 each. Cut-off = 27.
 * SSFS — 17 items split into SFFA (feelings/actions, 8 items) and SFB
 * (beliefs, 9 items), 1..4 each.
 */
data class AssessmentUiState(
    val midsAnswers: List<Int> = List(MIDS_ITEM_COUNT) { 0 },
    val sffaAnswers: List<Int> = List(SFFA_ITEM_COUNT) { 1 },
    val sfbAnswers: List<Int> = List(SFB_ITEM_COUNT) { 1 },
    val saved: Boolean = false,
) {
    val midsScore: Int get() = midsAnswers.sum()
    val sffaScore: Int get() = sffaAnswers.sum()
    val sfbScore: Int get() = sfbAnswers.sum()

    companion object {
        const val MIDS_ITEM_COUNT = 18
        const val SFFA_ITEM_COUNT = 8
        const val SFB_ITEM_COUNT = 9
        const val MIDS_CUTOFF = 27
    }
}

@HiltViewModel
class AssessmentViewModel @Inject constructor(
    private val repo: ModuleRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AssessmentUiState())
    val state: StateFlow<AssessmentUiState> = _state.asStateFlow()

    fun onMidsAnswer(index: Int, value: Int) = _state.update {
        it.copy(midsAnswers = it.midsAnswers.replace(index, value.coerceIn(0, 4)))
    }

    fun onSffaAnswer(index: Int, value: Int) = _state.update {
        it.copy(sffaAnswers = it.sffaAnswers.replace(index, value.coerceIn(1, 4)))
    }

    fun onSfbAnswer(index: Int, value: Int) = _state.update {
        it.copy(sfbAnswers = it.sfbAnswers.replace(index, value.coerceIn(1, 4)))
    }

    fun save() {
        viewModelScope.launch {
            val s = _state.value
            repo.saveAssessment(
                AssessmentEntity(
                    midsTotalScore = s.midsScore,
                    ssfsSffaScore = s.sffaScore,
                    ssfsSfbScore = s.sfbScore,
                )
            )
            _state.update { it.copy(saved = true) }
        }
    }

    private fun List<Int>.replace(i: Int, v: Int): List<Int> =
        toMutableList().also { it[i] = v }
}
