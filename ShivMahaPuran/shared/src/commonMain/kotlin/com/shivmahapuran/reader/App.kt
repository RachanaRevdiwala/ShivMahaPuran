package com.shivmahapuran.reader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shivmahapuran.reader.ads.InterstitialPolicy
import com.shivmahapuran.reader.ads.requestAdsConsent
import com.shivmahapuran.reader.domain.model.AppLanguage
import com.shivmahapuran.reader.navigation.AppNavHost
import com.shivmahapuran.reader.platform.LocalAppLocale
import com.shivmahapuran.reader.platform.currentTimeMillis
import com.shivmahapuran.reader.presentation.AppUiState
import com.shivmahapuran.reader.presentation.AppViewModel
import com.shivmahapuran.reader.ui.screens.ErrorScreen
import com.shivmahapuran.reader.ui.screens.LoadingScreen
import com.shivmahapuran.reader.ui.screens.OnboardingScreen
import com.shivmahapuran.reader.ui.theme.PuranTheme
import org.koin.compose.viewmodel.koinViewModel

/** Shared entry point rendered by both the Android activity and the iOS view controller. */
@Composable
fun App() {
    val viewModel: AppViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { InterstitialPolicy.onSessionStart(currentTimeMillis()) }

    val ready = state as? AppUiState.Ready
    val language = ready?.settings?.language
    val systemLanguage = remember { AppLanguage.fromCode(LocalAppLocaleSnapshot.systemCode) ?: AppLanguage.ENGLISH }
    val effective = language ?: systemLanguage

    val onboarded = ready != null && !ready.needsOnboarding
    LaunchedEffect(onboarded) { if (onboarded) requestAdsConsent() }

    CompositionLocalProvider(LocalAppLocale provides effective.code) {
        // Re-create the tree when the language changes so every string resource re-resolves.
        key(effective) {
            PuranTheme(language = effective, night = ready?.settings?.nightMode == true) {
                when {
                    state is AppUiState.Loading -> LoadingScreen()
                    state is AppUiState.Error -> ErrorScreen(onRetry = viewModel::prepareContent)
                    ready != null && ready.needsOnboarding -> OnboardingScreen(systemLanguage, viewModel::chooseLanguage)
                    else -> AppNavHost()
                }
            }
        }
    }
}

/** System language captured once at start-up, before any in-app override is applied. */
internal object LocalAppLocaleSnapshot {
    var systemCode: String? = null
}
