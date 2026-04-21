package com.helpapp.therapy.ui.screens.settings

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.helpapp.therapy.ui.components.SectionCard
import com.helpapp.therapy.ui.components.TherapyTopBar

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenExport: () -> Unit,
    vm: SettingsViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = { TherapyTopBar("Context & security", onBack) },
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
                ContextField("Biological trigger", state.context.biologicalTrigger, vm::onBiologicalChange)
                ContextField("Objective limitation", state.context.objectiveLimitation, vm::onLimitationChange)
                ContextField("Social / family duty", state.context.socialDuty, vm::onSocialChange)
                ContextField("Transcendent goal", state.context.transcendentGoal, vm::onGoalChange)
            }

            SectionCard(title = "Gemini API key") {
                Text(
                    "Stored locally in EncryptedSharedPreferences. Required for all LLM calls.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    value = state.apiKey,
                    onValueChange = vm::onApiKeyChange,
                    label = { Text("AIza…") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            SectionCard(title = "Daily reminders") {
                SwitchRow(
                    label = "Enable reminders",
                    checked = state.remindersEnabled,
                    onChange = vm::onRemindersEnabledChange,
                )
                TimeRow("Responsibility", state.reminderMorningMinutes, vm::onMorningChange)
                TimeRow("Dereflection", state.reminderMiddayMinutes, vm::onMiddayChange)
                TimeRow("Vitality", state.reminderEveningMinutes, vm::onEveningChange)
            }

            SectionCard(title = "Biometric gate") {
                Text(
                    "Require biometric or device-credential authentication before the app " +
                        "unlocks. The gate falls through if the device has no enrolled credentials.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                SwitchRow(
                    label = "Require biometric on launch",
                    checked = state.biometricGateEnabled,
                    onChange = vm::onBiometricChange,
                )
            }

            SectionCard(title = "Crisis contacts") {
                OutlinedTextField(
                    value = state.trustedContactName,
                    onValueChange = vm::onTrustedNameChange,
                    label = { Text("Trusted contact name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.trustedContactPhone,
                    onValueChange = vm::onTrustedPhoneChange,
                    label = { Text("Trusted contact phone") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.crisisHotline,
                    onValueChange = vm::onHotlineChange,
                    label = { Text("Crisis hotline") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            SectionCard(title = "Retention & export") {
                Text(
                    "Retain entries for ${state.retentionDays} days. Older records will be purged from the " +
                        "encrypted database.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Slider(
                    value = state.retentionDays.toFloat(),
                    onValueChange = { vm.onRetentionChange(it.toInt()) },
                    valueRange = 30f..730f,
                    steps = 10,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = vm::purgeOldData,
                        modifier = Modifier.weight(1f),
                    ) { Text("Purge old data") }
                    OutlinedButton(
                        onClick = onOpenExport,
                        modifier = Modifier.weight(1f),
                    ) { Text("Export as JSON") }
                }
            }

            Button(
                onClick = vm::save,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (state.saved) "Saved" else "Save changes") }
        }
    }
}

@Composable
private fun ContextField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        minLines = 2,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun TimeRow(label: String, minutes: Int, onChange: (Int) -> Unit) {
    val hours = minutes / 60
    val mins = minutes % 60
    Column {
        Text(
            "$label · %02d:%02d".format(hours, mins),
            style = MaterialTheme.typography.bodyLarge,
        )
        Slider(
            value = minutes.toFloat(),
            onValueChange = { onChange(it.toInt()) },
            valueRange = 0f..(23 * 60 + 59).toFloat(),
        )
    }
}
