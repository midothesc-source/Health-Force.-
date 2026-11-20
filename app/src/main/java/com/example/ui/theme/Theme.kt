package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp

val PulseLogShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun HealthForceTheme(
    themeOption: AppThemeOption = AppThemeOption.MIDNIGHT_EMERALD,
    content: @Composable () -> Unit
) {
    val target = themeOption.palette
    val animSpec = tween<androidx.compose.ui.graphics.Color>(durationMillis = 360)

    val background by animateColorAsState(target.background, animSpec, label = "bg")
    val cardSurface by animateColorAsState(target.cardSurface, animSpec, label = "surface")
    val cardSurfaceElevated by animateColorAsState(target.cardSurfaceElevated, animSpec, label = "surfaceElev")
    val pillSurface by animateColorAsState(target.pillSurface, animSpec, label = "pill")
    val subtleBorder by animateColorAsState(target.subtleBorder, animSpec, label = "borderSubtle")
    val strongBorder by animateColorAsState(target.strongBorder, animSpec, label = "borderStrong")
    val textPrimary by animateColorAsState(target.textPrimary, animSpec, label = "textPrimary")
    val textSecondary by animateColorAsState(target.textSecondary, animSpec, label = "textSecondary")
    val textMuted by animateColorAsState(target.textMuted, animSpec, label = "textMuted")
    val primaryAccent by animateColorAsState(target.primaryAccent, animSpec, label = "primary")
    val primarySoft by animateColorAsState(target.primarySoft, animSpec, label = "primarySoft")
    val primaryBorder by animateColorAsState(target.primaryBorder, animSpec, label = "primaryBorder")
    val secondaryAccent by animateColorAsState(target.secondaryAccent, animSpec, label = "secondary")
    val secondarySoft by animateColorAsState(target.secondarySoft, animSpec, label = "secondarySoft")
    val secondaryBorder by animateColorAsState(target.secondaryBorder, animSpec, label = "secondaryBorder")
    val tertiaryAccent by animateColorAsState(target.tertiaryAccent, animSpec, label = "tertiary")
    val tertiarySoft by animateColorAsState(target.tertiarySoft, animSpec, label = "tertiarySoft")
    val tertiaryBorder by animateColorAsState(target.tertiaryBorder, animSpec, label = "tertiaryBorder")
    val alertAccent by animateColorAsState(target.alertAccent, animSpec, label = "alert")
    val alertSoft by animateColorAsState(target.alertSoft, animSpec, label = "alertSoft")
    val skyAccent by animateColorAsState(target.skyAccent, animSpec, label = "sky")
    val skySoft by animateColorAsState(target.skySoft, animSpec, label = "skySoft")

    val animatedPalette = AppThemePalette(
        background = background,
        cardSurface = cardSurface,
        cardSurfaceElevated = cardSurfaceElevated,
        pillSurface = pillSurface,
        subtleBorder = subtleBorder,
        strongBorder = strongBorder,
        textPrimary = textPrimary,
        textSecondary = textSecondary,
        textMuted = textMuted,
        primaryAccent = primaryAccent,
        primarySoft = primarySoft,
        primaryBorder = primaryBorder,
        secondaryAccent = secondaryAccent,
        secondarySoft = secondarySoft,
        secondaryBorder = secondaryBorder,
        tertiaryAccent = tertiaryAccent,
        tertiarySoft = tertiarySoft,
        tertiaryBorder = tertiaryBorder,
        alertAccent = alertAccent,
        alertSoft = alertSoft,
        skyAccent = skyAccent,
        skySoft = skySoft
    )

    val colorScheme = darkColorScheme(
        primary = primaryAccent,
        onPrimary = background,
        primaryContainer = primarySoft,
        onPrimaryContainer = primaryAccent,
        secondary = secondaryAccent,
        onSecondary = background,
        secondaryContainer = secondarySoft,
        onSecondaryContainer = secondaryAccent,
        tertiary = tertiaryAccent,
        onTertiary = textPrimary,
        tertiaryContainer = tertiarySoft,
        onTertiaryContainer = tertiaryAccent,
        background = background,
        onBackground = textPrimary,
        surface = cardSurface,
        onSurface = textPrimary,
        surfaceVariant = cardSurfaceElevated,
        onSurfaceVariant = textSecondary,
        outline = subtleBorder,
        outlineVariant = strongBorder,
        error = alertAccent,
        onError = background
    )

    CompositionLocalProvider(LocalAppPalette provides animatedPalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = PulseLogTypography,
            shapes = PulseLogShapes,
            content = content
        )
    }
}
