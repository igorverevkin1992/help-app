package com.helpapp.therapy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.helpapp.therapy.ui.navigation.TherapyNavHost
import com.helpapp.therapy.ui.theme.TherapyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { TherapyRoot() }
    }
}

@Composable
private fun TherapyRoot() {
    TherapyTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            TherapyNavHost()
        }
    }
}
