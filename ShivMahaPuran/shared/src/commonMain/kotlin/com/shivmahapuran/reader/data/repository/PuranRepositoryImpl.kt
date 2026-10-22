package com.shivmahapuran.reader.data.repository

import com.shivmahapuran.reader.data.local.ContentSeeder
import com.shivmahapuran.reader.data.local.toDomain
import com.shivmahapuran.reader.db.PuranDatabase
import com.shivmahapuran.reader.domain.model.Book
import com.shivmahapuran.reader.domain.model.BookSection
import com.shivmahapuran.reader.domain.model.Chapter
import com.shivmahapuran.reader.domain.model.Part
import com.shivmahapuran.reader.domain.model.SearchResult
import com.shivmahapuran.reader.domain.model.Verse
import com.shivmahapuran.reader.domain.repository.PuranRepository
import com.shivmahapuran.reader.util.TextNormalizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

internal class PuranRepositoryImpl(
    private val database: PuranDatabase,
    private val seeder: ContentSeeder,
) : PuranRepository {

    private val queries get() = database.puranQueries

    // Parts and chapter metadata never change at runtime: cache them in memory.
    private var partCache: List<Part>? = null
    private var chapterCache: List<Chapter>? = null

    override suspend fun ensureContentReady() = seeder.seedCoreIfNeeded()

    override suspend fun prepareAllVerses() = seeder.seedAllParts()

    override suspend fun parts(): List<Part> = io {
        partCache ?: queries.selectParts().executeAsList().map { it.toDomain() }.also { partCache = it }
    }

    override suspend fun part(id: Int): Part? = parts().firstOrNull { it.id == id }

    override suspend fun chapters(): List<Chapter> = io {
        chapterCache ?: queries.selectChapters().executeAsList().map { it.toDomain() }.also { chapterCache = it }
    }

    override suspend fun chaptersOfPart(partId: Int): List<Chapter> = chapters().filter { it.partId == partId }

    override suspend fun chapter(id: Int): Chapter? = chapters().getOrNull(id - 1)?.takeIf { it.id == id }
        ?: chapters().firstOrNull { it.id == id }

    override suspend fun verses(chapterId: Int): List<Verse> {
        val chapter = chapter(chapterId) ?: return emptyList()
        seeder.seedPartIfNeeded(chapter.partId)
        return io { queries.selectVersesForChapter(chapterId.toLong()).executeAsList().map { it.toDomain() } }
    }

    override suspend fun chapterOfTheDay(epochDay: Long): Chapter? {
        val all = chapters()
        if (all.isEmpty()) return null
        // 211 is coprime with 457, so consecutive days walk through every chapter in a varied order.
        val index = ((epochDay * 211) % all.size + all.size) % all.size
        return all[index.toInt()]
    }

    override suspend fun search(query: String): List<SearchResult> {
        val trimmed = query.trim()
        if (trimmed.length < 2) return emptyList()
        val escaped = TextNormalizer.escapeLike(trimmed)
        val parts = parts().associateBy { it.id }
        val chapters = chapters().associateBy { it.id }
        val chapterHits = io { queries.searchChapters(escaped).executeAsList() }
            .mapNotNull { id -> chapters[id.toInt()]?.let { c -> parts[c.partId]?.let { SearchResult.ChapterMatch(c, it) } } }
        // Verse search only covers parts already seeded (all of them shortly after first launch).
        val verseHits = io { queries.searchVerses(escaped).executeAsList() }.mapNotNull { row ->
            val c = chapters[row.chapter_id.toInt()] ?: return@mapNotNull null
            val p = parts[c.partId] ?: return@mapNotNull null
            SearchResult.VerseMatch(row.toDomain(), c, p)
        }
        return chapterHits + verseHits
    }

    override suspend fun books(): List<Book> = io { queries.selectBooks().executeAsList().map { it.toDomain() } }

    override suspend fun book(id: String): Book? = io { queries.selectBook(id).executeAsOneOrNull()?.toDomain() }

    override suspend fun bookSections(id: String): List<BookSection> =
        io { queries.selectBookSections(id).executeAsList().map { it.toDomain() } }

    private suspend fun <T> io(block: suspend () -> T): T = withContext(Dispatchers.IO) { block() }
}
