package com.helpapp.therapy.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.helpapp.therapy.ui.components.SectionCard
import com.helpapp.therapy.ui.navigation.TherapyDestinations
import com.helpapp.therapy.ui.theme.PrimaryAccent
import com.helpapp.therapy.ui.theme.VitalityBright

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenModule: (String) -> Unit,
    vm: HomeViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cognitive Exoskeleton", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = { onOpenModule(TherapyDestinations.ANALYTICS) }) {
                        Icon(Icons.Default.Analytics, contentDescription = "Analytics")
                    }
                    IconButton(onClick = { onOpenModule(TherapyDestinations.SETTINGS) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
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
            SectionCard(title = "Today's neurocognitive cycle") {
                Text(
                    "15–20 minutes, three strict modules. Ordered: rational re-processing " +
                        "of the past, existential reframing of the present, value-aligned " +
                        "action toward the future.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                val session = state.session
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusDot("Morning", session?.morningCompleted == true)
                    StatusDot("Midday", session?.middayCompleted == true)
                    StatusDot("Evening", session?.eveningCompleted == true)
                }
            }

            ModuleEntry(
                title = "1 · Responsibility restructuring",
                subtitle = "Morning · CBT deconstruction of irrational guilt",
                accent = PrimaryAccent,
                onClick = { onOpenModule(TherapyDestinations.RESPONSIBILITY) },
            )
            ModuleEntry(
                title = "2 · Generative dereflection",
                subtitle = "Midday · Logotherapy reframing of operational tasks",
                accent = PrimaryAccent,
                onClick = { onOpenModule(TherapyDestinations.DEREFLECTION) },
            )
            ModuleEntry(
                title = "3 · Vitality compass",
                subtitle = "Evening / panic-trigger · ACT defusion + action timer",
                accent = VitalityBright,
                onClick = { onOpenModule(TherapyDestinations.VITALITY) },
            )

            SectionCard(title = "Weekly psychometric audit") {
                val score = state.latestMidsScore
                Text(
                    "MIDS cut-off for clinically significant moral injury: ${state.midsThreshold}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = if (score == null) "No assessment recorded yet."
                           else "Most recent MIDS total: $score",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Button(
                    onClick = { onOpenModule(TherapyDestinations.ASSESSMENT) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Run assessment") }
            }
        }
    }
}

@Composable
private fun StatusDot(label: String, done: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (done) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (done) VitalityBright else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "  $label",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun ModuleEntry(
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit,
) {
    SectionCard {
        Text(title, style = MaterialTheme.typography.titleLarge, color = accent)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.Black),
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Open module") }
    }
}
