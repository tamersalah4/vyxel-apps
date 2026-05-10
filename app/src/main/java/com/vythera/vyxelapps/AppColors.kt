package com.vythera.vyxelapps

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

data class AppThemeColors(
    val bgPrimary      : Color,
    val bgSurface      : Color,
    val bgSurfaceAlt   : Color,
    val textPrimary    : Color,
    val textSecondary  : Color,
    val accent         : Color,
    val accentAlt      : Color,
    val border         : Color,
    val dockBg         : Color,
    val dockForeground : Color,
    val isDark         : Boolean = false
)

val LightTheme = AppThemeColors(
    bgPrimary      = Color(0xFFF8F9FF),
    bgSurface      = Color(0xFFFFFFFF),
    bgSurfaceAlt   = Color(0xFFF0F2FF),
    textPrimary    = Color(0xFF0D0D1A),
    textSecondary  = Color(0xFF6B7280),
    accent         = Color(0xFF6C63FF),
    accentAlt      = Color(0xFF4F8EF7),
    border         = Color(0xFFE8EAFF),
    dockBg         = Color(0xBBFFFFFF),
    dockForeground = Color(0xFF1A1A2E)
)

val DarkTheme = AppThemeColors(
    bgPrimary      = Color(0xFF0A0A14),
    bgSurface      = Color(0xFF13131F),
    bgSurfaceAlt   = Color(0xFF1A1A2E),
    textPrimary    = Color(0xFFF0F0FF),
    textSecondary  = Color(0xFF8B8BA8),
    accent         = Color(0xFF7C73FF),
    accentAlt      = Color(0xFF4F8EF7),
    border         = Color(0xFF2A2A40),
    dockBg         = Color(0xBB13131F),
    dockForeground = Color(0xFFE0E0F0),
    isDark         = true
)

val MinimalTheme = AppThemeColors(
    bgPrimary      = Color(0xFFFAFAFA),
    bgSurface      = Color(0xFFFFFFFF),
    bgSurfaceAlt   = Color(0xFFF5F5F5),
    textPrimary    = Color(0xFF111111),
    textSecondary  = Color(0xFF888888),
    accent         = Color(0xFF111111),
    accentAlt      = Color(0xFF555555),
    border         = Color(0xFFE0E0E0),
    dockBg         = Color(0xBBFFFFFF),
    dockForeground = Color(0xFF111111)
)

val AmoledTheme = AppThemeColors(
    bgPrimary      = Color(0xFF000000),
    bgSurface      = Color(0xFF0A0A0A),
    bgSurfaceAlt   = Color(0xFF111111),
    textPrimary    = Color(0xFFFFFFFF),
    textSecondary  = Color(0xFF777777),
    accent         = Color(0xFF00E5FF),
    accentAlt      = Color(0xFFFF4081),
    border         = Color(0xFF1A1A1A),
    dockBg         = Color(0xBB000000),
    dockForeground = Color(0xFFFFFFFF),
    isDark         = true
)

val SunsetTheme = AppThemeColors(
    bgPrimary      = Color(0xFFFFF8F0),
    bgSurface      = Color(0xFFFFFFFF),
    bgSurfaceAlt   = Color(0xFFFFF0E6),
    textPrimary    = Color(0xFF1A0A00),
    textSecondary  = Color(0xFF8B6555),
    accent         = Color(0xFFFF6B35),
    accentAlt      = Color(0xFFFF3CAC),
    border         = Color(0xFFFFE0CC),
    dockBg         = Color(0xBBFFF8F0),
    dockForeground = Color(0xFF1A0A00)
)

val LocalTheme = compositionLocalOf<AppThemeColors> { LightTheme }

enum class ThemeName(val label: String, val emoji: String) {
    LIGHT("Light", "☀️"), DARK("Dark", "🌙"),
    MINIMAL("Minimal", "⬜"), AMOLED("AMOLED", "⚫"), SUNSET("Sunset", "🌅")
}

fun themeColors(name: ThemeName) = when (name) {
    ThemeName.LIGHT   -> LightTheme
    ThemeName.DARK    -> DarkTheme
    ThemeName.MINIMAL -> MinimalTheme
    ThemeName.AMOLED  -> AmoledTheme
    ThemeName.SUNSET  -> SunsetTheme
}

val CardColors = listOf(
    Color(0xFF6C63FF), Color(0xFFFF6B35), Color(0xFF1DB954),
    Color(0xFFE91E63), Color(0xFF00BCD4), Color(0xFFFF9800),
    Color(0xFF9C27B0), Color(0xFF2196F3)
)
val StarGold   = Color(0xFFF59E0B)
val GreenOk    = Color(0xFF1DB954)
val RedDanger  = Color(0xFFEF4444)


fun fontFamilyFor(name: String): FontFamily = when (name) {
    "Serif"     -> FontFamily.Serif
    "Monospace" -> FontFamily.Monospace
    "Cursive"   -> FontFamily.Cursive
    else        -> FontFamily.Default   // "Samsung One UI" on Samsung = system default
}