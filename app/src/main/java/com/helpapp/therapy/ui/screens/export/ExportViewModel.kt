package com.helpapp.therapy.ui.screens.export

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helpapp.therapy.domain.ErrorMapper
import com.helpapp.therapy.export.ExportService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExportUiState(
    val busy: Boolean = false,
    val lastBytes: Long? = null,
    val error: String? = null,
)

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val exportService: ExportService,
    private val errorMapper: ErrorMapper,
) : ViewModel() {

    private val _state = MutableStateFlow(ExportUiState())
    val state: StateFlow<ExportUiState> = _state.asStateFlow()

    fun export(target: Uri) {
        _state.update { it.copy(busy = true, error = null, lastBytes = null) }
        viewModelScope.launch {
            runCatching { exportService.export(target) }
                .onSuccess { bytes -> _state.update { it.copy(busy = false, lastBytes = bytes) } }
                .onFailure { t -> _state.update { it.copy(busy = false, error = errorMapper.map(t)) } }
        }
    }
}
