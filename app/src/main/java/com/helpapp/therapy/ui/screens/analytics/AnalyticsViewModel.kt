package com.helpapp.therapy.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helpapp.therapy.data.db.entities.AssessmentEntity
import com.helpapp.therapy.data.db.entities.ResponsibilityPieEntity
import com.helpapp.therapy.data.db.entities.VitalityCompassEntity
import com.helpapp.therapy.data.repository.ModuleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnalyticsUiState(
    val assessments: List<AssessmentEntity> = emptyList(),
    val pies: List<ResponsibilityPieEntity> = emptyList(),
    val vitality: List<VitalityCompassEntity> = emptyList(),
    val midsThreshold: Int = 27,
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repo: ModuleRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AnalyticsUiState())
    val state: StateFlow<AnalyticsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repo.observeAssessments(),
                repo.observeResponsibilityPies(),
                repo.observeVitality(),
            ) { a, p, v -> AnalyticsUiState(a, p, v) }
                .collect { _state.value = it }
        }
    }
}
