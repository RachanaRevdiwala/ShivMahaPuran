package com.shivmahapuran.reader.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shivmahapuran.reader.domain.model.AppLanguage
import com.shivmahapuran.reader.domain.model.AppSettings
import com.shivmahapuran.reader.domain.repository.PuranRepository
import com.shivmahapuran.reader.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AppUiState {
    data object Loading : AppUiState
    data object Error : AppUiState
    data class Ready(val settings: AppSettings) : AppUiState {
        val needsOnboarding: Boolean get() = settings.language == null
    }
}

/** Root ViewModel: prepares the database on launch and exposes global settings. */
class AppViewModel(
    private val puranRepository: PuranRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private enum class Status { Loading, Ready, Failed }

    private val status = MutableStateFlow(Status.Loading)

    val state: StateFlow<AppUiState> = combine(status, settingsRepository.settings) { s, settings ->
        when (s) {
            Status.Loading -> AppUiState.Loading
            Status.Failed -> AppUiState.Error
            Status.Ready -> AppUiState.Ready(settings)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AppUiState.Loading)

    init {
        prepareContent()
    }

    fun prepareContent() {
        status.value = Status.Loading
        viewModelScope.launch {
            status.value = try {
                puranRepository.ensureContentReady()
                Status.Ready
            } catch (e: Exception) {
                println("Content seeding failed: $e")
                Status.Failed
            }
            if (status.value == Status.Ready) {
                // ~24,000 shlokas are prepared part by part without blocking the UI; a part opened
                // before its turn is seeded on demand.
                runCatching { puranRepository.prepareAllVerses() }.onFailure { println("Verse seeding failed: $it") }
            }
        }
    }

    fun chooseLanguage(language: AppLanguage) {
        viewModelScope.launch { settingsRepository.setLanguage(language) }
    }
}
