package com.shivmahapuran.reader.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.shivmahapuran.reader.db.PuranDatabase
import com.shivmahapuran.reader.domain.model.BookmarkItem
import com.shivmahapuran.reader.domain.model.ReadingPosition
import com.shivmahapuran.reader.domain.repository.PuranRepository
import com.shivmahapuran.reader.domain.repository.UserDataRepository
import com.shivmahapuran.reader.platform.currentTimeMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class UserDataRepositoryImpl(
    private val database: PuranDatabase,
    private val puranRepository: PuranRepository,
) : UserDataRepository {

    private val queries get() = database.puranQueries

    private val bookmarkRows = queries.selectBookmarks().asFlow().mapToList(Dispatchers.IO)

    override val bookmarkKeys: Flow<Set<Pair<Int, Int>>> =
        bookmarkRows.map { rows -> rows.map { it.chapter_id.toInt() to it.verse_number.toInt() }.toSet() }

    override val bookmarks: Flow<List<BookmarkItem>> = bookmarkRows.map { rows ->
        rows.mapNotNull { row ->
            val chapter = puranRepository.chapter(row.chapter_id.toInt()) ?: return@mapNotNull null
            val part = puranRepository.part(chapter.partId) ?: return@mapNotNull null
            val verse = puranRepository.verses(chapter.id).firstOrNull { it.number == row.verse_number.toInt() }
            BookmarkItem(chapter, part, verse, row.verse_number.toInt(), row.created_at)
        }
    }

    override suspend fun toggleBookmark(chapterId: Int, verseNumber: Int) = withContext(Dispatchers.IO) {
        database.transaction {
            val exists = queries.selectBookmarks().executeAsList()
                .any { it.chapter_id.toInt() == chapterId && it.verse_number.toInt() == verseNumber }
            if (exists) queries.deleteBookmark(chapterId.toLong(), verseNumber.toLong())
            else queries.insertBookmark(chapterId.toLong(), verseNumber.toLong(), currentTimeMillis())
        }
    }

    override val readChapterIds: Flow<Set<Int>> =
        queries.selectReadChapterIds().asFlow().mapToList(Dispatchers.IO).map { ids -> ids.map { it.toInt() }.toSet() }

    override val lastPosition: Flow<ReadingPosition?> =
        queries.selectLastPosition().asFlow().mapToOneOrNull(Dispatchers.IO).map { row ->
            row ?: return@map null
            val chapter = puranRepository.chapter(row.chapter_id.toInt()) ?: return@map null
            val part = puranRepository.part(chapter.partId) ?: return@map null
            ReadingPosition(chapter, part, row.verse_number.toInt())
        }

    override suspend fun savePosition(chapterId: Int, verseNumber: Int): Unit = withContext(Dispatchers.IO) {
        queries.saveLastPosition(chapterId.toLong(), verseNumber.toLong(), currentTimeMillis())
    }

    override suspend fun markChapterRead(chapterId: Int): Unit = withContext(Dispatchers.IO) {
        queries.markChapterRead(chapterId.toLong(), currentTimeMillis())
    }
}
