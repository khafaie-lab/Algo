package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class AppThemePreset(
    val id: String,
    val nameFa: String,
    val nameEn: String,
    val isDark: Boolean,
    val previewSwatches: List<Color>
) {
    IBKR_OBSIDIAN(
        id = "IBKR_OBSIDIAN",
        nameFa = "ترمینال کلاسیک IBKR",
        nameEn = "IBKR Obsidian",
        isDark = true,
        previewSwatches = listOf(Color(0xFF0A0D14), Color(0xFF2B7FFF), Color(0xFF30D158))
    ),
    QUANT_EMERALD(
        id = "QUANT_EMERALD",
        nameFa = "کوانت زمردی (شبانه)",
        nameEn = "Quant Emerald",
        isDark = true,
        previewSwatches = listOf(Color(0xFF07110E), Color(0xFF00D68F), Color(0xFFF5B731))
    ),
    CARBON_GOLD(
        id = "CARBON_GOLD",
        nameFa = "کربن طلایی (نهادی)",
        nameEn = "Carbon Gold",
        isDark = true,
        previewSwatches = listOf(Color(0xFF0C0C10), Color(0xFFE5B84B), Color(0xFF38BDF8))
    ),
    MINIMAL_LIGHT(
        id = "MINIMAL_LIGHT",
        nameFa = "روشن مینیمال (روز)",
        nameEn = "Minimal Light",
        isDark = false,
        previewSwatches = listOf(Color(0xFFF4F6FA), Color(0xFF1A66FF), Color(0xFF0E9F6E))
    )
}

@Immutable
data class SemanticColors(
    val preset: AppThemePreset = AppThemePreset.IBKR_OBSIDIAN,
    val isDark: Boolean = true,
    val background: Color = Color(0xFF0A0D14),
    val surface: Color = Color(0xFF12161F),
    val surfaceVariant: Color = Color(0xFF181E29),
    val border: Color = Color(0xFF232A36),
    val text: Color = Color(0xFFF0F3F8),
    val textSecondary: Color = Color(0xFF8B95A5),
    val bullish: Color = Color(0xFF30D158),
    val bearish: Color = Color(0xFFFF453A),
    val accent: Color = Color(0xFF2B7FFF),
    val bid: Color = Color(0xFF2B7FFF),
    val bidBg: Color = Color(0xFF102542),
    val ask: Color = Color(0xFFFF453A),
    val askBg: Color = Color(0xFF381519)
)

fun paletteFor(preset: AppThemePreset): SemanticColors = when (preset) {
    AppThemePreset.IBKR_OBSIDIAN -> SemanticColors(
        preset = preset,
        isDark = true,
        background = Color(0xFF0A0D14),
        surface = Color(0xFF12161F),
        surfaceVariant = Color(0xFF181E29),
        border = Color(0xFF232A36),
        text = Color(0xFFF0F3F8),
        textSecondary = Color(0xFF8B95A5),
        bullish = Color(0xFF30D158),
        bearish = Color(0xFFFF453A),
        accent = Color(0xFF2B7FFF),
        bid = Color(0xFF2B7FFF),
        bidBg = Color(0xFF102542),
        ask = Color(0xFFFF453A),
        askBg = Color(0xFF381519)
    )
    AppThemePreset.QUANT_EMERALD -> SemanticColors(
        preset = preset,
        isDark = true,
        background = Color(0xFF07110E),
        surface = Color(0xFF0D1B17),
        surfaceVariant = Color(0xFF132620),
        border = Color(0xFF1D3830),
        text = Color(0xFFECFDF5),
        textSecondary = Color(0xFF84A99B),
        bullish = Color(0xFF00D68F),
        bearish = Color(0xFFFF5C5C),
        accent = Color(0xFF00D68F),
        bid = Color(0xFF00B8A9),
        bidBg = Color(0xFF092E2B),
        ask = Color(0xFFFF5C5C),
        askBg = Color(0xFF361518)
    )
    AppThemePreset.CARBON_GOLD -> SemanticColors(
        preset = preset,
        isDark = true,
        background = Color(0xFF0C0C10),
        surface = Color(0xFF14141B),
        surfaceVariant = Color(0xFF1D1D26),
        border = Color(0xFF2C2C3A),
        text = Color(0xFFF8FAFC),
        textSecondary = Color(0xFF9CA3AF),
        bullish = Color(0xFF22C55E),
        bearish = Color(0xFFEF4444),
        accent = Color(0xFFE5B84B),
        bid = Color(0xFFE5B84B),
        bidBg = Color(0xFF2B2310),
        ask = Color(0xFFEF4444),
        askBg = Color(0xFF361515)
    )
    AppThemePreset.MINIMAL_LIGHT -> SemanticColors(
        preset = preset,
        isDark = false,
        background = Color(0xFFF4F6FA),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFFEAF0F6),
        border = Color(0xFFD5DFEB),
        text = Color(0xFF0F172A),
        textSecondary = Color(0xFF526075),
        bullish = Color(0xFF0E9F6E),
        bearish = Color(0xFFDC2626),
        accent = Color(0xFF1A66FF),
        bid = Color(0xFF1A66FF),
        bidBg = Color(0xFFE0ECFF),
        ask = Color(0xFFDC2626),
        askBg = Color(0xFFFEE2E2)
    )
}

object ThemeState {
    var current by mutableStateOf(paletteFor(AppThemePreset.IBKR_OBSIDIAN))
}

val LocalSemanticColors = staticCompositionLocalOf { SemanticColors() }

// Dynamic theme-reactive semantic properties (automatically recompose when ThemeState.current changes)
val AppBackground: Color get() = ThemeState.current.background
val AppSurface: Color get() = ThemeState.current.surface
val AppSurfaceVariant: Color get() = ThemeState.current.surfaceVariant
val AppBorder: Color get() = ThemeState.current.border
val AppText: Color get() = ThemeState.current.text
val AppTextSecondary: Color get() = ThemeState.current.textSecondary
val AppBullish: Color get() = ThemeState.current.bullish
val AppBearish: Color get() = ThemeState.current.bearish
val AppAccent: Color get() = ThemeState.current.accent

// Dedicated IBKR Design Tokens
val IbkrCanvas: Color get() = ThemeState.current.background
val IbkrSurface: Color get() = ThemeState.current.surface
val IbkrSurfaceVariant: Color get() = ThemeState.current.surfaceVariant
val IbkrBorder: Color get() = ThemeState.current.border
val IbkrBid: Color get() = ThemeState.current.bid
val IbkrBidBg: Color get() = ThemeState.current.bidBg
val IbkrAsk: Color get() = ThemeState.current.ask
val IbkrAskBg: Color get() = ThemeState.current.askBg
val IbkrTextPrimary: Color get() = ThemeState.current.text
val IbkrTextSecondary: Color get() = ThemeState.current.textSecondary
val IbkrGreen: Color get() = ThemeState.current.bullish
val IbkrRed: Color get() = ThemeState.current.bearish

// Centralized Surface & Border Tokens
val DarkCanvasBackground: Color get() = ThemeState.current.background
val FrostedGlassSurface: Color get() = ThemeState.current.surface
val FrostedGlassSurfaceHighlighted: Color get() = ThemeState.current.surfaceVariant
val FrostedBorderTop: Color get() = ThemeState.current.border
val FrostedBorderBottom: Color get() = ThemeState.current.border

// Aliases mapped strictly to the centralized semantic tokens
val Background: Color get() = ThemeState.current.background
val Primary: Color get() = ThemeState.current.accent
val SurfaceDark: Color get() = ThemeState.current.surface
val GlassSurfaceTab: Color get() = ThemeState.current.surface
val GlassSurface: Color get() = ThemeState.current.surface
val GlassBorder: Color get() = ThemeState.current.border
val TextPrimary: Color get() = ThemeState.current.text
val TextSecondary: Color get() = ThemeState.current.textSecondary
val CodeText: Color get() = ThemeState.current.text
val TabSelectedIndicator: Color get() = ThemeState.current.accent

// Semantic Text Scale
val Slate50: Color get() = ThemeState.current.text
val Slate100: Color get() = ThemeState.current.text
val Slate200: Color get() = ThemeState.current.text
val Slate300: Color get() = if (ThemeState.current.isDark) Color(0xFFCBD2DC) else Color(0xFF334155)
val Slate400: Color get() = ThemeState.current.textSecondary
val Slate500: Color get() = ThemeState.current.textSecondary
val Slate600: Color get() = if (ThemeState.current.isDark) Color(0xFF4A5565) else Color(0xFF64748B)
val Slate700: Color get() = if (ThemeState.current.isDark) Color(0xFF353D49) else Color(0xFF94A3B8)
val Slate800: Color get() = ThemeState.current.border
val Slate900: Color get() = ThemeState.current.surface

// Semantic Accent & Tones
val Cyan200 = Color(0xFFA5F3FC)
val Cyan300 = Color(0xFF67E8F9)
val Cyan400 = Color(0xFF22D3EE)
val Cyan500 = Color(0xFF06B6D4)
val Blue300 = Color(0xFF93C5FD)
val Blue400: Color get() = ThemeState.current.accent
val Blue500: Color get() = ThemeState.current.accent
val Blue600 = Color(0xFF2563EB)
val Indigo200 = Color(0xFFC7D2FE)
val Indigo300 = Color(0xFFA5B4FC)
val Indigo400 = Color(0xFF818CF8)
val Indigo500 = Color(0xFF6366F1)
val Indigo600 = Color(0xFF4F46E5)

// Market Direction (Bullish / Bearish)
val Emerald300 = Color(0xFF56D197)
val Emerald400: Color get() = ThemeState.current.bullish
val Emerald500: Color get() = ThemeState.current.bullish
val Emerald600 = Color(0xFF1C9E63)
val Rose300 = Color(0xFFFDA4AF)
val Rose400: Color get() = ThemeState.current.bearish
val Rose500: Color get() = ThemeState.current.bearish
val Rose600 = Color(0xFFC73E43)
val Amber300 = Color(0xFFFCD34D)
val Amber400 = Color(0xFFFBBF24)
val Amber500 = Color(0xFFF59E0B)
