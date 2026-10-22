package com.shivmahapuran.reader.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Process-wide ad state. Platform code flips [canRequestAds] to true only after the
 * Google UMP consent flow has completed and the Mobile Ads SDK is initialised, so no
 * ad request is ever made without valid consent.
 */
object AdsState {
    private val _canRequestAds = MutableStateFlow(false)
    val canRequestAds: StateFlow<Boolean> = _canRequestAds.asStateFlow()

    private val _privacyOptionsRequired = MutableStateFlow(false)
    /** True when the devotee is in a region (e.g. EEA/UK) where a privacy-options entry point is required. */
    val privacyOptionsRequired: StateFlow<Boolean> = _privacyOptionsRequired.asStateFlow()

    fun onConsentResolved(canRequestAds: Boolean, privacyOptionsRequired: Boolean) {
        _canRequestAds.value = canRequestAds
        _privacyOptionsRequired.value = privacyOptionsRequired
    }
}

/** Anchored adaptive banner. Renders nothing until ads may be requested. */
@Composable
expect fun AdBanner(modifier: Modifier = Modifier)

/** Full-screen ads, shown only at natural pauses (completing a chapter) and frequency-capped. */
expect object InterstitialAds {
    fun preload()
    /** Shows a preloaded ad if one is ready, then calls [onFinished] (immediately when no ad is shown). */
    fun show(onFinished: () -> Unit)
}

/**
 * Starts the Google UMP consent flow and, when allowed, the Mobile Ads SDK. Called once after the
 * devotee has chosen a language, so the very first screen they see is the app, not a dialog.
 */
expect fun requestAdsConsent()

/** Re-opens the UMP privacy options form (required entry point in regulated regions). */
expect fun showPrivacyOptionsForm()

/**
 * Keeps full-screen ads respectful: at most one every [MIN_INTERVAL_MS], and never during
 * the first minutes of a session.
 */
object InterstitialPolicy {
    private const val MIN_INTERVAL_MS = 4 * 60 * 1000L
    private const val SESSION_GRACE_MS = 2 * 60 * 1000L
    private var sessionStart = 0L
    private var lastShown = 0L

    fun onSessionStart(now: Long) {
        if (sessionStart == 0L) sessionStart = now
    }

    fun shouldShow(now: Long): Boolean =
        now - sessionStart >= SESSION_GRACE_MS && now - lastShown >= MIN_INTERVAL_MS

    fun recordShown(now: Long) {
        lastShown = now
    }
}

/** Google's public test ad unit ids are used unless replaced in the platform `AdUnitIds`. */
expect object AdUnitIds {
    val banner: String
    val interstitial: String
}
