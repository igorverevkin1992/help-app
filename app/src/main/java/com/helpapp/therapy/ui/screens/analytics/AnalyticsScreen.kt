package com.helpapp.therapy.ui.screens.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.helpapp.therapy.ui.components.SparkLine
import com.helpapp.therapy.ui.components.TherapyTopBar
import com.helpapp.therapy.ui.theme.PrimaryAccent
import com.helpapp.therapy.ui.theme.VitalityBright

@Composable
fun AnalyticsScreen(
    onBack: () -> Unit,
    vm: AnalyticsViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = { TherapyTopBar("Analytics", onBack) },
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
            val mids = state.assessments.map { it.midsTotalScore.toFloat() }
            val sffa = state.assessments.map { it.ssfsSffaScore.toFloat() }
            val sfb = state.assessments.map { it.ssfsSfbScore.toFloat() }

            SectionCard(title = "MIDS total (target: decline below ${state.midsThreshold})") {
                if (mids.isEmpty()) {
                    EmptyState("No MIDS snapshots recorded yet.")
                } else {
                    Text(
                        "Latest: ${mids.last().toInt()} · threshold: ${state.midsThreshold}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    SparkLine(
                        points = mids,
                        lineColor = PrimaryAccent,
                        referenceValue = state.midsThreshold.toFloat(),
                        yMax = 72f,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            SectionCard(title = "SSFS · feelings & actions (SFFA — target: rise)") {
                if (sffa.isEmpty()) EmptyState("No SFFA data yet.") else SparkLine(
                    points = sffa,
                    lineColor = VitalityBright,
                    yMax = 32f,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            SectionCard(title = "SSFS · beliefs (SFB — target: rise)") {
                if (sfb.isEmpty()) EmptyState("No SFB data yet.") else SparkLine(
                    points = sfb,
                    lineColor = VitalityBright,
                    yMax = 36f,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            val preVsPost = state.pies.takeLast(20)
            if (preVsPost.isNotEmpty()) {
                SectionCard(title = "Guilt delta per morning session") {
                    val deltas = preVsPost.map {
                        (it.guiltScorePre - it.guiltScorePost).toFloat()
                    }
                    Text(
                        "Positive bars = net guilt reduction. Cumulative sessions: ${preVsPost.size}.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    SparkLine(
                        points = deltas,
                        lineColor = PrimaryAccent,
                        yMin = -10f,
                        yMax = 10f,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            SectionCard(title = "Vitality actions logged") {
                val count = state.vitality.size
                val timeSpent = state.vitality.sumOf { it.actionTimeSpentSec }
                Text(
                    "Total completed: $count · time reinvested in vitality: ${timeSpent / 60} min",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun EmptyState(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
