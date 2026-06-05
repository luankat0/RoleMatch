package com.pentagono.rolematch.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary              = PrimaryGreen,
    onPrimary            = TextOnPrimary,
    primaryContainer     = LightGreen,
    onPrimaryContainer   = DarkGreen,
    secondary            = OrangeAccent,
    onSecondary          = TextOnPrimary,
    secondaryContainer   = WarningYellow,
    onSecondaryContainer = TextDark,
    tertiary             = BlueAccent,
    error                = DiscountRed,
    background           = BackgroundCream,
    onBackground         = TextDark,
    surface              = CardWhite,
    onSurface            = TextDark,
    surfaceVariant       = CardCream,
    onSurfaceVariant     = TextMuted,
    outline              = BorderLight,
    outlineVariant       = DividerLight,
)

private val DarkColorScheme = darkColorScheme(
    primary              = DarkPrimaryGreen,
    onPrimary            = TextOnPrimary,
    primaryContainer     = Color(0xFF1E3A00),
    onPrimaryContainer   = Color(0xFFB8E676),
    secondary            = Color(0xFFFFAA44),
    onSecondary          = TextOnPrimary,
    secondaryContainer   = Color(0xFF4A2800),
    onSecondaryContainer = Color(0xFFFFD9A8),
    tertiary             = Color(0xFF5599FF),
    error                = Color(0xFFFF6B6B),
    background           = DarkBackground,
    onBackground         = DarkTextPrimary,
    surface              = DarkSurface,
    onSurface            = DarkTextPrimary,
    surfaceVariant       = DarkCard,
    onSurfaceVariant     = DarkTextSecondary,
    outline              = DarkBorder,
    outlineVariant       = DarkBorder,
)

@Composable
fun RoleMatchTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        ThemeMode.DARK   -> true
        ThemeMode.LIGHT  -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    MaterialTheme(
        colorScheme = if (useDark) DarkColorScheme else LightColorScheme,
        typography  = RoleMatchTypography,
        content     = content
    )
}