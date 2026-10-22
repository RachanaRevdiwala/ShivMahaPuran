package com.shivmahapuran.reader.util

import com.shivmahapuran.reader.domain.model.AppLanguage

object TextNormalizer {
    private val diacritics = mapOf(
        'ā' to "a", 'ī' to "i", 'ū' to "u", 'ṛ' to "ri", 'ṝ' to "ri", 'ḷ' to "l", 'ṅ' to "n", 'ñ' to "n",
        'ṭ' to "t", 'ḍ' to "d", 'ṇ' to "n", 'ś' to "sh", 'ṣ' to "sh", 'ṁ' to "m", 'ṃ' to "m", 'ḥ' to "h",
        'é' to "e", 'ē' to "e", 'ō' to "o", '’' to "", '\'' to "", '-' to "", ' ' to " ",
    )

    /** Lower-cases and strips IAST diacritics and hyphens so "kṣhetre" matches "kshetre". */
    fun plain(text: String): String = buildString {
        for (ch in text.lowercase()) append(diacritics[ch] ?: ch.toString())
    }.replace("shh", "sh").replace(Regex("\\s+"), " ")

    /** Escapes LIKE wildcards; the queries declare '\' as the escape character. */
    fun escapeLike(text: String): String =
        text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
}

private const val DEVANAGARI_ZERO = 0x0966
private const val GUJARATI_ZERO = 0x0AE6

/** Renders a number with the digits of the given language (१२ / ૧૨ / 12). */
fun Int.localized(language: AppLanguage): String {
    val zero = when (language) {
        AppLanguage.ENGLISH -> return toString()
        AppLanguage.HINDI -> DEVANAGARI_ZERO
        AppLanguage.GUJARATI -> GUJARATI_ZERO
    }
    return toString().map { if (it.isDigit()) Char(zero + (it - '0')) else it }.joinToString("")
}
