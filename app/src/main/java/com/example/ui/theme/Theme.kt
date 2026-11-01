package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val RepoManagerColorScheme = darkColorScheme(
    primary = PrimaryPurple,
    onPrimary = TextPrimary,
    primaryContainer = DarkElevatedSurface,
    onPrimaryContainer = PrimaryPurpleLight,
    secondary = SecondaryCyan,
    onSecondary = DarkBg,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = SecondaryCyanLight,
    tertiary = AccentIndigo,
    onTertiary = TextPrimary,
    background = DarkBg,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkElevatedSurface,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderHighlight,
    error = StatusError,
    onError = DarkBg
)

@Composable
fun RepoManagerProTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RepoManagerColorScheme,
        typography = Typography,
        content = content
    )
}
