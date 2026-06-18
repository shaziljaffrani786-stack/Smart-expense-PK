package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Md3DarkPrimary,
    onPrimary = Md3DarkOnPrimary,
    primaryContainer = Md3DarkPrimaryContainer,
    onPrimaryContainer = Md3DarkOnPrimaryContainer,
    background = Md3DarkBackground,
    onBackground = Md3DarkOnBackground,
    surface = Md3DarkSurface,
    onSurface = Md3DarkOnSurface,
    surfaceVariant = Md3DarkSurfaceVariant,
    onSurfaceVariant = Md3DarkOnSurfaceVariant,
    secondary = AccentGold,
    onSecondary = Md3DarkBackground,
    tertiary = LightEmerald
)

private val LightColorScheme = lightColorScheme(
    primary = Md3LightPrimary,
    onPrimary = Md3LightOnPrimary,
    primaryContainer = Md3LightPrimaryContainer,
    onPrimaryContainer = Md3LightOnPrimaryContainer,
    background = Md3LightBackground,
    onBackground = Md3LightOnBackground,
    surface = Md3LightSurface,
    onSurface = Md3LightOnSurface,
    surfaceVariant = Md3LightSurfaceVariant,
    onSurfaceVariant = Md3LightOnSurfaceVariant,
    secondary = PakistaniGreen,
    onSecondary = Md3LightOnPrimary,
    tertiary = AccentGold
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
