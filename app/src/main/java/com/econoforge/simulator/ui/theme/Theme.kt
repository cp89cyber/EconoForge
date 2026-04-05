package com.econoforge.simulator.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AppColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = Slate,
    secondary = Aqua,
    onSecondary = Slate,
    tertiary = Success,
    onTertiary = Slate,
    background = Slate,
    onBackground = Ink,
    surface = Navy,
    onSurface = Ink,
    surfaceVariant = DeepTeal,
    onSurfaceVariant = SoftWhite,
    error = Danger,
    onError = Ink,
)

@Composable
fun EconoForgeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = AppTypography,
        content = content,
    )
}
