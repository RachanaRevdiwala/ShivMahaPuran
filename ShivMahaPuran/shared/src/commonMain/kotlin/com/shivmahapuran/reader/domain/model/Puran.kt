package com.shivmahapuran.reader.domain.model

/** A samhita, or a khanda / bhaga of one (the Shiva Mahapurana has 12 such parts). */
data class Part(
    val id: Int,
    val samhita: Int,
    /** 0 when the samhita is not divided. */
    val khanda: Int,
    val nameSanskrit: String,
    val name: LocalizedText,
    val description: LocalizedText,
    val chapterCount: Int,
)

/** An adhyaya. [id] is the global reading order (1..457). */
data class Chapter(
    val id: Int,
    val partId: Int,
    val number: Int,
    val titleSanskrit: String,
    val synopsisSanskrit: String,
    val title: Map<AppLanguage, String>,
    val summary: Map<AppLanguage, String>,
    val verseCount: Int,
    val colophon: String,
    val hasMeanings: Boolean,
) {
    /** Localized title, falling back to the Sanskrit title / synopsis while a translation is pending. */
    fun displayTitle(language: AppLanguage): String =
        title[language] ?: titleSanskrit.ifBlank { synopsisSanskrit.take(60) }

    /** True when [displayTitle] already shows the whole synopsis, so it should not be repeated below it. */
    fun synopsisIsTitle(language: AppLanguage): Boolean =
        title[language] == null && titleSanskrit.isBlank() && synopsisSanskrit.length <= 60
}

data class Verse(
    val id: Long,
    val chapterId: Int,
    val number: Int,
    val speaker: String?,
    val sanskrit: String,
    val meaning: Map<AppLanguage, String>,
)

data class ChapterProgress(val chapter: Chapter, val read: Boolean)

data class PartProgress(val part: Part, val chaptersRead: Int) {
    val fraction: Float get() = if (part.chapterCount == 0) 0f else chaptersRead.toFloat() / part.chapterCount
}

data class ReadingPosition(val chapter: Chapter, val part: Part, val verseNumber: Int)

data class BookmarkItem(val chapter: Chapter, val part: Part, val verse: Verse?, val verseNumber: Int, val createdAt: Long)

sealed interface SearchResult {
    data class ChapterMatch(val chapter: Chapter, val part: Part) : SearchResult
    data class VerseMatch(val verse: Verse, val chapter: Chapter, val part: Part) : SearchResult
}

data class Book(
    val id: String,
    /** "sa" for stotras with Sanskrit verses, otherwise the language the book is written in. */
    val language: String,
    val title: LocalizedText,
    val subtitle: LocalizedText,
    val author: LocalizedText,
    val source: String,
    val sectionCount: Int,
)

data class BookSection(
    val index: Int,
    val title: Map<AppLanguage, String>,
    val sanskrit: String?,
    val text: Map<AppLanguage, String>,
) {
    fun textFor(language: AppLanguage): Pair<AppLanguage, String>? =
        text[language]?.let { language to it }
            ?: listOf(AppLanguage.ENGLISH, AppLanguage.HINDI, AppLanguage.GUJARATI).firstNotNullOfOrNull { l -> text[l]?.let { l to it } }

    fun titleFor(language: AppLanguage): String? = title[language] ?: title.values.firstOrNull()
}

/** True for a speaker line such as "सूत उवाच" or "ब्रह्मोवाच" (also in Gujarati script). */
fun String.isSpeakerLine(): Boolean = trim().trimEnd('।', '॥', ' ').let { it.endsWith("वाच") || it.endsWith("વાચ") || it.endsWith("ऊचुः") }
