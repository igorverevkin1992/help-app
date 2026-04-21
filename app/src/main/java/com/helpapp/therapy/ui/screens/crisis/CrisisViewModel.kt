package com.helpapp.therapy.ui.screens.crisis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helpapp.therapy.data.prefs.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CrisisUiState(
    val crisisHotline: String = "",
    val trustedContactName: String = "",
    val trustedContactPhone: String = "",
)

@HiltViewModel
class CrisisViewModel @Inject constructor(
    preferences: AppPreferences,
) : ViewModel() {

    private val _state = MutableStateFlow(CrisisUiState())
    val state: StateFlow<CrisisUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            preferences.snapshot.collect { snap ->
                _state.value = CrisisUiState(
                    crisisHotline = snap.crisisHotline,
                    trustedContactName = snap.trustedContactName,
                    trustedContactPhone = snap.trustedContactPhone,
                )
            }
        }
    }
}
