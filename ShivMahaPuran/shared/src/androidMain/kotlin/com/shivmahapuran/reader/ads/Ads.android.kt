package com.shivmahapuran.reader.ads

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shivmahapuran.reader.platform.AndroidPlatform
import com.shivmahapuran.reader.platform.currentTimeMillis
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

actual object AdUnitIds {
    // Google's public TEST ad units. Replace with your own AdMob units before publishing.
    actual val banner: String = "ca-app-pub-3940256099942544/9214589741"
    actual val interstitial: String = "ca-app-pub-3940256099942544/1033173712"
}

@Composable
actual fun AdBanner(modifier: Modifier) {
    if (LocalInspectionMode.current) return
    val canRequestAds by AdsState.canRequestAds.collectAsStateWithLifecycle()
    if (!canRequestAds) return
    val context = LocalContext.current
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val width = maxWidth.value.toInt()
        val adSize = remember(width) { AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, width) }
        AndroidView(
            modifier = Modifier.fillMaxWidth().height(adSize.height.dp),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(adSize)
                    adUnitId = AdUnitIds.banner
                    loadAd(AdRequest.Builder().build())
                }
            },
            onRelease = { it.destroy() },
        )
    }
}

actual object InterstitialAds {
    private const val TAG = "InterstitialAds"
    private var ad: InterstitialAd? = null
    private var loading = false

    actual fun preload() {
        if (!AdsState.canRequestAds.value || ad != null || loading) return
        loading = true
        InterstitialAd.load(
            AndroidPlatform.application,
            AdUnitIds.interstitial,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(loaded: InterstitialAd) {
                    ad = loaded
                    loading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.d(TAG, "Interstitial failed to load: ${error.message}")
                    loading = false
                }
            },
        )
    }

    actual fun show(onFinished: () -> Unit) {
        val activity = AndroidPlatform.currentActivity
        val ready = ad
        val now = currentTimeMillis()
        if (activity == null || ready == null || !InterstitialPolicy.shouldShow(now)) {
            onFinished()
            preload()
            return
        }
        ready.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                ad = null
                onFinished()
                preload()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                ad = null
                onFinished()
                preload()
            }
        }
        InterstitialPolicy.recordShown(now)
        ready.show(activity)
    }
}

actual fun showPrivacyOptionsForm() {
    val activity = AndroidPlatform.currentActivity ?: return
    UserMessagingPlatform.showPrivacyOptionsForm(activity) { error ->
        if (error != null) Log.w("Ads", "Privacy options form error: ${error.message}")
    }
}

private val consentRequested = AtomicBoolean(false)
private val adsInitialized = AtomicBoolean(false)

actual fun requestAdsConsent() {
    val activity = AndroidPlatform.currentActivity ?: return
    if (!consentRequested.compareAndSet(false, true)) return
    val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
    consentInformation.requestConsentInfoUpdate(
        activity,
        ConsentRequestParameters.Builder().build(),
        {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { _ ->
                if (consentInformation.canRequestAds()) initializeAds(activity)
                AndroidAdsBridge.onConsentResolved(activity)
            }
        },
        { _ ->
            // Consent info could not be refreshed (e.g. offline); fall back to the cached status.
            if (consentInformation.canRequestAds()) initializeAds(activity)
        },
    )
    // Returning users with cached consent can start immediately.
    if (consentInformation.canRequestAds()) initializeAds(activity)
}

private fun initializeAds(activity: Activity) {
    if (!adsInitialized.compareAndSet(false, true)) return
    // A scripture app: restrict ads to content suitable for general audiences.
    MobileAds.setRequestConfiguration(
        RequestConfiguration.Builder()
            .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
            .build()
    )
    thread {
        MobileAds.initialize(activity.applicationContext) {
            activity.runOnUiThread { AndroidAdsBridge.onConsentResolved(activity) }
        }
    }
}

private object AndroidAdsBridge {
    fun onConsentResolved(activity: Activity) {
        val info = UserMessagingPlatform.getConsentInformation(activity)
        AdsState.onConsentResolved(
            canRequestAds = info.canRequestAds(),
            privacyOptionsRequired = info.privacyOptionsRequirementStatus ==
                com.google.android.ump.ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED,
        )
        InterstitialAds.preload()
    }
}
