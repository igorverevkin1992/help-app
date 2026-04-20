package com.helpapp.therapy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.helpapp.therapy.ui.screens.analytics.AnalyticsScreen
import com.helpapp.therapy.ui.screens.assessment.AssessmentScreen
import com.helpapp.therapy.ui.screens.dereflection.DereflectionScreen
import com.helpapp.therapy.ui.screens.home.HomeScreen
import com.helpapp.therapy.ui.screens.responsibility.ResponsibilityPieScreen
import com.helpapp.therapy.ui.screens.settings.SettingsScreen
import com.helpapp.therapy.ui.screens.vitality.VitalityCompassScreen

@Composable
fun TherapyNavHost() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = TherapyDestinations.HOME) {
        composable(TherapyDestinations.HOME) {
            HomeScreen(
                onOpenModule = { route -> nav.navigate(route) },
            )
        }
        composable(TherapyDestinations.RESPONSIBILITY) {
            ResponsibilityPieScreen(onBack = { nav.popBackStack() })
        }
        composable(TherapyDestinations.DEREFLECTION) {
            DereflectionScreen(onBack = { nav.popBackStack() })
        }
        composable(TherapyDestinations.VITALITY) {
            VitalityCompassScreen(onBack = { nav.popBackStack() })
        }
        composable(TherapyDestinations.ANALYTICS) {
            AnalyticsScreen(onBack = { nav.popBackStack() })
        }
        composable(TherapyDestinations.ASSESSMENT) {
            AssessmentScreen(onBack = { nav.popBackStack() })
        }
        composable(TherapyDestinations.SETTINGS) {
            SettingsScreen(onBack = { nav.popBackStack() })
        }
    }
}
