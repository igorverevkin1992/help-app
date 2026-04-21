package com.helpapp.therapy.security

import com.helpapp.therapy.data.prefs.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Foreground session lock shared between the Application lifecycle observer
 * and the Compose [com.helpapp.therapy.ui.security.BiometricGate]. The state
 * is volatile — never persisted — so unlock does not survive process death.
 *
 * Semantics:
 *  - App starts cold → [locked] is true. Gate runs biometric prompt.
 *  - User authenticates → [unlock] sets [locked] false.
 *  - App goes to background → [markBackgrounded] stamps the wall time.
 *  - App returns to foreground → [evaluateOnForeground] compares elapsed
 *    against the user's autoLockSeconds preference and re-locks if exceeded.
 */
@Singleton
class SessionLock @Inject constructor(
    private val preferences: AppPreferences,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _locked = MutableStateFlow(true)
    val locked: StateFlow<Boolean> = _locked.asStateFlow()

    @Volatile private var backgroundedAt: Long = 0L

    fun unlock() {
        _locked.value = false
    }

    fun markBackgrounded() {
        backgroundedAt = System.currentTimeMillis()
    }

    fun evaluateOnForeground() {
        if (_locked.value) return
        val stamp = backgroundedAt
        if (stamp == 0L) return
        val elapsedSec = (System.currentTimeMillis() - stamp) / 1000L
        scope.launch {
            val snap = preferences.snapshot.first()
            if (!snap.biometricGateEnabled) return@launch
            if (elapsedSec >= snap.autoLockSeconds) {
                _locked.value = true
            }
        }
    }
}
