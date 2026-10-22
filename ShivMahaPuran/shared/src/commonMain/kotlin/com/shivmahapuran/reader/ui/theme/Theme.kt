package com.shivmahapuran.reader.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.shivmahapuran.reader.domain.model.AppLanguage

/**
 * Palette of Neelkantha — Shiva whose throat turned blue holding the halahala poison: deep poison
 * blue for headings, a brighter halahala blue for accents, and gold for ornaments.
 * (Property names are kept from the shared design system: maroon = primary ink, saffron = accent.)
 */
@Immutable
data class PuranColors(
    val night: Boolean,
    val ink: Color,
    val inkSoft: Color,
    val maroon: Color,
    val saffron: Color,
    val gold: Color,
    val goldSoft: Color,
    val card: Color,
    val cardBorder: Color,
    val pageOverlay: Color,
    val onAccent: Color,
    /** Opaque surface for bottom bars (tab bar, ad strip) so scrolling content never shows through. */
    val barSurface: Color,
    /** Tint applied to the parchment background image. */
    val parchmentDark: Color,
    val parchmentLight: Color,
)

val DayColors = PuranColors(
    night = false,
    ink = Color(0xFF13233B),
    inkSoft = Color(0xFF3C5675),
    maroon = Color(0xFF173E73),
    saffron = Color(0xFF2B6CB0),
    gold = Color(0xFFA9822B),
    goldSoft = Color(0x402B6CB0),
    card = Color(0x80F2F7FC),
    cardBorder = Color(0x80557FB0),
    pageOverlay = Color.Transparent,
    onAccent = Color(0xFFF4F8FC),
    barSurface = Color(0xFFDCE7F3),
    parchmentDark = Color(0xFF1B3A64),
    parchmentLight = Color(0xFFE6EEF8),
)

val NightColors = PuranColors(
    night = true,
    ink = Color(0xFFDCE6F2),
    inkSoft = Color(0xFFA6B8CF),
    maroon = Color(0xFF9CC3F0),
    saffron = Color(0xFF6FA8E8),
    gold = Color(0xFFD4B060),
    goldSoft = Color(0x406FA8E8),
    card = Color(0x40000814),
    cardBorder = Color(0x664F78A8),
    pageOverlay = Color(0xE00A1322),
    onAccent = Color(0xFF0B1626),
    barSurface = Color(0xFF0E1A2C),
    parchmentDark = Color(0xFF0A1830),
    parchmentLight = Color(0xFF3A5A82),
)

val LocalPuranColors = staticCompositionLocalOf { DayColors }
val LocalPuranFonts = staticCompositionLocalOf<PuranFonts> { error("PuranFonts not provided") }

object PuranTheme {
    val colors: PuranColors @Composable get() = LocalPuranColors.current
    val fonts: PuranFonts @Composable get() = LocalPuranFonts.current
}

@Composable
fun PuranTheme(language: AppLanguage, night: Boolean, content: @Composable () -> Unit) {
    val colors = if (night) NightColors else DayColors
    val fonts = rememberPuranFonts(language)
    val scheme = if (night) {
        darkColorScheme(
            primary = colors.saffron, onPrimary = colors.onAccent, secondary = colors.gold,
            background = Color(0xFF0A1322), surface = Color(0xFF13213A), onSurface = colors.ink,
            onBackground = colors.ink, surfaceVariant = Color(0xFF1B2C48), onSurfaceVariant = colors.inkSoft,
            outline = colors.cardBorder, secondaryContainer = colors.goldSoft, onSecondaryContainer = colors.ink,
            surfaceContainer = Color(0xFF13213A), surfaceContainerHigh = Color(0xFF1B2C48),
            surfaceContainerLow = Color(0xFF0F1B30),
        )
    } else {
        lightColorScheme(
            primary = colors.maroon, onPrimary = colors.onAccent, secondary = colors.saffron,
            background = Color(0xFFE6EEF8), surface = Color(0xFFF2F7FC), onSurface = colors.ink,
            onBackground = colors.ink, surfaceVariant = Color(0xFFDCE7F3), onSurfaceVariant = colors.inkSoft,
            outline = colors.cardBorder, secondaryContainer = Color(0x4D2B6CB0), onSecondaryContainer = colors.ink,
            surfaceContainer = Color(0xFFE9F0F8), surfaceContainerHigh = Color(0xFFDCE7F3),
            surfaceContainerLow = Color(0xFFF2F7FC),
        )
    }
    CompositionLocalProvider(LocalPuranColors provides colors, LocalPuranFonts provides fonts) {
        MaterialTheme(colorScheme = scheme, typography = typography(fonts.body, fonts.heading), content = content)
    }
}

private fun typography(body: FontFamily, heading: FontFamily): Typography {
    val base = Typography()
    fun TextStyle.withBody() = copy(fontFamily = body)
    fun TextStyle.withHeading() = copy(fontFamily = heading)
    return Typography(
        displayLarge = base.displayLarge.withHeading(),
        displayMedium = base.displayMedium.withHeading(),
        displaySmall = base.displaySmall.withHeading(),
        headlineLarge = base.headlineLarge.withHeading().copy(fontWeight = FontWeight.SemiBold),
        headlineMedium = base.headlineMedium.withHeading().copy(fontWeight = FontWeight.SemiBold),
        headlineSmall = base.headlineSmall.withHeading().copy(fontWeight = FontWeight.SemiBold),
        titleLarge = base.titleLarge.withHeading().copy(fontWeight = FontWeight.SemiBold),
        titleMedium = base.titleMedium.withHeading().copy(fontWeight = FontWeight.SemiBold),
        titleSmall = base.titleSmall.withHeading(),
        bodyLarge = base.bodyLarge.withBody().copy(fontSize = 17.sp, lineHeight = 27.sp),
        bodyMedium = base.bodyMedium.withBody().copy(fontSize = 15.sp, lineHeight = 23.sp),
        bodySmall = base.bodySmall.withBody(),
        labelLarge = base.labelLarge.withBody().copy(fontWeight = FontWeight.SemiBold),
        labelMedium = base.labelMedium.withBody(),
        labelSmall = base.labelSmall.withBody(),
    )
}
