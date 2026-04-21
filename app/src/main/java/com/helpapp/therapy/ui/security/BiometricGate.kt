package com.helpapp.therapy.ui.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helpapp.therapy.data.prefs.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private enum class GateState { Unknown, Required, Unlocked, NotAvailable }

@HiltViewModel
class BiometricGateViewModel @Inject constructor(
    preferences: AppPreferences,
) : ViewModel() {

    private val _enabled = MutableStateFlow<Boolean?>(null)
    val enabled: StateFlow<Boolean?> = _enabled.asStateFlow()

    init {
        viewModelScope.launch {
            val snap = preferences.snapshot.first()
            _enabled.value = snap.biometricGateEnabled
        }
    }
}

@Composable
fun BiometricGate(
    vm: BiometricGateViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    val enabled by vm.enabled.collectAsState()
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    var gate by remember { mutableStateOf(GateState.Unknown) }

    LaunchedEffect(enabled, activity) {
        val flag = enabled
        if (flag == null) return@LaunchedEffect
        if (!flag || activity == null) {
            gate = GateState.Unlocked
            return@LaunchedEffect
        }
        val canAuth = BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        gate = if (canAuth == BiometricManager.BIOMETRIC_SUCCESS) GateState.Required
        else GateState.NotAvailable
    }

    when (gate) {
        GateState.Unknown -> Box(Modifier.fillMaxSize())
        GateState.Unlocked, GateState.NotAvailable -> content()
        GateState.Required -> {
            if (activity == null) {
                content()
                return
            }
            LockedPane(
                onUnlock = {
                    promptBiometric(
                        activity = activity,
                        onSuccess = { gate = GateState.Unlocked },
                        onFatal = { gate = GateState.NotAvailable },
                    )
                },
            )
            LaunchedEffect(activity) {
                promptBiometric(
                    activity = activity,
                    onSuccess = { gate = GateState.Unlocked },
                    onFatal = { gate = GateState.NotAvailable },
                )
            }
        }
    }
}

@Composable
private fun LockedPane(onUnlock: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "Cognitive Exoskeleton is locked.",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                "Authenticate to access your clinical records.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onUnlock) { Text("Unlock") }
        }
    }
}

private fun promptBiometric(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onFatal: () -> Unit,
) {
    val executor = ContextCompat.getMainExecutor(activity)
    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            onSuccess()
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            if (errorCode == BiometricPrompt.ERROR_HW_NOT_PRESENT ||
                errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS ||
                errorCode == BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL
            ) {
                onFatal()
            }
        }
    }
    val prompt = BiometricPrompt(activity, executor, callback)
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Unlock Cognitive Exoskeleton")
        .setSubtitle("Clinical data is protected")
        .setAllowedAuthenticators(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        .build()
    prompt.authenticate(info)
}
