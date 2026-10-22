package com.shivmahapuran.reader.domain.repository

import com.shivmahapuran.reader.domain.model.AppLanguage
import com.shivmahapuran.reader.domain.model.AppSettings
import com.shivmahapuran.reader.domain.model.Book
import com.shivmahapuran.reader.domain.model.BookSection
import com.shivmahapuran.reader.domain.model.BookmarkItem
import com.shivmahapuran.reader.domain.model.Chapter
import com.shivmahapuran.reader.domain.model.Part
import com.shivmahapuran.reader.domain.model.ReadingPosition
import com.shivmahapuran.reader.domain.model.SearchResult
import com.shivmahapuran.reader.domain.model.ShlokaScript
import com.shivmahapuran.reader.domain.model.Verse
import kotlinx.coroutines.flow.Flow

/** Read-only access to the Purana stored in the local SQLite database. */
interface PuranRepository {
    /** Seeds parts, chapters and the library from the bundled JSON if needed. */
    suspend fun ensureContentReady()
    /** Seeds every part's verses; run once in the background after launch. */
    suspend fun prepareAllVerses()

    suspend fun parts(): List<Part>
    suspend fun part(id: Int): Part?
    suspend fun chapters(): List<Chapter>
    suspend fun chaptersOfPart(partId: Int): List<Chapter>
    suspend fun chapter(id: Int): Chapter?
    /** Verses of a chapter (seeds the chapter's part first if needed). */
    suspend fun verses(chapterId: Int): List<Verse>
    suspend fun chapterOfTheDay(epochDay: Long): Chapter?
    suspend fun search(query: String): List<SearchResult>

    suspend fun books(): List<Book>
    suspend fun book(id: String): Book?
    suspend fun bookSections(id: String): List<BookSection>
}

/** Devotee's personal data: bookmarks and reading progress. */
interface UserDataRepository {
    val bookmarks: Flow<List<BookmarkItem>>
    val bookmarkKeys: Flow<Set<Pair<Int, Int>>>
    suspend fun toggleBookmark(chapterId: Int, verseNumber: Int)

    val readChapterIds: Flow<Set<Int>>
    val lastPosition: Flow<ReadingPosition?>
    suspend fun savePosition(chapterId: Int, verseNumber: Int)
    suspend fun markChapterRead(chapterId: Int)
}

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun current(): AppSettings
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setFontScale(scale: Float)
    suspend fun setShowTransliteration(show: Boolean)
    suspend fun setShowMeanings(show: Boolean)
    suspend fun setShlokaScript(script: ShlokaScript)
    suspend fun setVersesPerPage(count: Int)
    suspend fun setKeepScreenOn(enabled: Boolean)
    suspend fun setNightMode(enabled: Boolean)
}
