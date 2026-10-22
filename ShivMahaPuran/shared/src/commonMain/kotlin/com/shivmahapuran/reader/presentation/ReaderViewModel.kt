package com.shivmahapuran.reader.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shivmahapuran.reader.domain.model.AppSettings
import com.shivmahapuran.reader.domain.model.Chapter
import com.shivmahapuran.reader.domain.model.Part
import com.shivmahapuran.reader.domain.model.ShlokaScript
import com.shivmahapuran.reader.domain.model.Verse
import com.shivmahapuran.reader.domain.repository.PuranRepository
import com.shivmahapuran.reader.domain.repository.SettingsRepository
import com.shivmahapuran.reader.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** A page of the book-style reader. */
sealed interface ReaderPage {
    data object Opening : ReaderPage
    data class Shlokas(val verses: List<Verse>) : ReaderPage
    data object Closing : ReaderPage
}

data class ReaderUiState(
    val loading: Boolean = true,
    val settings: AppSettings = AppSettings(),
    val chapter: Chapter? = null,
    val part: Part? = null,
    val pages: List<ReaderPage> = emptyList(),
    val bookmarkKeys: Set<Pair<Int, Int>> = emptySet(),
    val nextChapter: Chapter? = null,
    val nextChapterPart: Part? = null,
    val showOptions: Boolean = false,
)

/**
 * Drives the book-style reader for one adhyaya: an opening page (title + summary), pages of
 * shlokas, and a closing page with the colophon. Reading position is saved as pages settle.
 */
class ReaderViewModel(
    val chapterId: Int,
    /** 1-based verse to open at (0 or 1 opens the chapter's opening page). */
    val startVerse: Int,
    private val puranRepository: PuranRepository,
    private val settingsRepository: SettingsRepository,
    private val userDataRepository: UserDataRepository,
) : ViewModel() {

    private val showOptions = MutableStateFlow(false)

    private data class Content(val chapter: Chapter?, val part: Part?, val verses: List<Verse>, val next: Chapter?, val nextPart: Part?)

    private val content = flow {
        val chapter = puranRepository.chapter(chapterId)
        val part = chapter?.let { puranRepository.part(it.partId) }
        val next = puranRepository.chapter(chapterId + 1)
        emit(Content(chapter, part, puranRepository.verses(chapterId), next, next?.let { puranRepository.part(it.partId) }))
    }

    val state: StateFlow<ReaderUiState> = combine(
        settingsRepository.settings, content, userDataRepository.bookmarkKeys, showOptions,
    ) { settings, c, bookmarks, options ->
        ReaderUiState(
            loading = false,
            settings = settings,
            chapter = c.chapter,
            part = c.part,
            pages = buildList {
                add(ReaderPage.Opening)
                c.verses.chunked(settings.versesPerPage.coerceAtLeast(1)).forEach { add(ReaderPage.Shlokas(it)) }
                add(ReaderPage.Closing)
            },
            bookmarkKeys = bookmarks,
            nextChapter = c.next,
            nextChapterPart = c.nextPart,
            showOptions = options,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReaderUiState())

    /** Page to open first, for the given layout. */
    fun initialPage(versesPerPage: Int): Int =
        if (startVerse <= 1) 0 else 1 + (startVerse - 1) / versesPerPage.coerceAtLeast(1)

    fun onPageSettled(page: ReaderPage) {
        viewModelScope.launch {
            when (page) {
                is ReaderPage.Shlokas -> userDataRepository.savePosition(chapterId, page.verses.first().number)
                ReaderPage.Closing -> userDataRepository.markChapterRead(chapterId)
                ReaderPage.Opening -> userDataRepository.savePosition(chapterId, 1)
            }
        }
    }

    fun toggleBookmark(verseNumber: Int) {
        viewModelScope.launch { userDataRepository.toggleBookmark(chapterId, verseNumber) }
    }

    fun setShowOptions(show: Boolean) {
        showOptions.value = show
    }

    fun setFontScale(scale: Float) = launch { settingsRepository.setFontScale(scale) }
    fun setShowTransliteration(show: Boolean) = launch { settingsRepository.setShowTransliteration(show) }
    fun setShowMeanings(show: Boolean) = launch { settingsRepository.setShowMeanings(show) }
    fun setNightMode(enabled: Boolean) = launch { settingsRepository.setNightMode(enabled) }
    fun setScript(script: ShlokaScript) = launch { settingsRepository.setShlokaScript(script) }
    fun setVersesPerPage(count: Int) = launch { settingsRepository.setVersesPerPage(count) }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
