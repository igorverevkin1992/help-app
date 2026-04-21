package com.helpapp.therapy.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.helpapp.therapy.ui.components.ErrorBox
import com.helpapp.therapy.ui.components.SectionCard
import com.helpapp.therapy.ui.components.TherapyTopBar

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    vm: OnboardingViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(state.step) {
        if (state.step == OnboardingStep.Done) onComplete()
    }

    Scaffold(
        topBar = { TherapyTopBar("Onboarding") },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            when (state.step) {
                OnboardingStep.Consent -> ConsentStep(
                    accepted = state.consentAccepted,
                    onAcceptedChange = vm::setConsent,
                )
                OnboardingStep.ApiKey -> ApiKeyStep(
                    apiKey = state.apiKey,
                    onChange = vm::setApiKey,
                )
                OnboardingStep.Context -> ContextStep(
                    state = state,
                    onBiological = vm::setBiological,
                    onLimitation = vm::setLimitation,
                    onSocial = vm::setSocial,
                    onGoal = vm::setGoal,
                )
                OnboardingStep.TrustedContact -> TrustedContactStep(
                    state = state,
                    onName = vm::setTrustedName,
                    onPhone = vm::setTrustedPhone,
                    onHotline = vm::setHotline,
                )
                OnboardingStep.Done -> Text("Completing setup…")
            }

            state.error?.let { ErrorBox(it) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = vm::back,
                    enabled = state.step != OnboardingStep.Consent && !state.saving,
                ) { Text("Back") }
                Button(
                    onClick = vm::next,
                    enabled = !state.saving,
                ) { Text(if (state.step == OnboardingStep.TrustedContact) "Finish" else "Next") }
            }
        }
    }
}

@Composable
private fun ConsentStep(accepted: Boolean, onAcceptedChange: (Boolean) -> Unit) {
    SectionCard(title = "Terms of use") {
        Text(
            "Cognitive Exoskeleton is a self-help instrument. It is not a medical " +
                "device, not a substitute for a licensed clinician, and not a crisis " +
                "service. In acute distress — suicidal or homicidal ideation, psychosis, " +
                "medical emergency — stop using this app and call a professional or " +
                "emergency services.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            "All clinical entries are stored only on this device, inside an encrypted " +
                "database (SQLCipher) protected by a key held in the Android Keystore. " +
                "Your Claude API key lives in EncryptedSharedPreferences and is used " +
                "exclusively to talk to api.anthropic.com.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = accepted, onCheckedChange = onAcceptedChange)
            Text("I understand and accept these conditions.")
        }
    }
}

@Composable
private fun ApiKeyStep(apiKey: String, onChange: (String) -> Unit) {
    SectionCard(title = "Claude API key") {
        Text(
            "Obtain a key from console.anthropic.com. It is stored locally and never " +
                "logged. Leave empty and the app will refuse to call the model.",
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            value = apiKey,
            onValueChange = onChange,
            label = { Text("sk-ant-…") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ContextStep(
    state: OnboardingUiState,
    onBiological: (String) -> Unit,
    onLimitation: (String) -> Unit,
    onSocial: (String) -> Unit,
    onGoal: (String) -> Unit,
) {
    SectionCard(title = "Context variables") {
        Text(
            "Four abstract variables drive every LLM prompt. They are the only " +
                "biographical input the system ever sees.",
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            value = state.context.biologicalTrigger,
            onValueChange = onBiological,
            label = { Text("Biological trigger") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.context.objectiveLimitation,
            onValueChange = onLimitation,
            label = { Text("Objective limitation") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.context.socialDuty,
            onValueChange = onSocial,
            label = { Text("Social / family duty") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.context.transcendentGoal,
            onValueChange = onGoal,
            label = { Text("Transcendent goal") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun TrustedContactStep(
    state: OnboardingUiState,
    onName: (String) -> Unit,
    onPhone: (String) -> Unit,
    onHotline: (String) -> Unit,
) {
    SectionCard(title = "Trusted contact & hotline") {
        Text(
            "If the on-device crisis screener detects acute language, the app will " +
                "offer a one-tap call to the contact below. Leave empty to skip.",
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            value = state.trustedContactName,
            onValueChange = onName,
            label = { Text("Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.trustedContactPhone,
            onValueChange = onPhone,
            label = { Text("Phone (+country code)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.crisisHotline,
            onValueChange = onHotline,
            label = { Text("Crisis hotline (local)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
