package com.helpapp.therapy.ui.screens.crisis

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.helpapp.therapy.ui.components.SectionCard
import com.helpapp.therapy.ui.components.TherapyTopBar

@Composable
fun CrisisScreen(
    onBack: () -> Unit,
    vm: CrisisViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = { TherapyTopBar("Immediate support", onBack) },
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
            SectionCard(title = "Pause") {
                Text(
                    "Your input contained language that routes you to human support. " +
                        "The app is not a substitute for a clinician in acute crisis. " +
                        "Contact one of the resources below before returning to the module.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            if (state.crisisHotline.isNotBlank()) {
                SectionCard(title = "Crisis hotline") {
                    Text(state.crisisHotline, style = MaterialTheme.typography.titleLarge)
                    Button(
                        onClick = { dial(context, state.crisisHotline) },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Call hotline") }
                }
            }

            if (state.trustedContactPhone.isNotBlank()) {
                SectionCard(title = "Trusted contact") {
                    Text(
                        text = state.trustedContactName.ifBlank { "Contact" } +
                            " · ${state.trustedContactPhone}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Button(
                        onClick = { dial(context, state.trustedContactPhone) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Call trusted contact") }
                }
            }

            SectionCard(title = "Grounding") {
                Text(
                    "Before returning to the app, complete two minutes of paced breathing " +
                        "(4 seconds in, 6 seconds out) and name five objects in the room " +
                        "that are physically in front of you.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

private fun dial(context: android.content.Context, phone: String) {
    val intent = Intent(Intent.ACTION_DIAL).apply {
        data = Uri.parse("tel:${phone.filter { it.isDigit() || it == '+' }}")
    }
    runCatching { context.startActivity(intent) }
}
