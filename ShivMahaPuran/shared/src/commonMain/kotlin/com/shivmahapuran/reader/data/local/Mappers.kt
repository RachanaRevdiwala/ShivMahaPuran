package com.shivmahapuran.reader.data.local

import com.shivmahapuran.reader.domain.model.AppLanguage
import com.shivmahapuran.reader.domain.model.Book
import com.shivmahapuran.reader.domain.model.BookSection
import com.shivmahapuran.reader.domain.model.Chapter
import com.shivmahapuran.reader.domain.model.LocalizedText
import com.shivmahapuran.reader.domain.model.Part
import com.shivmahapuran.reader.domain.model.Verse
import com.shivmahapuran.reader.db.Book as BookRow
import com.shivmahapuran.reader.db.Book_section as BookSectionRow
import com.shivmahapuran.reader.db.Chapter as ChapterRow
import com.shivmahapuran.reader.db.Part as PartRow
import com.shivmahapuran.reader.db.Verse as VerseRow

internal fun localizedMap(en: String?, hi: String?, gu: String?): Map<AppLanguage, String> = buildMap {
    en?.takeIf { it.isNotBlank() }?.let { put(AppLanguage.ENGLISH, it) }
    hi?.takeIf { it.isNotBlank() }?.let { put(AppLanguage.HINDI, it) }
    gu?.takeIf { it.isNotBlank() }?.let { put(AppLanguage.GUJARATI, it) }
}

internal fun PartRow.toDomain() = Part(
    id = id.toInt(), samhita = samhita.toInt(), khanda = khanda.toInt(),
    nameSanskrit = name_sanskrit,
    name = LocalizedText(name_en, name_hi, name_gu),
    description = LocalizedText(description_en, description_hi, description_gu),
    chapterCount = chapter_count.toInt(),
)

internal fun ChapterRow.toDomain() = Chapter(
    id = id.toInt(), partId = part_id.toInt(), number = number.toInt(),
    titleSanskrit = title_sanskrit, synopsisSanskrit = synopsis_sanskrit,
    title = localizedMap(title_en, title_hi, title_gu),
    summary = localizedMap(summary_en, summary_hi, summary_gu),
    verseCount = verse_count.toInt(), colophon = colophon, hasMeanings = has_meanings != 0L,
)

internal fun VerseRow.toDomain() = Verse(
    id = id, chapterId = chapter_id.toInt(), number = number.toInt(), speaker = speaker, sanskrit = sanskrit,
    meaning = localizedMap(meaning_en, meaning_hi, meaning_gu),
)

internal fun BookRow.toDomain() = Book(
    id = id, language = lang,
    title = LocalizedText(title_en, title_hi, title_gu),
    subtitle = LocalizedText(subtitle_en, subtitle_hi, subtitle_gu),
    author = LocalizedText(author_en, author_hi, author_gu),
    source = source, sectionCount = section_count.toInt(),
)

internal fun BookSectionRow.toDomain() = BookSection(
    index = idx.toInt(),
    title = localizedMap(title_en, title_hi, title_gu),
    sanskrit = sanskrit,
    text = localizedMap(text_en, text_hi, text_gu),
)
