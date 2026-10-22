import AppTrackingTransparency
import GoogleMobileAds
import Shared
import UIKit
import UserMessagingPlatform

/// Implements the Kotlin `IosAdsBridge` with the Google Mobile Ads SDK and the UMP consent SDK.
final class GoogleAdsBridge: NSObject, IosAdsBridge, FullScreenContentDelegate {

    private var interstitial: InterstitialAd?
    private var isLoadingInterstitial = false
    private var onInterstitialDismiss: (() -> Void)?
    private var started = false

    // MARK: - Consent + SDK start

    /// UMP consent first (GDPR / US states), then ATT, then the Mobile Ads SDK.
    func gatherConsentAndStart() {
        #if DEBUG
        // Lets UI tests / screenshots run without the consent dialog: SIMCTL_CHILD_PURAN_DISABLE_ADS=1
        if ProcessInfo.processInfo.environment["PURAN_DISABLE_ADS"] == "1" { return }
        #endif
        ConsentInformation.shared.requestConsentInfoUpdate(with: RequestParameters()) { [weak self] _ in
            guard let self else { return }
            DispatchQueue.main.async {
                guard let presenter = Self.topViewController() else { self.startIfAllowed(); return }
                ConsentForm.loadAndPresentIfRequired(from: presenter) { _ in
                    self.requestTrackingThenStart()
                }
            }
        }
        // Returning users with cached consent can start immediately.
        if ConsentInformation.shared.canRequestAds { startIfAllowed() }
    }

    private func requestTrackingThenStart() {
        if #available(iOS 14, *), ATTrackingManager.trackingAuthorizationStatus == .notDetermined {
            ATTrackingManager.requestTrackingAuthorization { _ in
                DispatchQueue.main.async { self.startIfAllowed() }
            }
        } else {
            startIfAllowed()
        }
    }

    private func startIfAllowed() {
        let info = ConsentInformation.shared
        let privacyRequired = info.privacyOptionsRequirementStatus == .required
        guard info.canRequestAds else {
            IosAds.shared.onConsentResolved(canRequestAds: false, privacyOptionsRequired: privacyRequired)
            return
        }
        if started {
            IosAds.shared.onConsentResolved(canRequestAds: true, privacyOptionsRequired: privacyRequired)
            return
        }
        started = true
        // A scripture app: only ads suitable for general audiences.
        MobileAds.shared.requestConfiguration.maxAdContentRating = .general
        MobileAds.shared.start { _ in
            DispatchQueue.main.async {
                IosAds.shared.onConsentResolved(canRequestAds: true, privacyOptionsRequired: privacyRequired)
            }
        }
    }

    // MARK: - IosAdsBridge

    func bannerHeight(width: Double) -> Double {
        Double(currentOrientationAnchoredAdaptiveBanner(width: CGFloat(width)).size.height)
    }

    func createBanner(adUnitId: String, width: Double) -> UIView {
        let banner = BannerView(adSize: currentOrientationAnchoredAdaptiveBanner(width: CGFloat(width)))
        banner.adUnitID = adUnitId
        banner.rootViewController = Self.topViewController()
        banner.load(Request())
        return banner
    }

    func loadInterstitial(adUnitId: String) {
        guard interstitial == nil, !isLoadingInterstitial else { return }
        isLoadingInterstitial = true
        InterstitialAd.load(with: adUnitId, request: Request()) { [weak self] ad, _ in
            guard let self else { return }
            self.isLoadingInterstitial = false
            self.interstitial = ad
            ad?.fullScreenContentDelegate = self
        }
    }

    func isInterstitialReady() -> Bool {
        interstitial != nil
    }

    func showInterstitial(onDismiss: @escaping () -> Void) {
        guard let ad = interstitial, let presenter = Self.topViewController() else {
            onDismiss()
            return
        }
        onInterstitialDismiss = onDismiss
        ad.present(from: presenter)
    }

    func showPrivacyOptions() {
        guard let presenter = Self.topViewController() else { return }
        ConsentForm.presentPrivacyOptionsForm(from: presenter) { _ in }
    }

    // MARK: - FullScreenContentDelegate

    func adDidDismissFullScreenContent(_ ad: FullScreenPresentingAd) {
        finishInterstitial()
    }

    func ad(_ ad: FullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: Error) {
        finishInterstitial()
    }

    private func finishInterstitial() {
        interstitial = nil
        let callback = onInterstitialDismiss
        onInterstitialDismiss = nil
        callback?()
    }

    // MARK: - Helpers

    static func topViewController() -> UIViewController? {
        let scene = UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .first { $0.activationState == .foregroundActive } ?? UIApplication.shared.connectedScenes.first as? UIWindowScene
        var top = scene?.windows.first { $0.isKeyWindow }?.rootViewController ?? scene?.windows.first?.rootViewController
        while let presented = top?.presentedViewController { top = presented }
        return top
    }
}
