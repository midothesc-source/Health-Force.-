package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class AppThemePalette(
    val background: Color,
    val cardSurface: Color,
    val cardSurfaceElevated: Color,
    val pillSurface: Color,
    val subtleBorder: Color,
    val strongBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val primaryAccent: Color,
    val primarySoft: Color,
    val primaryBorder: Color,
    val secondaryAccent: Color,
    val secondarySoft: Color,
    val secondaryBorder: Color,
    val tertiaryAccent: Color,
    val tertiarySoft: Color,
    val tertiaryBorder: Color,
    val alertAccent: Color,
    val alertSoft: Color,
    val skyAccent: Color,
    val skySoft: Color
)

enum class AppThemeOption(
    val id: String,
    val displayName: String,
    val tagline: String,
    val palette: AppThemePalette
) {
    MIDNIGHT_EMERALD(
        id = "midnight_emerald",
        displayName = "Midnight Emerald",
        tagline = "Deep slate · Emerald & amber gold",
        palette = AppThemePalette(
            background = Color(0xFF090D16),
            cardSurface = Color(0xFF111827),
            cardSurfaceElevated = Color(0xFF172033),
            pillSurface = Color(0xFF1E293B),
            subtleBorder = Color(0x14FFFFFF),
            strongBorder = Color(0x24FFFFFF),
            textPrimary = Color(0xFFF8FAFC),
            textSecondary = Color(0xFF94A3B8),
            textMuted = Color(0xFF64748B),
            primaryAccent = Color(0xFF10B981),
            primarySoft = Color(0x2610B981),
            primaryBorder = Color(0x4D10B981),
            secondaryAccent = Color(0xFFF59E0B),
            secondarySoft = Color(0x26F59E0B),
            secondaryBorder = Color(0x4DF59E0B),
            tertiaryAccent = Color(0xFF8B5CF6),
            tertiarySoft = Color(0x268B5CF6),
            tertiaryBorder = Color(0x4D8B5CF6),
            alertAccent = Color(0xFFFB7185),
            alertSoft = Color(0x26FB7185),
            skyAccent = Color(0xFF38BDF8),
            skySoft = Color(0x2638BDF8)
        )
    ),
    AURORA_BOREALIS(
        id = "aurora_borealis",
        displayName = "Aurora Borealis",
        tagline = "Arctic midnight · Polar teal & prism",
        palette = AppThemePalette(
            background = Color(0xFF050E17),
            cardSurface = Color(0xFF0B1929),
            cardSurfaceElevated = Color(0xFF102438),
            pillSurface = Color(0xFF16304A),
            subtleBorder = Color(0x185EEAD4),
            strongBorder = Color(0x335EEAD4),
            textPrimary = Color(0xFFF0FDFA),
            textSecondary = Color(0xFF99B8CC),
            textMuted = Color(0xFF5D7E96),
            primaryAccent = Color(0xFF2DD4BF),
            primarySoft = Color(0x262DD4BF),
            primaryBorder = Color(0x4D2DD4BF),
            secondaryAccent = Color(0xFF38BDF8),
            secondarySoft = Color(0x2638BDF8),
            secondaryBorder = Color(0x4D38BDF8),
            tertiaryAccent = Color(0xFFE879F9),
            tertiarySoft = Color(0x26E879F9),
            tertiaryBorder = Color(0x4DE879F9),
            alertAccent = Color(0xFFF43F5E),
            alertSoft = Color(0x26F43F5E),
            skyAccent = Color(0xFF7DD3FC),
            skySoft = Color(0x267DD3FC)
        )
    ),
    CYBERPUNK_NEON(
        id = "cyberpunk_neon",
        displayName = "Cyberpunk Neon",
        tagline = "Obsidian void · Electric cyan & hyper pink",
        palette = AppThemePalette(
            background = Color(0xFF080712),
            cardSurface = Color(0xFF121024),
            cardSurfaceElevated = Color(0xFF1B1836),
            pillSurface = Color(0xFF252147),
            subtleBorder = Color(0x1E00F0FF),
            strongBorder = Color(0x3800F0FF),
            textPrimary = Color(0xFFF8FAFC),
            textSecondary = Color(0xFFA5A3C7),
            textMuted = Color(0xFF6B6894),
            primaryAccent = Color(0xFF00F0FF),
            primarySoft = Color(0x2400F0FF),
            primaryBorder = Color(0x5200F0FF),
            secondaryAccent = Color(0xFFFF2E93),
            secondarySoft = Color(0x26FF2E93),
            secondaryBorder = Color(0x52FF2E93),
            tertiaryAccent = Color(0xFFB8FF3C),
            tertiarySoft = Color(0x24B8FF3C),
            tertiaryBorder = Color(0x4DB8FF3C),
            alertAccent = Color(0xFFFF4D6D),
            alertSoft = Color(0x26FF4D6D),
            skyAccent = Color(0xFF60A5FA),
            skySoft = Color(0x2660A5FA)
        )
    ),
    VOLCANIC_OBSIDIAN(
        id = "volcanic_obsidian",
        displayName = "Volcanic Obsidian",
        tagline = "Warm basalt · Molten ember & honey gold",
        palette = AppThemePalette(
            background = Color(0xFF0E0A09),
            cardSurface = Color(0xFF1A1210),
            cardSurfaceElevated = Color(0xFF261B17),
            pillSurface = Color(0xFF33241F),
            subtleBorder = Color(0x1AFF8A4C),
            strongBorder = Color(0x33FF8A4C),
            textPrimary = Color(0xFFFFF7ED),
            textSecondary = Color(0xFFC4A79B),
            textMuted = Color(0xFF876C61),
            primaryAccent = Color(0xFFFF6B35),
            primarySoft = Color(0x26FF6B35),
            primaryBorder = Color(0x4DFF6B35),
            secondaryAccent = Color(0xFFFFB627),
            secondarySoft = Color(0x26FFB627),
            secondaryBorder = Color(0x4DFFB627),
            tertiaryAccent = Color(0xFFFB7185),
            tertiarySoft = Color(0x26FB7185),
            tertiaryBorder = Color(0x4DFB7185),
            alertAccent = Color(0xFFEF4444),
            alertSoft = Color(0x26EF4444),
            skyAccent = Color(0xFFFDBA74),
            skySoft = Color(0x26FDBA74)
        )
    ),
    BIOLUMINESCENT_ABYSS(
        id = "bioluminescent_abyss",
        displayName = "Bioluminescent Abyss",
        tagline = "Ocean trench · Glowing aqua & indigo",
        palette = AppThemePalette(
            background = Color(0xFF040B14),
            cardSurface = Color(0xFF081628),
            cardSurfaceElevated = Color(0xFF0E223D),
            pillSurface = Color(0xFF153054),
            subtleBorder = Color(0x1A22D3EE),
            strongBorder = Color(0x3322D3EE),
            textPrimary = Color(0xFFECFEFF),
            textSecondary = Color(0xFF8FAEC9),
            textMuted = Color(0xFF537596),
            primaryAccent = Color(0xFF22D3EE),
            primarySoft = Color(0x2622D3EE),
            primaryBorder = Color(0x4D22D3EE),
            secondaryAccent = Color(0xFF34D399),
            secondarySoft = Color(0x2634D399),
            secondaryBorder = Color(0x4D34D399),
            tertiaryAccent = Color(0xFF818CF8),
            tertiarySoft = Color(0x26818CF8),
            tertiaryBorder = Color(0x4D818CF8),
            alertAccent = Color(0xFFFB7185),
            alertSoft = Color(0x26FB7185),
            skyAccent = Color(0xFF38BDF8),
            skySoft = Color(0x2638BDF8)
        )
    ),
    ROYAL_AMETHYST(
        id = "royal_amethyst",
        displayName = "Royal Amethyst",
        tagline = "Velvet dusk · Champagne gold & amethyst",
        palette = AppThemePalette(
            background = Color(0xFF0C0814),
            cardSurface = Color(0xFF161024),
            cardSurfaceElevated = Color(0xFF211836),
            pillSurface = Color(0xFF2D2147),
            subtleBorder = Color(0x1AA78BFA),
            strongBorder = Color(0x33A78BFA),
            textPrimary = Color(0xFFFAF5FF),
            textSecondary = Color(0xFFB6A6D4),
            textMuted = Color(0xFF796999),
            primaryAccent = Color(0xFFFBBF24),
            primarySoft = Color(0x26FBBF24),
            primaryBorder = Color(0x4DFBBF24),
            secondaryAccent = Color(0xFFA78BFA),
            secondarySoft = Color(0x26A78BFA),
            secondaryBorder = Color(0x4DA78BFA),
            tertiaryAccent = Color(0xFFF472B6),
            tertiarySoft = Color(0x26F472B6),
            tertiaryBorder = Color(0x4DF472B6),
            alertAccent = Color(0xFFFB7185),
            alertSoft = Color(0x26FB7185),
            skyAccent = Color(0xFFC084FC),
            skySoft = Color(0x26C084FC)
        )
    );

    companion object {
        fun fromId(id: String?): AppThemeOption =
            entries.firstOrNull { it.id == id } ?: MIDNIGHT_EMERALD
    }
}

val LocalAppPalette = staticCompositionLocalOf { AppThemeOption.MIDNIGHT_EMERALD.palette }

// Reactive Theme Tokens (automatically adapt across all screens on theme change)
val SlateBackground: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.background
val CardSurface: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.cardSurface
val CardSurfaceElevated: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.cardSurfaceElevated
val PillSurface: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.pillSurface
val SubtleBorder: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.subtleBorder
val StrongBorder: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.strongBorder

val TextPrimary: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.textPrimary
val TextSecondary: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.textSecondary
val TextMuted: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.textMuted

val EmeraldAccent: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.primaryAccent
val EmeraldSoft: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.primarySoft
val EmeraldBorder: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.primaryBorder

val AmberAccent: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.secondaryAccent
val AmberSoft: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.secondarySoft
val AmberBorder: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.secondaryBorder

val VioletAccent: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.tertiaryAccent
val VioletSoft: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.tertiarySoft
val VioletBorder: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.tertiaryBorder

val RoseAccent: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.alertAccent
val RoseSoft: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.alertSoft

val SkyAccent: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.skyAccent
val SkySoft: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.skySoft
