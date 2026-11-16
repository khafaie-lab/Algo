package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

object AppTheme {
    val colors: SemanticColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSemanticColors.current
}

@Composable
fun MyApplicationTheme(
    themePreset: AppThemePreset = ThemeState.current.preset,
    content: @Composable () -> Unit,
) {
    val semanticColors = paletteFor(themePreset)
    ThemeState.current = semanticColors

    val colorScheme = if (semanticColors.isDark) {
        darkColorScheme(
            primary = semanticColors.accent,
            background = semanticColors.background,
            surface = semanticColors.surface,
            surfaceVariant = semanticColors.surfaceVariant,
            outline = semanticColors.border,
            onPrimary = Color.White,
            onBackground = semanticColors.text,
            onSurface = semanticColors.text,
            onSurfaceVariant = semanticColors.textSecondary
        )
    } else {
        lightColorScheme(
            primary = semanticColors.accent,
            background = semanticColors.background,
            surface = semanticColors.surface,
            surfaceVariant = semanticColors.surfaceVariant,
            outline = semanticColors.border,
            onPrimary = Color.White,
            onBackground = semanticColors.text,
            onSurface = semanticColors.text,
            onSurfaceVariant = semanticColors.textSecondary
        )
    }

    CompositionLocalProvider(LocalSemanticColors provides semanticColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
