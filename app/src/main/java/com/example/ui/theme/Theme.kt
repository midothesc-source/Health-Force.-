package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val PulseLogDarkColorScheme = darkColorScheme(
    primary = EmeraldAccent,
    onPrimary = SlateBackground,
    primaryContainer = EmeraldSoft,
    onPrimaryContainer = EmeraldAccent,
    secondary = AmberAccent,
    onSecondary = SlateBackground,
    secondaryContainer = AmberSoft,
    onSecondaryContainer = AmberAccent,
    tertiary = VioletAccent,
    onTertiary = TextPrimary,
    tertiaryContainer = VioletSoft,
    onTertiaryContainer = VioletAccent,
    background = SlateBackground,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = CardSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = SubtleBorder,
    outlineVariant = StrongBorder,
    error = RoseAccent,
    onError = SlateBackground
)

val PulseLogShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun PulseLogTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PulseLogDarkColorScheme,
        typography = PulseLogTypography,
        shapes = PulseLogShapes,
        content = content
    )
}
