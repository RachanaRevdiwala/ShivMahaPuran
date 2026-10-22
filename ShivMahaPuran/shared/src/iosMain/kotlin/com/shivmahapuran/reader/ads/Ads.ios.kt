package com.shivmahapuran.reader.ads

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shivmahapuran.reader.platform.currentTimeMillis
import platform.UIKit.UIView

/**
 * Implemented in Swift (iosApp/iosApp/GoogleAdsBridge.swift) on top of the GoogleMobileAds SDK,
 * which is distributed via Swift Package Manager.
 */
interface IosAdsBridge {
    fun bannerHeight(width: Double): Double
    fun createBanner(adUnitId: String, width: Double): UIView
    fun loadInterstitial(adUnitId: String)
    fun isInterstitialReady(): Boolean
    /** Presents the loaded interstitial and calls [onDismiss] when it closes or fails. */
    fun showInterstitial(onDismiss: () -> Unit)
    fun showPrivacyOptions()
    /** Runs UMP consent (+ ATT) and starts the Mobile Ads SDK, then calls [IosAds.onConsentResolved]. */
    fun gatherConsentAndStart()
}

object IosAds {
    var bridge: IosAdsBridge? = null

    /** Called from Swift once UMP consent is resolved and the SDK has started. */
    fun onConsentResolved(canRequestAds: Boolean, privacyOptionsRequired: Boolean) {
        AdsState.onConsentResolved(canRequestAds, privacyOptionsRequired)
        InterstitialAds.preload()
    }
}

actual object AdUnitIds {
    // Google's public TEST ad units. Replace with your own AdMob units before publishing.
    actual val banner: String = "ca-app-pub-3940256099942544/2435281174"
    actual val interstitial: String = "ca-app-pub-3940256099942544/4411468910"
}

@Composable
actual fun AdBanner(modifier: Modifier) {
    val canRequestAds by AdsState.canRequestAds.collectAsStateWithLifecycle()
    val bridge = IosAds.bridge
    if (!canRequestAds || bridge == null) return
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val width = maxWidth.value.toDouble()
        val height = remember(width) { bridge.bannerHeight(width) }
        UIKitView(
            factory = { bridge.createBanner(AdUnitIds.banner, width) },
            modifier = Modifier.fillMaxWidth().height(height.dp),
        )
    }
}

actual object InterstitialAds {
    actual fun preload() {
        if (!AdsState.canRequestAds.value) return
        val bridge = IosAds.bridge ?: return
        if (!bridge.isInterstitialReady()) bridge.loadInterstitial(AdUnitIds.interstitial)
    }

    actual fun show(onFinished: () -> Unit) {
        val bridge = IosAds.bridge
        val now = currentTimeMillis()
        if (bridge == null || !bridge.isInterstitialReady() || !InterstitialPolicy.shouldShow(now)) {
            onFinished()
            preload()
            return
        }
        InterstitialPolicy.recordShown(now)
        bridge.showInterstitial {
            onFinished()
            preload()
        }
    }
}

private var consentRequested = false

actual fun requestAdsConsent() {
    if (consentRequested) return
    consentRequested = true
    IosAds.bridge?.gatherConsentAndStart()
}

actual fun showPrivacyOptionsForm() {
    IosAds.bridge?.showPrivacyOptions()
}
