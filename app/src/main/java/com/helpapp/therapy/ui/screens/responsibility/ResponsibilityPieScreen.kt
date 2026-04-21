package com.helpapp.therapy.ui.screens.responsibility

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.helpapp.therapy.domain.prompts.PieResponse
import com.helpapp.therapy.ui.components.ErrorBox
import com.helpapp.therapy.ui.components.LoadingRow
import com.helpapp.therapy.ui.components.PieSegment
import com.helpapp.therapy.ui.components.Pill
import com.helpapp.therapy.ui.components.ResponsibilityPie
import com.helpapp.therapy.ui.components.SectionCard
import com.helpapp.therapy.ui.components.TherapyTopBar
import com.helpapp.therapy.ui.theme.PieBiology
import com.helpapp.therapy.ui.theme.PieControl
import com.helpapp.therapy.ui.theme.PieMedical
import com.helpapp.therapy.ui.theme.PieSocial

@Composable
fun ResponsibilityPieScreen(
    onBack: () -> Unit,
    onCrisis: () -> Unit,
    vm: ResponsibilityViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    var selected by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(state.crisisTriggered) {
        if (state.crisisTriggered) {
            vm.clearCrisis()
            onCrisis()
        }
    }

    Scaffold(
        topBar = { TherapyTopBar("Responsibility pie · morning", onBack) },
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
            SectionCard(title = "1 · Irrational thought") {
                OutlinedTextField(
                    value = state.thought,
                    onValueChange = vm::onThoughtChange,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    label = { Text("Dominant self-blaming thought") },
                )
            }

            SectionCard(title = "2 · Subjective guilt (pre): ${state.guiltPre} / 10") {
                Slider(
                    value = state.guiltPre.toFloat(),
                    onValueChange = { vm.onGuiltPreChange(it.toInt()) },
                    valueRange = 0f..10f,
                    steps = 9,
                )
            }

            Button(
                onClick = vm::deconstruct,
                enabled = !state.loading && state.thought.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (state.loading) "Deconstructing…" else "Deconstruct") }

            if (state.loading) LoadingRow("Claude is distributing responsibility…")
            state.error?.let { ErrorBox(it) }

            state.pie?.let { pie ->
                PieResultBlock(
                    pie = pie,
                    selectedIndex = selected,
                    onSelect = { selected = it },
                )

                SectionCard(title = "3 · Subjective guilt (post)") {
                    val post = state.guiltPost ?: state.guiltPre
                    Text("$post / 10", style = MaterialTheme.typography.titleLarge)
                    Slider(
                        value = post.toFloat(),
                        onValueChange = { vm.onGuiltPostChange(it.toInt()) },
                        valueRange = 0f..10f,
                        steps = 9,
                    )
                    if (state.guiltPost != null) {
                        val delta = state.guiltPre - (state.guiltPost ?: state.guiltPre)
                        Text(
                            "Delta: ${if (delta >= 0) "-$delta" else "+${-delta}"} points",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
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

@Composable
private fun PieResultBlock(
    pie: PieResponse,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit,
) {
    val segments = listOf(
        PieSegment("Biology", pie.biologyPct, PieBiology, pie.tooltips.biology),
        PieSegment("Medical", pie.medicalPct, PieMedical, pie.tooltips.medical),
        PieSegment("Social duty", pie.socialPct, PieSocial, pie.tooltips.social),
        PieSegment("Subjective control", pie.controlPct, PieControl, pie.tooltips.control),
    )

    SectionCard(title = "Responsibility distribution") {
        ResponsibilityPie(
            segments = segments,
            selectedIndex = selectedIndex,
            onSegmentSelected = onSelect,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            segments.forEach { seg ->
                Pill(label = "${seg.label} ${seg.percent}%", color = seg.color)
            }
        }

        val activeTip = selectedIndex?.let { segments.getOrNull(it) }
        if (activeTip != null && activeTip.tooltip.isNotBlank()) {
            Text(
                "${activeTip.label}: ${activeTip.tooltip}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        } else {
            Text(
                "Tap a segment to reveal the clinical rationale.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }

    if (pie.statement.isNotBlank()) {
        SectionCard(title = "Self-forgiveness statement") {
            Text(pie.statement, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
