package com.helpapp.therapy.ui.screens.assessment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.helpapp.therapy.ui.components.SectionCard
import com.helpapp.therapy.ui.components.TherapyTopBar

@Composable
fun AssessmentScreen(
    onBack: () -> Unit,
    vm: AssessmentViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = { TherapyTopBar("Weekly psychometric audit", onBack) },
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
            SectionCard(title = "MIDS · 18 items · score 0..4") {
                Text(
                    "Moral Injury and Distress Scale — Part Two. Clinical cut-off = ${AssessmentUiState.MIDS_CUTOFF}. " +
                        "Rate each item in the past week.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                state.midsAnswers.forEachIndexed { index, value ->
                    LikertRow(
                        label = AssessmentItems.MIDS[index],
                        value = value,
                        range = 0..4,
                        onChange = { vm.onMidsAnswer(index, it) },
                    )
                }
                Text(
                    "MIDS total: ${state.midsScore}",
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            SectionCard(title = "SSFS · SFFA · 8 items · score 1..4") {
                state.sffaAnswers.forEachIndexed { index, value ->
                    LikertRow(
                        label = AssessmentItems.SFFA[index],
                        value = value,
                        range = 1..4,
                        onChange = { vm.onSffaAnswer(index, it) },
                    )
                }
                Text("SFFA total: ${state.sffaScore}", style = MaterialTheme.typography.titleLarge)
            }

            SectionCard(title = "SSFS · SFB · 9 items · score 1..4") {
                state.sfbAnswers.forEachIndexed { index, value ->
                    LikertRow(
                        label = AssessmentItems.SFB[index],
                        value = value,
                        range = 1..4,
                        onChange = { vm.onSfbAnswer(index, it) },
                    )
                }
                Text("SFB total: ${state.sfbScore}", style = MaterialTheme.typography.titleLarge)
            }

            Button(
                onClick = vm::save,
                enabled = !state.saved,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (state.saved) "Assessment saved" else "Save assessment") }
        }
    }
}

@Composable
private fun LikertRow(
    label: String,
    value: Int,
    range: IntRange,
    onChange: (Int) -> Unit,
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            range.forEach { option ->
                FilterChip(
                    selected = value == option,
                    onClick = { onChange(option) },
                    label = { Text("$option") },
                )
            }
        }
    }
}
