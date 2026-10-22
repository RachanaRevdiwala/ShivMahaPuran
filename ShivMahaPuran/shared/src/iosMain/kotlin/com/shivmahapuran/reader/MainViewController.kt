package com.shivmahapuran.reader

import androidx.compose.ui.window.ComposeUIViewController
import com.shivmahapuran.reader.ads.IosAds
import com.shivmahapuran.reader.ads.IosAdsBridge
import com.shivmahapuran.reader.data.local.DatabaseDriverFactory
import com.shivmahapuran.reader.di.initKoin
import org.koin.dsl.module
import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages
import platform.UIKit.UIViewController

private var started = false

/** Called once from Swift at launch with the AdMob bridge implementation. */
fun startApp(adsBridge: IosAdsBridge?) {
    if (started) return
    started = true
    LocalAppLocaleSnapshot.systemCode = (NSLocale.preferredLanguages.firstOrNull() as? String)?.take(2)
    IosAds.bridge = adsBridge
    initKoin(module { single { DatabaseDriverFactory() } })
}

fun MainViewController(): UIViewController = ComposeUIViewController { App() }

/** Forwards a URL opened via the `shivmahapuran://` scheme. Returns true when it was understood. */
fun handleDeepLink(url: String): Boolean = com.shivmahapuran.reader.navigation.DeepLinkBus.handle(url)
