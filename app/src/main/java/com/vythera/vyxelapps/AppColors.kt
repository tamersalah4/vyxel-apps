package com.vythera.vyxelapps

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

data class AppThemeColors(
    val bgPrimary      : Color,
    val bgSurface      : Color,
    val bgSurfaceAlt   : Color,
    val bgSurfaceHigh  : Color,
    val textPrimary    : Color,
    val textSecondary  : Color,
    val accent         : Color,
    val accentAlt      : Color,
    val accentContainer: Color,
    val onAccentContainer: Color,
    val border         : Color,
    val borderVariant  : Color,
    val dockBg         : Color,
    val dockForeground : Color,
    val isDark         : Boolean = false
)

// Parse "#RRGGBB" or "RRGGBB" to a fully-opaque Compose Color
fun hexToColor(hex: String, fallback: Color = Color.Black): Color = try {
    val h = hex.trimStart('#').let {
        if (it.length == 3) it.map { c -> "$c$c" }.joinToString("") else it
    }.padStart(6, '0').take(6)
    Color(android.graphics.Color.parseColor("#$h"))
} catch (_: Exception) { fallback }

// Convert a Color to its "#RRGGBB" hex string (alpha ignored — always FF when reloaded)
fun Color.toHex6(): String = "#%06X".format(((value shr 32).toInt()) and 0x00FFFFFF)

// Convert any AppThemeColors to a CustomThemeData for editing
fun AppThemeColors.toCustomThemeData() = CustomThemeData(
    bgPrimary         = bgPrimary.toHex6(),
    bgSurface         = bgSurface.toHex6(),
    bgSurfaceAlt      = bgSurfaceAlt.toHex6(),
    bgSurfaceHigh     = bgSurfaceHigh.toHex6(),
    textPrimary       = textPrimary.toHex6(),
    textSecondary     = textSecondary.toHex6(),
    accent            = accent.toHex6(),
    accentAlt         = accentAlt.toHex6(),
    accentContainer   = accentContainer.toHex6(),
    onAccentContainer = onAccentContainer.toHex6(),
    border            = border.toHex6(),
    borderVariant     = borderVariant.toHex6(),
    isDark            = isDark
)

// Reconstruct a live AppThemeColors from the user-saved hex strings
fun CustomThemeData.toAppThemeColors(): AppThemeColors {
    val bg  = hexToColor(bgPrimary,  Color.Black)
    val acc = hexToColor(accent,     Color(0xFFD0BCFF))
    return AppThemeColors(
        bgPrimary         = bg,
        bgSurface         = hexToColor(bgSurface,         Color(0xFF1A1825)),
        bgSurfaceAlt      = hexToColor(bgSurfaceAlt,      Color(0xFF231F30)),
        bgSurfaceHigh     = hexToColor(bgSurfaceHigh,     Color(0xFF2D2840)),
        textPrimary       = hexToColor(textPrimary,       Color(0xFFE6E1E5)),
        textSecondary     = hexToColor(textSecondary,     Color(0xFFCAC4D0)),
        accent            = acc,
        accentAlt         = hexToColor(accentAlt,         Color(0xFFCCC2DC)),
        accentContainer   = hexToColor(accentContainer,   Color(0xFF4F378B)),
        onAccentContainer = hexToColor(onAccentContainer, Color(0xFFEADDFF)),
        border            = hexToColor(border,            Color(0xFF938F99)),
        borderVariant     = hexToColor(borderVariant,     Color(0xFF49454F)),
        dockBg            = bg.copy(alpha = 0.94f),
        dockForeground    = acc,
        isDark            = isDark
    )
}

// ── M3 Expressive – Light ─────────────────────────────────────────────────────
// Surfaces tinted with the M3 neutral tonal palette; accent = M3 Purple 40.
// Monet (wallpaper-derived): accent1_600 as primary, accent1_100 as container.
val LightTheme = AppThemeColors(
    bgPrimary         = Color(0xFFFDFCFF),
    bgSurface         = Color(0xFFFFFFFF),
    bgSurfaceAlt      = Color(0xFFF2EFF9),   // surfaceContainerLow
    bgSurfaceHigh     = Color(0xFFE8E3F5),   // surfaceContainerHigh
    textPrimary       = Color(0xFF1C1B1F),
    textSecondary     = Color(0xFF49454F),
    accent            = Color(0xFF6750A4),   // primary (purple40)
    accentAlt         = Color(0xFF958DA5),   // secondary
    accentContainer   = Color(0xFFEADDFF),   // primaryContainer
    onAccentContainer = Color(0xFF21005D),
    border            = Color(0xFF79747E),
    borderVariant     = Color(0xFFCAC4D0),
    dockBg            = Color(0xF0FDFCFF),
    dockForeground    = Color(0xFF6750A4),
    isDark            = false
)

// ── M3 Expressive – Dark ──────────────────────────────────────────────────────
// Surfaces carry a subtle blue-purple tonal tint (M3 neutral1 palette).
// Monet: accent1_200 as primary (light on dark), accent1_700 as container.
val DarkTheme = AppThemeColors(
    bgPrimary         = Color(0xFF0F0E13),   // near-black with purple undertone
    bgSurface         = Color(0xFF1A1825),   // surfaceContainer
    bgSurfaceAlt      = Color(0xFF231F30),   // surfaceContainerHigh
    bgSurfaceHigh     = Color(0xFF2D2840),   // surfaceContainerHighest
    textPrimary       = Color(0xFFE6E1E5),   // onSurface
    textSecondary     = Color(0xFFCAC4D0),   // onSurfaceVariant
    accent            = Color(0xFFD0BCFF),   // primary (purple80 — light on dark)
    accentAlt         = Color(0xFFCCC2DC),   // secondary
    accentContainer   = Color(0xFF4F378B),   // primaryContainer (purple30)
    onAccentContainer = Color(0xFFEADDFF),   // onPrimaryContainer
    border            = Color(0xFF938F99),   // outline
    borderVariant     = Color(0xFF49454F),   // outlineVariant
    dockBg            = Color(0xF00F0E13),
    dockForeground    = Color(0xFFD0BCFF),
    isDark            = true
)

// ── AMOLED ───────────────────────────────────────────────────────────────────
// True black OLED substrate + vivid Monet-ready accents.
// Monet: accent1_300 as primary (neon-like on black), accent2_300 as secondary.
val AmoledTheme = AppThemeColors(
    bgPrimary         = Color(0xFF000000),
    bgSurface         = Color(0xFF0A0A0A),
    bgSurfaceAlt      = Color(0xFF141414),
    bgSurfaceHigh     = Color(0xFF1C1C1C),
    textPrimary       = Color(0xFFFFFFFF),
    textSecondary     = Color(0xFFAAAAAA),
    accent            = Color(0xFFA8FF78),   // spring green
    accentAlt         = Color(0xFF00F5D4),   // neon teal
    accentContainer   = Color(0xFF0D3D20),
    onAccentContainer = Color(0xFFC8FFD4),
    border            = Color(0xFF2A2A2A),
    borderVariant     = Color(0xFF141414),
    dockBg            = Color(0xF2000000),
    dockForeground    = Color(0xFFA8FF78),
    isDark            = true
)

// ── Minimal (dark monochrome) ─────────────────────────────────────────────────
// Near-black with strictly gray neutrals — zero colour saturation.
// Monet: uses accent2 (lower-saturation secondary palette) for a subtle tint.
val MinimalTheme = AppThemeColors(
    bgPrimary         = Color(0xFF080808),
    bgSurface         = Color(0xFF141414),
    bgSurfaceAlt      = Color(0xFF202020),
    bgSurfaceHigh     = Color(0xFF2C2C2C),
    textPrimary       = Color(0xFFF2F2F2),
    textSecondary     = Color(0xFF9E9E9E),
    accent            = Color(0xFFFFFFFF),   // pure white: maximum contrast on black
    accentAlt         = Color(0xFFB0B0B0),
    accentContainer   = Color(0xFF3C3C3C),
    onAccentContainer = Color(0xFFF2F2F2),
    border            = Color(0xFF3A3A3A),
    borderVariant     = Color(0xFF1F1F1F),
    dockBg            = Color(0xF2080808),
    dockForeground    = Color(0xFFFFFFFF),
    isDark            = true
)

// ── Sunset ────────────────────────────────────────────────────────────────────
// Warm amber/terracotta M3 Expressive palette — dark mode with warm surface tones.
// Monet: accent1 warm tones (amber/orange from wallpaper) enhance the accent.
val SunsetTheme = AppThemeColors(
    bgPrimary         = Color(0xFF160E0A),   // very dark coffee-brown
    bgSurface         = Color(0xFF241510),   // dark terracotta
    bgSurfaceAlt      = Color(0xFF301C15),   // warm clay
    bgSurfaceHigh     = Color(0xFF3D231B),   // elevated clay
    textPrimary       = Color(0xFFFFE5D0),   // warm cream
    textSecondary     = Color(0xFFD4A082),   // dusty rose
    accent            = Color(0xFFFFAB40),   // warm amber
    accentAlt         = Color(0xFFFF7C6B),   // coral
    accentContainer   = Color(0xFF6B2500),   // deep amber container
    onAccentContainer = Color(0xFFFFDCC6),
    border            = Color(0xFF6B3525),
    borderVariant     = Color(0xFF3D1E14),
    dockBg            = Color(0xF2160E0A),
    dockForeground    = Color(0xFFFFAB40),
    isDark            = true
)

// ── Custom (user-defined) — live object rebuilt from CustomThemeData ──────────
// The actual value is assembled in HomeScreen from state.customTheme.toAppThemeColors()
val CustomTheme: AppThemeColors get() = DarkTheme.copy()

val LocalTheme = compositionLocalOf<AppThemeColors> { DarkTheme }

enum class ThemeName(val label: String, val emoji: String) {
    LIGHT("Light", "☀️"), DARK("Dark", "🌙"), MINIMAL("Minimal", "◾"),
    AMOLED("AMOLED", "🖤"), SUNSET("Sunset", "🌅"), CUSTOM("Custom", "🎨")
}

fun themeColors(name: ThemeName) = when (name) {
    ThemeName.LIGHT   -> LightTheme
    ThemeName.DARK    -> DarkTheme
    ThemeName.MINIMAL -> MinimalTheme
    ThemeName.AMOLED  -> AmoledTheme
    ThemeName.SUNSET  -> SunsetTheme
    ThemeName.CUSTOM  -> CustomTheme
}

val StarGold  = Color(0xFFF59E0B)
val GreenOk   = Color(0xFF1DB954)
val RedDanger = Color(0xFFEF4444)

fun fontFamilyFor(name: String): FontFamily = when (name) {
    "Serif"     -> FontFamily.Serif
    "Monospace" -> FontFamily.Monospace
    "Cursive"   -> FontFamily.Cursive
    else        -> FontFamily.Default
}
