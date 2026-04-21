package com.helpapp.therapy.ui.screens.export

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.helpapp.therapy.ui.components.ErrorBox
import com.helpapp.therapy.ui.components.LoadingRow
import com.helpapp.therapy.ui.components.SectionCard
import com.helpapp.therapy.ui.components.TherapyTopBar
import java.time.LocalDate

@Composable
fun ExportScreen(
    onBack: () -> Unit,
    vm: ExportViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let(vm::export) }

    Scaffold(
        topBar = { TherapyTopBar("Export clinical records", onBack) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SectionCard(title = "Scope") {
                Text(
                    "Exports all responsibility pies, dereflection entries, vitality " +
                        "compass runs, and psychometric assessments as a single JSON " +
                        "document. Your API key and biographical context variables are " +
                        "NOT included.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Button(
                enabled = !state.busy,
                onClick = {
                    val stamp = LocalDate.now().toString()
                    launcher.launch("cognitive-exoskeleton-$stamp.json")
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Choose location and export") }

            if (state.busy) LoadingRow("Writing encrypted data to the chosen location…")
            state.lastBytes?.let {
                SectionCard(title = "Export successful") {
                    Text("Wrote $it bytes.", style = MaterialTheme.typography.bodyLarge)
                }
            }
            state.error?.let { ErrorBox(it) }
        }
    }
}
