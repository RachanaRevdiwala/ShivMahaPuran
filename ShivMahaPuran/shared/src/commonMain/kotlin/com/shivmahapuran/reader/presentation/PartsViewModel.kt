package com.shivmahapuran.reader.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shivmahapuran.reader.domain.model.AppSettings
import com.shivmahapuran.reader.domain.model.Chapter
import com.shivmahapuran.reader.domain.model.ChapterProgress
import com.shivmahapuran.reader.domain.model.Part
import com.shivmahapuran.reader.domain.model.PartProgress
import com.shivmahapuran.reader.domain.repository.PuranRepository
import com.shivmahapuran.reader.domain.repository.SettingsRepository
import com.shivmahapuran.reader.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class PartsUiState(
    val loading: Boolean = true,
    val settings: AppSettings = AppSettings(),
    val parts: List<PartProgress> = emptyList(),
)

/** The seven samhitas (twelve parts) of the Purana with reading progress. */
class PartsViewModel(
    puranRepository: PuranRepository,
    settingsRepository: SettingsRepository,
    userDataRepository: UserDataRepository,
) : ViewModel() {
    val state: StateFlow<PartsUiState> = combine(settingsRepository.settings, userDataRepository.readChapterIds) { settings, readIds ->
        val chapters = puranRepository.chapters()
        PartsUiState(
            loading = false,
            settings = settings,
            parts = puranRepository.parts().map { p -> PartProgress(p, chapters.count { it.partId == p.id && it.id in readIds }) },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PartsUiState())
}

data class PartDetailUiState(
    val loading: Boolean = true,
    val settings: AppSettings = AppSettings(),
    val part: Part? = null,
    val chapters: List<ChapterProgress> = emptyList(),
    /** First unread chapter, to continue the part from. */
    val nextChapter: Chapter? = null,
    val chaptersRead: Int = 0,
)

class PartDetailViewModel(
    private val partId: Int,
    puranRepository: PuranRepository,
    settingsRepository: SettingsRepository,
    userDataRepository: UserDataRepository,
) : ViewModel() {
    val state: StateFlow<PartDetailUiState> = combine(settingsRepository.settings, userDataRepository.readChapterIds) { settings, readIds ->
        val chapters = puranRepository.chaptersOfPart(partId)
        PartDetailUiState(
            loading = false,
            settings = settings,
            part = puranRepository.part(partId),
            chapters = chapters.map { ChapterProgress(it, it.id in readIds) },
            nextChapter = chapters.firstOrNull { it.id !in readIds } ?: chapters.firstOrNull(),
            chaptersRead = chapters.count { it.id in readIds },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PartDetailUiState())
}
