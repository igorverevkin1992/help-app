package com.helpapp.therapy.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.helpapp.therapy.ui.components.SectionCard
import com.helpapp.therapy.ui.components.TherapyTopBar

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    vm: SettingsViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = { TherapyTopBar("Context & API", onBack) },
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
            SectionCard(title = "Dynamic context variables") {
                Text(
                    "These four abstract variables are injected into every LLM prompt. " +
                        "Change their values as life conditions shift — no code change required.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                ContextField(
                    label = "Biological trigger",
                    value = state.context.biologicalTrigger,
                    onChange = vm::onBiologicalChange,
                )
                ContextField(
                    label = "Objective limitation",
                    value = state.context.objectiveLimitation,
                    onChange = vm::onLimitationChange,
                )
                ContextField(
                    label = "Social / family duty",
                    value = state.context.socialDuty,
                    onChange = vm::onSocialChange,
                )
                ContextField(
                    label = "Transcendent goal",
                    value = state.context.transcendentGoal,
                    onChange = vm::onGoalChange,
                )
            }

            SectionCard(title = "Claude API key") {
                Text(
                    "Stored locally in EncryptedSharedPreferences. Required for all LLM calls.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    value = state.apiKey,
                    onValueChange = vm::onApiKeyChange,
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

            Button(
                onClick = vm::save,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (state.saved) "Saved" else "Save changes") }
        }
    }
}

@Composable
private fun ContextField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        minLines = 2,
        modifier = Modifier.fillMaxWidth(),
    )
}
