package com.shivmahapuran.reader.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import platform.Foundation.NSBundle
import platform.Foundation.NSDate
import platform.Foundation.NSLocale
import platform.Foundation.NSTimeZone
import platform.Foundation.NSUserDefaults
import platform.Foundation.localTimeZone
import platform.Foundation.preferredLanguages
import platform.Foundation.secondsFromGMT
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.popoverPresentationController

actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

actual fun localEpochDay(): Long {
    val seconds = NSDate().timeIntervalSince1970.toLong() + NSTimeZone.localTimeZone.secondsFromGMT
    return seconds / 86_400L
}

actual val appVersionName: String
    get() = NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: "1.0"

actual fun shareText(text: String) {
    val presenter = topViewController() ?: return
    val controller = UIActivityViewController(activityItems = listOf(text), applicationActivities = null)
    // iPad requires an anchor for the popover.
    controller.popoverPresentationController?.sourceView = presenter.view
    presenter.presentViewController(controller, animated = true, completion = null)
}

@Composable
actual fun KeepScreenOn(enabled: Boolean) {
    DisposableEffect(enabled) {
        UIApplication.sharedApplication.idleTimerDisabled = enabled
        onDispose { UIApplication.sharedApplication.idleTimerDisabled = false }
    }
}

@Suppress("DEPRECATION")
internal fun topViewController(): UIViewController? {
    var controller = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (controller?.presentedViewController != null) controller = controller.presentedViewController
    return controller
}

actual object LocalAppLocale {
    private const val LANG_KEY = "AppleLanguages"
    private val default = NSLocale.preferredLanguages.first() as String
    private val LocalAppLocale = staticCompositionLocalOf { default }

    actual val current: String
        @Composable get() = LocalAppLocale.current

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val new = value ?: default
        if (value == null) {
            NSUserDefaults.standardUserDefaults.removeObjectForKey(LANG_KEY)
        } else {
            NSUserDefaults.standardUserDefaults.setObject(listOf(new), LANG_KEY)
        }
        return LocalAppLocale.provides(new)
    }
}
