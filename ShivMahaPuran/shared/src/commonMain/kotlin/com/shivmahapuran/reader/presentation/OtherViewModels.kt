package com.shivmahapuran.reader.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shivmahapuran.reader.ads.AdsState
import com.shivmahapuran.reader.domain.model.AppLanguage
import com.shivmahapuran.reader.domain.model.AppSettings
import com.shivmahapuran.reader.domain.model.Book
import com.shivmahapuran.reader.domain.model.BookSection
import com.shivmahapuran.reader.domain.model.BookmarkItem
import com.shivmahapuran.reader.domain.model.SearchResult
import com.shivmahapuran.reader.domain.model.ShlokaScript
import com.shivmahapuran.reader.domain.repository.PuranRepository
import com.shivmahapuran.reader.domain.repository.SettingsRepository
import com.shivmahapuran.reader.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BookmarksUiState(
    val loading: Boolean = true,
    val settings: AppSettings = AppSettings(),
    val bookmarks: List<BookmarkItem> = emptyList(),
)

class BookmarksViewModel(
    settingsRepository: SettingsRepository,
    private val userDataRepository: UserDataRepository,
) : ViewModel() {
    val state: StateFlow<BookmarksUiState> = combine(settingsRepository.settings, userDataRepository.bookmarks) { s, b ->
        BookmarksUiState(false, s, b)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BookmarksUiState())

    fun remove(item: BookmarkItem) {
        viewModelScope.launch { userDataRepository.toggleBookmark(item.chapter.id, item.verseNumber) }
    }
}

data class SearchUiState(
    val query: String = "",
    val settings: AppSettings = AppSettings(),
    val searching: Boolean = false,
    val results: List<SearchResult> = emptyList(),
)

class SearchViewModel(
    private val puranRepository: PuranRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val outcome = query.debounce(300).mapLatest { q -> q to puranRepository.search(q) }

    val state: StateFlow<SearchUiState> = combine(query, settingsRepository.settings, outcome) { q, s, (searched, results) ->
        SearchUiState(
            query = q,
            settings = s,
            searching = q.trim().length >= 2 && q != searched,
            results = if (q.isBlank()) emptyList() else results,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

    fun onQueryChange(value: String) {
        query.value = value
    }
}

data class SettingsUiState(val settings: AppSettings = AppSettings(), val privacyOptionsRequired: Boolean = false)

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {
    val state: StateFlow<SettingsUiState> = combine(settingsRepository.settings, AdsState.privacyOptionsRequired) { s, p ->
        SettingsUiState(s, p)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setLanguage(language: AppLanguage) = launch { settingsRepository.setLanguage(language) }
    fun setFontScale(scale: Float) = launch { settingsRepository.setFontScale(scale) }
    fun setShowTransliteration(show: Boolean) = launch { settingsRepository.setShowTransliteration(show) }
    fun setShowMeanings(show: Boolean) = launch { settingsRepository.setShowMeanings(show) }
    fun setScript(script: ShlokaScript) = launch { settingsRepository.setShlokaScript(script) }
    fun setVersesPerPage(count: Int) = launch { settingsRepository.setVersesPerPage(count) }
    fun setKeepScreenOn(enabled: Boolean) = launch { settingsRepository.setKeepScreenOn(enabled) }
    fun setNightMode(enabled: Boolean) = launch { settingsRepository.setNightMode(enabled) }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}

data class LibraryUiState(val loading: Boolean = true, val settings: AppSettings = AppSettings(), val books: List<Book> = emptyList())

class LibraryViewModel(puranRepository: PuranRepository, settingsRepository: SettingsRepository) : ViewModel() {
    val state: StateFlow<LibraryUiState> = combine(settingsRepository.settings, flow { emit(puranRepository.books()) }) { s, b ->
        LibraryUiState(false, s, b)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())
}

data class BookUiState(
    val loading: Boolean = true,
    val settings: AppSettings = AppSettings(),
    val book: Book? = null,
    val sections: List<BookSection> = emptyList(),
)

class BookViewModel(
    val bookId: String,
    val startPage: Int,
    puranRepository: PuranRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {
    val state: StateFlow<BookUiState> = combine(
        settingsRepository.settings,
        flow { emit(puranRepository.book(bookId) to puranRepository.bookSections(bookId)) },
    ) { s, (book, sections) -> BookUiState(false, s, book, sections) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BookUiState())
}
