package com.shivmahapuran.reader.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shivmahapuran.reader.domain.model.AppSettings
import com.shivmahapuran.reader.domain.model.Book
import com.shivmahapuran.reader.domain.model.Chapter
import com.shivmahapuran.reader.domain.model.Part
import com.shivmahapuran.reader.domain.model.PartProgress
import com.shivmahapuran.reader.domain.model.ReadingPosition
import com.shivmahapuran.reader.domain.repository.PuranRepository
import com.shivmahapuran.reader.domain.repository.SettingsRepository
import com.shivmahapuran.reader.domain.repository.UserDataRepository
import com.shivmahapuran.reader.platform.localEpochDay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val loading: Boolean = true,
    val settings: AppSettings = AppSettings(),
    val chapterOfTheDay: Chapter? = null,
    val chapterOfTheDayPart: Part? = null,
    val lastPosition: ReadingPosition? = null,
    val parts: List<PartProgress> = emptyList(),
    val chaptersRead: Int = 0,
    val totalChapters: Int = 0,
    val featuredStotra: Book? = null,
)

class HomeViewModel(
    puranRepository: PuranRepository,
    settingsRepository: SettingsRepository,
    userDataRepository: UserDataRepository,
) : ViewModel() {

    val state: StateFlow<HomeUiState> = combine(
        settingsRepository.settings,
        userDataRepository.readChapterIds,
        userDataRepository.lastPosition,
    ) { settings, readIds, last ->
        val parts = puranRepository.parts()
        val chapters = puranRepository.chapters()
        val today = puranRepository.chapterOfTheDay(localEpochDay())
        HomeUiState(
            loading = false,
            settings = settings,
            chapterOfTheDay = today,
            chapterOfTheDayPart = today?.let { c -> parts.firstOrNull { it.id == c.partId } },
            lastPosition = last,
            parts = parts.map { p -> PartProgress(p, chapters.count { it.partId == p.id && it.id in readIds }) },
            chaptersRead = readIds.size,
            totalChapters = chapters.size,
            featuredStotra = puranRepository.books().firstOrNull(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}
