package com.shivmahapuran.reader.domain.model

data class AppSettings(
    /** null until the devotee has picked a language on first launch. */
    val language: AppLanguage? = null,
    val fontScale: Float = 1f,
    val showTransliteration: Boolean = false,
    val showMeanings: Boolean = true,
    val shlokaScript: ShlokaScript = ShlokaScript.AUTO,
    /** Shlokas shown on one page of the reader. */
    val versesPerPage: Int = DEFAULT_VERSES_PER_PAGE,
    val keepScreenOn: Boolean = true,
    val nightMode: Boolean = false,
) {
    val effectiveLanguage: AppLanguage get() = language ?: AppLanguage.ENGLISH

    companion object {
        const val MIN_FONT_SCALE = 0.8f
        const val MAX_FONT_SCALE = 1.8f
        const val DEFAULT_VERSES_PER_PAGE = 6
        val VERSES_PER_PAGE_OPTIONS = listOf(1, 3, 6, 10)
    }
}
