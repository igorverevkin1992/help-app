package com.helpapp.therapy.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val DarkColors = darkColorScheme(
    background = BackgroundNight,
    surface = SurfaceNight,
    surfaceVariant = SurfaceElevated,
    primary = PrimaryAccent,
    onPrimary = BackgroundNight,
    secondary = PrimaryStrong,
    onBackground = OnSurfaceHigh,
    onSurface = OnSurfaceHigh,
    onSurfaceVariant = OnSurfaceMuted,
)

private val LightColors = lightColorScheme(
    background = BackgroundNight,
    surface = SurfaceNight,
    surfaceVariant = SurfaceElevated,
    primary = PrimaryStrong,
    onPrimary = OnSurfaceHigh,
    secondary = PrimaryAccent,
    onBackground = OnSurfaceHigh,
    onSurface = OnSurfaceHigh,
    onSurfaceVariant = OnSurfaceMuted,
)

private val TherapyTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
    ),
)

@Composable
fun TherapyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = TherapyTypography,
        content = content,
    )
}
