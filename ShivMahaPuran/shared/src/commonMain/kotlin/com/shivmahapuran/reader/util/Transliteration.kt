package com.shivmahapuran.reader.util

/** Script conversions for Sanskrit text stored in Devanagari. */
object Transliteration {
    private const val GUJARATI_OFFSET = 0x0A80 - 0x0900

    // Devanagari letters with no Gujarati counterpart are left unchanged; dandas are shared.
    private val noGujarati = setOf(0x0904, 0x090E, 0x0912, 0x0929, 0x0931, 0x0934, 0x0946, 0x094A, 0x0964, 0x0965)

    private fun gujaratiMappable(cp: Int): Boolean = cp !in noGujarati && (
        cp in 0x0901..0x0939 || cp in 0x093C..0x094D || cp == 0x0950 || cp in 0x0960..0x0963 || cp in 0x0966..0x096F
    )

    /** Devanagari → Gujarati script, code point for code point (the blocks are aligned 0x180 apart). */
    fun toGujarati(text: String): String = buildString(text.length) {
        for (ch in text) append(if (gujaratiMappable(ch.code)) Char(ch.code + GUJARATI_OFFSET) else ch)
    }

    private val independentVowels = mapOf(
        'अ' to "a", 'आ' to "ā", 'इ' to "i", 'ई' to "ī", 'उ' to "u", 'ऊ' to "ū", 'ऋ' to "ṛ", 'ॠ' to "ṝ",
        'ऌ' to "ḷ", 'ए' to "e", 'ऐ' to "ai", 'ओ' to "o", 'औ' to "au",
    )
    private val vowelSigns = mapOf(
        'ा' to "ā", 'ि' to "i", 'ी' to "ī", 'ु' to "u", 'ू' to "ū", 'ृ' to "ṛ", 'ॄ' to "ṝ", 'ॢ' to "ḷ",
        'े' to "e", 'ै' to "ai", 'ो' to "o", 'ौ' to "au",
    )
    private val consonants = mapOf(
        'क' to "k", 'ख' to "kh", 'ग' to "g", 'घ' to "gh", 'ङ' to "ṅ",
        'च' to "c", 'छ' to "ch", 'ज' to "j", 'झ' to "jh", 'ञ' to "ñ",
        'ट' to "ṭ", 'ठ' to "ṭh", 'ड' to "ḍ", 'ढ' to "ḍh", 'ण' to "ṇ",
        'त' to "t", 'थ' to "th", 'द' to "d", 'ध' to "dh", 'न' to "n",
        'प' to "p", 'फ' to "ph", 'ब' to "b", 'भ' to "bh", 'म' to "m",
        'य' to "y", 'र' to "r", 'ल' to "l", 'व' to "v", 'ळ' to "ḻ",
        'श' to "ś", 'ष' to "ṣ", 'स' to "s", 'ह' to "h",
    )
    private val others = mapOf(
        'ं' to "ṃ", 'ः' to "ḥ", 'ँ' to "m̐", 'ऽ' to "'", '।' to "|", '॥' to "||", 'ॐ' to "oṃ",
        '०' to "0", '१' to "1", '२' to "2", '३' to "3", '४' to "4", '५' to "5", '६' to "6", '७' to "7", '८' to "8", '९' to "9",
    )

    /** Devanagari → IAST (International Alphabet of Sanskrit Transliteration). */
    fun toIast(text: String): String = buildString(text.length + text.length / 2) {
        var i = 0
        while (i < text.length) {
            val ch = text[i]
            val consonant = consonants[ch]
            if (consonant != null) {
                append(consonant)
                val next = text.getOrNull(i + 1)
                when {
                    next == '्' -> i++                                  // virama: no inherent vowel
                    next != null && vowelSigns.containsKey(next) -> { append(vowelSigns.getValue(next)); i++ }
                    next == '़' -> { i++; if (text.getOrNull(i + 1) != '्') append('a') }
                    else -> append('a')                                 // inherent vowel
                }
            } else {
                append(independentVowels[ch] ?: others[ch] ?: ch.toString())
            }
            i++
        }
    }
}
