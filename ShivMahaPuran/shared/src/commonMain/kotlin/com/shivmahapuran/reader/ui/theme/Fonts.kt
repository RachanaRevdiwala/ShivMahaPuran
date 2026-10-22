package com.shivmahapuran.reader.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.shivmahapuran.reader.domain.model.AppLanguage
import com.shivmahapuran.reader.domain.model.ShlokaScript
import com.shivmahapuran.reader.domain.model.resolve
import com.shivmahapuran.reader.resources.Res
import com.shivmahapuran.reader.resources.eb_garamond_italic
import com.shivmahapuran.reader.resources.eb_garamond_regular
import com.shivmahapuran.reader.resources.eb_garamond_semibold
import com.shivmahapuran.reader.resources.noto_serif_devanagari_bold
import com.shivmahapuran.reader.resources.noto_serif_devanagari_regular
import com.shivmahapuran.reader.resources.noto_serif_gujarati_bold
import com.shivmahapuran.reader.resources.noto_serif_gujarati_regular
import com.shivmahapuran.reader.resources.tiro_devanagari_sanskrit
import org.jetbrains.compose.resources.Font

@Immutable
data class PuranFonts(
    /** Body text in the current UI language. */
    val body: FontFamily,
    /** Headings in the current UI language. */
    val heading: FontFamily,
    /** Latin serif used for IAST transliteration and English. */
    val latin: FontFamily,
    val sanskritDevanagari: FontFamily,
    val sanskritGujarati: FontFamily,
    val devanagari: FontFamily,
    val gujarati: FontFamily,
) {
    fun shloka(script: ShlokaScript, language: AppLanguage): FontFamily =
        if (script.resolve(language) == ShlokaScript.GUJARATI) sanskritGujarati else sanskritDevanagari

    fun forLanguage(language: AppLanguage): FontFamily = when (language) {
        AppLanguage.ENGLISH -> latin
        AppLanguage.HINDI -> devanagari
        AppLanguage.GUJARATI -> gujarati
    }
}

@Composable
fun rememberPuranFonts(language: AppLanguage): PuranFonts {
    val latin = FontFamily(
        Font(Res.font.eb_garamond_regular, FontWeight.Normal),
        Font(Res.font.eb_garamond_semibold, FontWeight.SemiBold),
        Font(Res.font.eb_garamond_semibold, FontWeight.Bold),
        Font(Res.font.eb_garamond_italic, FontWeight.Normal, FontStyle.Italic),
    )
    val devanagari = FontFamily(
        Font(Res.font.noto_serif_devanagari_regular, FontWeight.Normal),
        Font(Res.font.noto_serif_devanagari_bold, FontWeight.SemiBold),
        Font(Res.font.noto_serif_devanagari_bold, FontWeight.Bold),
    )
    val gujarati = FontFamily(
        Font(Res.font.noto_serif_gujarati_regular, FontWeight.Normal),
        Font(Res.font.noto_serif_gujarati_bold, FontWeight.SemiBold),
        Font(Res.font.noto_serif_gujarati_bold, FontWeight.Bold),
    )
    val sanskrit = FontFamily(Font(Res.font.tiro_devanagari_sanskrit, FontWeight.Normal))
    val body = when (language) {
        AppLanguage.ENGLISH -> latin
        AppLanguage.HINDI -> devanagari
        AppLanguage.GUJARATI -> gujarati
    }
    return PuranFonts(
        body = body,
        heading = body,
        latin = latin,
        sanskritDevanagari = sanskrit,
        sanskritGujarati = gujarati,
        devanagari = devanagari,
        gujarati = gujarati,
    )
}
