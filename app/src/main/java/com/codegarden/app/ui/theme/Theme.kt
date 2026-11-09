package com.codegarden.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ======================================================================
// 调色板：浅色（治愈暖米）/ 深色（深夜森林）两套一一对应的颜色
// ======================================================================

@Immutable
data class GardenPalette(
    val isDark: Boolean,
    val mintPrimary: Color,
    val mintDeep: Color,
    val mintLight: Color,
    val mintContainer: Color,
    val bgTop: Color,
    val bgSoft: Color,
    val bgBottom: Color,
    val card: Color,
    val pinkAccent: Color,
    val pinkContainer: Color,
    val pinkDeep: Color,
    val soilBrown: Color,
    val potBrown: Color,
    val potRim: Color,
    val ink: Color,
    val inkSecondary: Color,
    val inkTertiary: Color,
    val amber: Color,
    val amberContainer: Color,
    val error: Color,
    val dropBlue: Color,
    val waterBlue: Color,
    val fertilizeGreen: Color,
    val hairline: Color,
    val sageRing: Color,
    val glowMint: Color,
    val glowPink: Color,
    val glowAmber: Color,
    val warnSurface: Color,
    val criticSurface: Color,
    val badgeWarn: Color,
    val badgeWarnFg: Color,
    val badgeYellowFg: Color,
    val heat: List<Color>,
    // —— 场景色（“我的花园”橱窗）——
    val grassTop: Color,
    val grassBottom: Color,
    val grassBlade: Color,
    val sceneShadow: Color,
    val sunCore: Color,
    val sunGlow: Color,
    val sunRay: Color,
    val sunHalo: Color,
    val moonCore: Color,
    val moonGlow: Color,
    val starTint: Color,
    val cloudBody: Color,
    val snowBody: Color,
    val plateSurface: Color,
    val chipSurface: Color
)

/** 浅色主题：暖米治愈 */
val LightGardenPalette = GardenPalette(
    isDark = false,
    mintPrimary = Color(0xFF4E8C6A),
    mintDeep = Color(0xFF2F6B4E),
    mintLight = Color(0xFF9CC2A9),
    mintContainer = Color(0xFFE4EDE1),
    bgTop = Color(0xFFF4F2EA),
    bgSoft = Color(0xFFF8F6EF),
    bgBottom = Color(0xFFF0EEE5),
    card = Color(0xFFFCFBF7),
    pinkAccent = Color(0xFFC98A7E),
    pinkContainer = Color(0xFFF1E4DE),
    pinkDeep = Color(0xFF8A5A50),
    soilBrown = Color(0xFF8A6242),
    potBrown = Color(0xFFC98A6B),
    potRim = Color(0xFFB57855),
    ink = Color(0xFF33403A),
    inkSecondary = Color(0xFF8C948B),
    inkTertiary = Color(0xFFB3B8AE),
    amber = Color(0xFFC69A4C),
    amberContainer = Color(0xFFF3E9D2),
    error = Color(0xFFC0604F),
    dropBlue = Color(0xFF6E9BC4),
    waterBlue = Color(0xFF7AA8CC),
    fertilizeGreen = Color(0xFF5E8C63),
    hairline = Color(0xFFE7E3D6),
    sageRing = Color(0xFF93B79E),
    glowMint = Color(0xFFB8DDC0).copy(alpha = 0.35f),
    glowPink = Color(0xFFF5C9BE).copy(alpha = 0.30f),
    glowAmber = Color(0xFFF0D89A).copy(alpha = 0.25f),
    warnSurface = Color(0xFFFBEED9),
    criticSurface = Color(0xFFF8E2DE),
    badgeWarn = Color(0xFFFFF3E0),
    badgeWarnFg = Color(0xFFC89A4C),
    badgeYellowFg = Color(0xFF9A5A1F),
    heat = listOf(
        Color(0xFFECE8DB),
        Color(0xFFCDE3CB),
        Color(0xFFA6CDA6),
        Color(0xFF6FA982),
        Color(0xFF3C7B5C)
    ),
    grassTop = Color(0xFFAED3A0),
    grassBottom = Color(0xFF8FBC7E),
    grassBlade = Color(0xFF7FB16C),
    sceneShadow = Color(0xFF6F5238),
    sunCore = Color(0xFFFFC94D),
    sunGlow = Color(0xFFFFF0B8),
    sunRay = Color(0xFFFFD978),
    sunHalo = Color(0xFFFFF0B0),
    moonCore = Color(0xFFF2EEDC),
    moonGlow = Color(0xFFCBD7E6),
    starTint = Color(0xFFFFF6D8),
    cloudBody = Color.White,
    snowBody = Color.White,
    plateSurface = Color.White,
    chipSurface = Color.White
)

/** 深色主题：深夜森林 */
val DarkGardenPalette = GardenPalette(
    isDark = true,
    mintPrimary = Color(0xFF6FAF8A),
    mintDeep = Color(0xFF9BD3B4),
    mintLight = Color(0xFF4E7A63),
    mintContainer = Color(0xFF2E4038),
    bgTop = Color(0xFF141C17),
    bgSoft = Color(0xFF1A241E),
    bgBottom = Color(0xFF111813),
    card = Color(0xFF212D26),
    pinkAccent = Color(0xFFD89D91),
    pinkContainer = Color(0xFF3A2C29),
    pinkDeep = Color(0xFFEFC0B6),
    soilBrown = Color(0xFF7A5638),
    potBrown = Color(0xFFB07A5C),
    potRim = Color(0xFF9C6647),
    ink = Color(0xFFE6EDE7),
    inkSecondary = Color(0xFF9AA89F),
    inkTertiary = Color(0xFF6E7B72),
    amber = Color(0xFFD9B56A),
    amberContainer = Color(0xFF3A3222),
    error = Color(0xFFE08A7B),
    dropBlue = Color(0xFF8CB3D4),
    waterBlue = Color(0xFF8CB3D4),
    fertilizeGreen = Color(0xFF7FBF98),
    hairline = Color(0xFF324039),
    sageRing = Color(0xFF4E6B5A),
    glowMint = Color(0xFF7FBF98).copy(alpha = 0.18f),
    glowPink = Color(0xFFD89D91).copy(alpha = 0.14f),
    glowAmber = Color(0xFFD9B56A).copy(alpha = 0.12f),
    warnSurface = Color(0xFF332B1E),
    criticSurface = Color(0xFF382322),
    badgeWarn = Color(0xFF3A3222),
    badgeWarnFg = Color(0xFFD9B56A),
    badgeYellowFg = Color(0xFFE0B77A),
    heat = listOf(
        Color(0xFF22302A),
        Color(0xFF2F4438),
        Color(0xFF3C5C48),
        Color(0xFF4E7A5F),
        Color(0xFF5F9375)
    ),
    grassTop = Color(0xFF2C5540),
    grassBottom = Color(0xFF1D3A2C),
    grassBlade = Color(0xFF3E7455),
    sceneShadow = Color(0xFF0B120E),
    sunCore = Color(0xFF3D4C63),
    sunGlow = Color(0xFF5B6E8C),
    sunRay = Color(0xFF4A5B78),
    sunHalo = Color(0xFF6A7C9B),
    moonCore = Color(0xFFE8EEF7),
    moonGlow = Color(0xFF9FB6D6),
    starTint = Color(0xFFDCE6FF),
    cloudBody = Color(0xFF6C7A8C),
    snowBody = Color(0xFFDCE6F2),
    plateSurface = Color(0xFF22303A),
    chipSurface = Color(0xFF24313B)
)

/** 当前调色板（由 CodeGardenTheme 提供） */
val LocalGardenPalette = staticCompositionLocalOf { LightGardenPalette }

// ======================================================================
// 对外颜色（全部随主题切换）
// ======================================================================

val MintPrimary: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.mintPrimary
val MintDeep: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.mintDeep
val MintLight: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.mintLight
val MintContainer: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.mintContainer
val WarmBg: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.bgTop
val WarmBgSoft: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.bgSoft
val WarmBgBottom: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.bgBottom
val CardWarm: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.card
val PinkAccent: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.pinkAccent
val PinkContainer: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.pinkContainer
val PinkDeep: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.pinkDeep
val SoilBrown: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.soilBrown
val PotBrown: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.potBrown
val PotRim: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.potRim
val InkDark: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.ink
val InkSecondary: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.inkSecondary
val InkTertiary: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.inkTertiary
val Amber: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.amber
val AmberContainer: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.amberContainer
val ErrorRed: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.error
val DropBlue: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.dropBlue
val WaterBlue: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.waterBlue
val FertilizeGreen: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.fertilizeGreen
val Hairline: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.hairline
val SageRing: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.sageRing
val GlowMint: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.glowMint
val GlowPink: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.glowPink
val GlowAmber: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.glowAmber
val WarnSurface: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.warnSurface
val CriticSurface: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.criticSurface
val BadgeWarn: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.badgeWarn
val BadgeWarnFg: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.badgeWarnFg
val BadgeYellowFg: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.badgeYellowFg
val HeatColors: List<Color> @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.heat

// —— 场景色（“我的花园”橱窗）——
val GrassTop: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.grassTop
val GrassBottom: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.grassBottom
val GrassBlade: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.grassBlade
val SceneShadow: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.sceneShadow
val SunCore: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.sunCore
val SunGlow: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.sunGlow
val SunRay: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.sunRay
val SunHalo: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.sunHalo
val MoonCore: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.moonCore
val MoonGlow: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.moonGlow
val StarTint: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.starTint
val CloudBody: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.cloudBody
val SnowBody: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.snowBody
val PlateSurface: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.plateSurface
val ChipSurface: Color @Composable @ReadOnlyComposable get() = LocalGardenPalette.current.chipSurface

@Composable
@ReadOnlyComposable
fun heatColor(count: Int): Color = when {
    count <= 0 -> HeatColors[0]
    count == 1 -> HeatColors[1]
    count <= 3 -> HeatColors[2]
    count <= 5 -> HeatColors[3]
    else -> HeatColors[4]
}

// ======================================================================
// Scheme 工厂
// ======================================================================

private val GardenShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

private val GardenTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 40.sp, lineHeight = 46.sp, letterSpacing = (-0.5).sp),
    headlineLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 36.sp, letterSpacing = (-0.3).sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.2).sp),
    titleLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 27.sp, letterSpacing = (-0.1).sp),
    titleMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 23.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 10.sp, lineHeight = 14.sp)
)

private val LightScheme = lightColorScheme(
    primary = LightGardenPalette.mintDeep,
    onPrimary = Color.White,
    primaryContainer = LightGardenPalette.mintContainer,
    onPrimaryContainer = LightGardenPalette.mintDeep,
    secondary = LightGardenPalette.pinkAccent,
    onSecondary = Color.White,
    secondaryContainer = LightGardenPalette.pinkContainer,
    onSecondaryContainer = LightGardenPalette.pinkDeep,
    background = LightGardenPalette.bgTop,
    onBackground = LightGardenPalette.ink,
    surface = LightGardenPalette.card,
    onSurface = LightGardenPalette.ink,
    surfaceVariant = Color(0xFFECEDE4),
    onSurfaceVariant = LightGardenPalette.inkSecondary,
    outline = Color(0xFFD8D3C4),
    error = LightGardenPalette.error,
    errorContainer = LightGardenPalette.pinkContainer
)

private val DarkScheme = darkColorScheme(
    primary = DarkGardenPalette.mintDeep,
    onPrimary = DarkGardenPalette.bgTop,
    primaryContainer = DarkGardenPalette.mintContainer,
    onPrimaryContainer = DarkGardenPalette.mintDeep,
    secondary = DarkGardenPalette.pinkAccent,
    onSecondary = DarkGardenPalette.bgTop,
    secondaryContainer = DarkGardenPalette.pinkContainer,
    onSecondaryContainer = DarkGardenPalette.pinkDeep,
    background = DarkGardenPalette.bgTop,
    onBackground = DarkGardenPalette.ink,
    surface = DarkGardenPalette.card,
    onSurface = DarkGardenPalette.ink,
    surfaceVariant = DarkGardenPalette.bgSoft,
    onSurfaceVariant = DarkGardenPalette.inkSecondary,
    outline = DarkGardenPalette.hairline,
    error = DarkGardenPalette.error,
    errorContainer = DarkGardenPalette.pinkContainer
)

@Composable
fun CodeGardenTheme(dark: Boolean = false, content: @Composable () -> Unit) {
    val palette = if (dark) DarkGardenPalette else LightGardenPalette
    androidx.compose.runtime.CompositionLocalProvider(LocalGardenPalette provides palette) {
        MaterialTheme(
            colorScheme = if (dark) DarkScheme else LightScheme,
            shapes = GardenShapes,
            typography = GardenTypography,
            content = content
        )
    }
}
