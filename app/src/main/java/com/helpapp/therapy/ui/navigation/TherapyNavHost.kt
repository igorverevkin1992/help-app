package com.helpapp.therapy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.helpapp.therapy.data.prefs.AppPreferences
import com.helpapp.therapy.ui.screens.analytics.AnalyticsScreen
import com.helpapp.therapy.ui.screens.assessment.AssessmentScreen
import com.helpapp.therapy.ui.screens.crisis.CrisisScreen
import com.helpapp.therapy.ui.screens.dereflection.DereflectionScreen
import com.helpapp.therapy.ui.screens.export.ExportScreen
import com.helpapp.therapy.ui.screens.home.HomeScreen
import com.helpapp.therapy.ui.screens.onboarding.OnboardingScreen
import com.helpapp.therapy.ui.screens.responsibility.ResponsibilityPieScreen
import com.helpapp.therapy.ui.screens.settings.SettingsScreen
import com.helpapp.therapy.ui.screens.vitality.VitalityCompassScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NavBootstrapViewModel @Inject constructor(
    preferences: AppPreferences,
) : ViewModel() {
    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            val snap = preferences.snapshot.first()
            _startDestination.value = if (snap.onboardingCompleted) {
                TherapyDestinations.HOME
            } else {
                TherapyDestinations.ONBOARDING
            }
        }
    }
}

@Composable
fun TherapyNavHost(
    vm: NavBootstrapViewModel = hiltViewModel(),
) {
    val start by vm.startDestination.collectAsState()
    val nav = rememberNavController()
    val resolved = start ?: return

    NavHost(navController = nav, startDestination = resolved) {
        composable(TherapyDestinations.ONBOARDING) {
            OnboardingScreen(
                onComplete = {
                    nav.navigate(TherapyDestinations.HOME) {
                        popUpTo(TherapyDestinations.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }
        composable(TherapyDestinations.HOME) {
            HomeScreen(
                onOpenModule = { route -> nav.navigate(route) },
            )
        }
        composable(TherapyDestinations.RESPONSIBILITY) {
            ResponsibilityPieScreen(
                onBack = { nav.popBackStack() },
                onCrisis = { nav.navigate(TherapyDestinations.CRISIS) },
            )
        }
        composable(TherapyDestinations.DEREFLECTION) {
            DereflectionScreen(
                onBack = { nav.popBackStack() },
                onCrisis = { nav.navigate(TherapyDestinations.CRISIS) },
            )
        }
        composable(TherapyDestinations.VITALITY) {
            VitalityCompassScreen(
                onBack = { nav.popBackStack() },
                onCrisis = { nav.navigate(TherapyDestinations.CRISIS) },
            )
        }
        composable(TherapyDestinations.ANALYTICS) {
            AnalyticsScreen(onBack = { nav.popBackStack() })
        }
        composable(TherapyDestinations.ASSESSMENT) {
            AssessmentScreen(onBack = { nav.popBackStack() })
        }
        composable(TherapyDestinations.SETTINGS) {
            SettingsScreen(
                onBack = { nav.popBackStack() },
                onOpenExport = { nav.navigate(TherapyDestinations.EXPORT) },
            )
        }
        composable(TherapyDestinations.CRISIS) {
            CrisisScreen(onBack = { nav.popBackStack() })
        }
        composable(TherapyDestinations.EXPORT) {
            ExportScreen(onBack = { nav.popBackStack() })
        }
    }
}
