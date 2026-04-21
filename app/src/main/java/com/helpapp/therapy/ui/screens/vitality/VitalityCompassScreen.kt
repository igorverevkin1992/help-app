package com.helpapp.therapy.ui.screens.vitality

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.helpapp.therapy.ui.components.ErrorBox
import com.helpapp.therapy.ui.components.LoadingRow
import com.helpapp.therapy.ui.components.SectionCard
import com.helpapp.therapy.ui.components.TherapyTopBar
import com.helpapp.therapy.ui.theme.SufferingMuted
import com.helpapp.therapy.ui.theme.VitalityBright

@Composable
fun VitalityCompassScreen(
    onBack: () -> Unit,
    onCrisis: () -> Unit,
    vm: VitalityViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(state.crisisTriggered) {
        if (state.crisisTriggered) {
            vm.clearCrisis()
            onCrisis()
        }
    }

    Scaffold(
        topBar = { TherapyTopBar("Vitality compass · evening", onBack) },
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
            SectionCard(title = "Cognitive hook") {
                Text(
                    "Capture the intrusive escapist / danger-seeking fantasy currently " +
                        "exerting pressure. The wording will be reframed as an observed " +
                        "output of your amygdala, not a self-originated directive.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    value = state.hook,
                    onValueChange = vm::onHookChange,
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    onClick = vm::defuse,
                    enabled = !state.loading && state.hook.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (state.loading) "Defusing…" else "Defuse & generate split") }
            }

            if (state.loading) LoadingRow("Claude is running ACT defusion…")
            state.error?.let { ErrorBox(it) }

            state.response?.let { r ->
                if (r.defusedThought.isNotBlank()) {
                    SectionCard(title = "Defused thought") {
                        Text(r.defusedThought, style = MaterialTheme.typography.bodyLarge)
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PathColumn(
                        title = "Suffering",
                        accent = SufferingMuted,
                        items = r.sufferingPath,
                        modifier = Modifier.weight(1f),
                    )
                    PathColumn(
                        title = "Vitality",
                        accent = VitalityBright,
                        items = r.vitalityPath,
                        modifier = Modifier.weight(1f),
                    )
                }

                VitalityTimer(
                    running = state.timerRunning,
                    elapsed = state.elapsedSeconds,
                    target = state.timerTargetSeconds,
                    onStart = vm::startTimer,
                    onStop = { vm.stopTimer(persist = true) },
                )
            }
        }
    }
}

@Composable
private fun PathColumn(
    title: String,
    accent: Color,
    items: List<String>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .background(accent.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, color = accent, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        items.forEach { item ->
            Text("• $item", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun VitalityTimer(
    running: Boolean,
    elapsed: Int,
    target: Int,
    onStart: () -> Unit,
    onStop: () -> Unit,
) {
    SectionCard(title = "Grounding action timer") {
        Text(
            "Begin one vitality micro-task immediately. Close the rumination gestalt.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val stroke = 12f
                    val fraction = (elapsed.toFloat() / target.coerceAtLeast(1)).coerceIn(0f, 1f)
                    drawArc(
                        color = VitalityBright.copy(alpha = 0.2f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = stroke),
                    )
                    drawArc(
                        color = VitalityBright,
                        startAngle = -90f,
                        sweepAngle = 360f * fraction,
                        useCenter = false,
                        style = Stroke(width = stroke),
                    )
                }
                Text(
                    formatTime(elapsed, target),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onStart,
                enabled = !running,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = VitalityBright, contentColor = Color.Black),
            ) { Text("Start 5 min") }
            OutlinedButton(onClick = onStop, enabled = running, modifier = Modifier.weight(1f)) {
                Text("Stop & record")
            }
        }
    }
}

private fun formatTime(elapsed: Int, target: Int): String {
    val remaining = (target - elapsed).coerceAtLeast(0)
    val m = remaining / 60
    val s = remaining % 60
    return "%02d:%02d".format(m, s)
}
