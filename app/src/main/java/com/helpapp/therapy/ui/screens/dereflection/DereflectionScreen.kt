package com.helpapp.therapy.ui.screens.dereflection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.helpapp.therapy.ui.components.ErrorBox
import com.helpapp.therapy.ui.components.LoadingRow
import com.helpapp.therapy.ui.components.SectionCard
import com.helpapp.therapy.ui.components.TherapyTopBar

@Composable
fun DereflectionScreen(
    onBack: () -> Unit,
    onCrisis: () -> Unit,
    vm: DereflectionViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(state.crisisTriggered) {
        if (state.crisisTriggered) {
            vm.clearCrisis()
            onCrisis()
        }
    }

    Scaffold(
        topBar = { TherapyTopBar("Dereflection · midday", onBack) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionCard(title = "Task audit") {
                Text(
                    "List today's mundane, bureaucratic, operational, or logistical tasks — " +
                        "one per line.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    value = state.taskInput,
                    onValueChange = vm::onTaskInputChange,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                )
                Button(
                    onClick = vm::requestPatternInterrupt,
                    enabled = !state.loading && state.taskInput.isNotBlank() && !state.patternInterrupt,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (state.loading) "Reframing…" else "Reframe as generative combat") }
            }

            if (state.patternInterrupt) {
                var seconds by remember { mutableStateOf(10) }
                LaunchedEffect(state.patternInterrupt) {
                    seconds = 10
                    while (seconds > 0) {
                        delay(1000)
                        seconds -= 1
                    }
                    vm.generate()
                }
                SectionCard(title = "Pattern interrupt · $seconds s") {
                    Text(
                        "Hold. The app is imposing a mandatory ten-second pause before the " +
                            "reframe is generated. Use this window to notice the urge to skip " +
                            "the audit.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            if (state.loading) LoadingRow("Claude is reframing operational tasks…")
            state.error?.let { ErrorBox(it) }

            state.response?.let { r ->
                SectionCard(title = "Mundane perception → generative equivalent") {
                    r.reframingTable.forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Mundane",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(row.mundane, style = MaterialTheme.typography.bodyMedium)
                            }
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Generative combat",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    row.generativeEquivalent,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
                        )
                    }
                }

                if (r.anchorStatement.isNotBlank()) {
                    SectionCard(title = "Existential anchor") {
                        Text(r.anchorStatement, style = MaterialTheme.typography.bodyLarge)
                    }
                }

                Button(
                    onClick = vm::save,
                    enabled = !state.saved,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (state.saved) "Saved for today" else "Record this session") }
            }
        }
    }
}
