package com.shivmahapuran.reader.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.shivmahapuran.reader.domain.model.AppSettings
import com.shivmahapuran.reader.domain.model.Chapter
import com.shivmahapuran.reader.domain.model.Part
import com.shivmahapuran.reader.domain.model.ShlokaScript
import com.shivmahapuran.reader.domain.model.Verse
import com.shivmahapuran.reader.domain.model.resolve
import com.shivmahapuran.reader.platform.shareText
import com.shivmahapuran.reader.resources.Res
import com.shivmahapuran.reader.resources.share_footer
import org.jetbrains.compose.resources.stringResource

private const val REF_TOKEN = "{ref}"

/** Shloka (in the reader's script), its meaning when available, and a reference such as "Rudra Samhita · Sati Khanda 12.4". */
fun formatVerseForSharing(verse: Verse, chapter: Chapter, part: Part, settings: AppSettings, footerTemplate: String): String {
    val language = settings.effectiveLanguage
    val gujarati = settings.shlokaScript.resolve(language) == ShlokaScript.GUJARATI
    val reference = "${part.name[language]} ${chapter.number.localized(language)}.${verse.number.localized(language)}"
    return buildString {
        appendLine(if (gujarati) Transliteration.toGujarati(verse.sanskrit) else verse.sanskrit)
        verse.meaning[language]?.let { appendLine(); appendLine(it) }
        appendLine()
        append(footerTemplate.replace(REF_TOKEN, reference))
    }
}

@Composable
fun rememberVerseSharer(): (Verse, Chapter, Part, AppSettings) -> Unit {
    val footer = stringResource(Res.string.share_footer, REF_TOKEN)
    return { v, c, p, s -> shareText(formatVerseForSharing(v, c, p, s, footer)) }
}

@Suppress("DEPRECATION")
@Composable
fun rememberVerseCopier(): (Verse, Chapter, Part, AppSettings) -> Unit {
    val clipboard = LocalClipboardManager.current
    val footer = stringResource(Res.string.share_footer, REF_TOKEN)
    return { v, c, p, s -> clipboard.setText(AnnotatedString(formatVerseForSharing(v, c, p, s, footer))) }
}
