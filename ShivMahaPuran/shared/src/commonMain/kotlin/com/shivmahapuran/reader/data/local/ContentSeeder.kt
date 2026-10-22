package com.shivmahapuran.reader.data.local

import com.shivmahapuran.reader.db.Book
import com.shivmahapuran.reader.db.Book_section
import com.shivmahapuran.reader.db.Chapter
import com.shivmahapuran.reader.db.Part
import com.shivmahapuran.reader.db.PuranDatabase
import com.shivmahapuran.reader.db.Verse
import com.shivmahapuran.reader.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Copies the bundled JSON into SQLite.
 *
 * The core file (parts, chapter titles and summaries, library — a few hundred KB) is seeded on
 * first launch. The ~24,000 Sanskrit verses live in one file per part and are seeded separately —
 * in the background after launch, or on demand when a chapter of that part is opened first — so
 * no large file is ever parsed at once. User tables are never touched.
 */
internal class ContentSeeder(private val database: PuranDatabase) {

    private val coreMutex = Mutex()
    private val verseMutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true }
    private val queries get() = database.puranQueries

    suspend fun seedCoreIfNeeded() = coreMutex.withLock {
        withContext(Dispatchers.IO) {
            val stored = queries.selectPreference(KEY_CONTENT_VERSION).executeAsOneOrNull()?.toIntOrNull()
            val populated = queries.selectChapterCount().executeAsOne() > 0
            if (stored == BUNDLED_CONTENT_VERSION && populated) return@withContext

            val core = json.decodeFromString<CoreDto>(Res.readBytes(CORE_PATH).decodeToString())
            check(core.contentVersion == BUNDLED_CONTENT_VERSION) {
                "shivpuran_core.json is version ${core.contentVersion}, app expects $BUNDLED_CONTENT_VERSION"
            }
            database.transaction {
                queries.deleteAllVerses()
                queries.deleteAllChapters()
                queries.deleteAllParts()
                queries.deleteAllBookSections()
                queries.deleteAllBooks()
                core.parts.forEach { p ->
                    queries.upsertPreference(versesKey(p.id), "0")
                    queries.insertPart(
                        Part(
                            id = p.id.toLong(), samhita = p.samhita.toLong(), khanda = p.khanda.toLong(),
                            name_sanskrit = p.nameSanskrit,
                            name_en = p.name.en, name_hi = p.name.hi, name_gu = p.name.gu,
                            description_en = p.description.en, description_hi = p.description.hi, description_gu = p.description.gu,
                            chapter_count = p.chapterCount.toLong(),
                        )
                    )
                }
                core.chapters.forEach { c ->
                    queries.insertChapter(
                        Chapter(
                            id = c.id.toLong(), part_id = c.part.toLong(), number = c.number.toLong(),
                            title_sanskrit = c.titleSanskrit, synopsis_sanskrit = c.synopsisSanskrit,
                            title_en = c.title.en, title_hi = c.title.hi, title_gu = c.title.gu,
                            summary_en = c.summary.en, summary_hi = c.summary.hi, summary_gu = c.summary.gu,
                            verse_count = c.verseCount.toLong(), colophon = c.colophon,
                            has_meanings = if (c.hasMeanings) 1L else 0L,
                        )
                    )
                }
                core.books.forEachIndexed { order, b ->
                    queries.insertBook(
                        Book(
                            id = b.id, lang = b.language,
                            title_en = b.title.en, title_hi = b.title.hi, title_gu = b.title.gu,
                            subtitle_en = b.subtitle.en, subtitle_hi = b.subtitle.hi, subtitle_gu = b.subtitle.gu,
                            author_en = b.author.en, author_hi = b.author.hi, author_gu = b.author.gu,
                            source = b.source, sort_order = order.toLong(), section_count = b.sections.size.toLong(),
                        )
                    )
                    b.sections.forEachIndexed { i, s ->
                        queries.insertBookSection(
                            Book_section(
                                book_id = b.id, idx = i.toLong(),
                                title_en = s.title?.en, title_hi = s.title?.hi, title_gu = s.title?.gu,
                                sanskrit = s.sanskrit, text_en = s.text.en, text_hi = s.text.hi, text_gu = s.text.gu,
                            )
                        )
                    }
                }
                queries.upsertPreference(KEY_CONTENT_VERSION, BUNDLED_CONTENT_VERSION.toString())
            }
        }
    }

    /** Seeds one part's verses if they are missing or from an older content version. */
    suspend fun seedPartIfNeeded(partId: Int) = verseMutex.withLock {
        withContext(Dispatchers.IO) {
            val stored = queries.selectPreference(versesKey(partId)).executeAsOneOrNull()?.toIntOrNull()
            if (stored == BUNDLED_CONTENT_VERSION) return@withContext
            val file = json.decodeFromString<VerseFileDto>(
                Res.readBytes("files/verses/part_${partId.toString().padStart(2, '0')}.json").decodeToString()
            )
            check(file.contentVersion == BUNDLED_CONTENT_VERSION && file.part == partId) {
                "verse file for part $partId does not match content version $BUNDLED_CONTENT_VERSION"
            }
            database.transaction {
                queries.deleteVersesForPart(partId.toLong())
                file.verses.forEach { v ->
                    queries.insertVerse(
                        Verse(
                            id = v.id, chapter_id = v.chapter.toLong(), number = v.number.toLong(),
                            speaker = v.speaker, sanskrit = v.sanskrit,
                            meaning_en = v.meaning?.en, meaning_hi = v.meaning?.hi, meaning_gu = v.meaning?.gu,
                        )
                    )
                }
                queries.upsertPreference(versesKey(partId), BUNDLED_CONTENT_VERSION.toString())
            }
        }
    }

    suspend fun seedAllParts() {
        for (part in 1..PART_COUNT) seedPartIfNeeded(part)
    }

    private fun versesKey(partId: Int) = "verses_version_part_$partId"

    companion object {
        /** Must match `CONTENT_VERSION` in tools/build_content.py. Bump both when content changes. */
        const val BUNDLED_CONTENT_VERSION = 2
        const val PART_COUNT = 12
        private const val CORE_PATH = "files/shivpuran_core.json"
        private const val KEY_CONTENT_VERSION = "content_version"
    }
}
