package com.helpapp.therapy

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.helpapp.therapy.ui.navigation.TherapyNavHost
import com.helpapp.therapy.ui.security.BiometricGate
import com.helpapp.therapy.ui.theme.TherapyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Block screenshots and disable content preview in the recents switcher —
        // clinical PHI must never leak to OS-level caches.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE,
        )
        enableEdgeToEdge()
        setContent { TherapyRoot() }
    }
}

@Composable
private fun TherapyRoot() {
    TherapyTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            BiometricGate {
                TherapyNavHost()
            }
        }
    }
}
