package com.shivmahapuran.reader.platform

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import java.util.Locale
import java.util.TimeZone

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual fun localEpochDay(): Long {
    val now = System.currentTimeMillis()
    return (now + TimeZone.getDefault().getOffset(now)) / 86_400_000L
}

actual val appVersionName: String
    get() = runCatching {
        val app = AndroidPlatform.application
        val info = if (Build.VERSION.SDK_INT >= 33) {
            app.packageManager.getPackageInfo(app.packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            app.packageManager.getPackageInfo(app.packageName, 0)
        }
        info.versionName
    }.getOrNull() ?: "1.0"

actual fun shareText(text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    val chooser = Intent.createChooser(send, null)
    val activity = AndroidPlatform.currentActivity
    if (activity != null) {
        activity.startActivity(chooser)
    } else {
        AndroidPlatform.application.startActivity(chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

@Composable
actual fun KeepScreenOn(enabled: Boolean) {
    val view = LocalView.current
    DisposableEffect(view, enabled) {
        view.keepScreenOn = enabled
        onDispose { view.keepScreenOn = false }
    }
}

actual object LocalAppLocale {
    private var default: Locale? = null

    actual val current: String
        @Composable get() = Locale.getDefault().toString()

    @Suppress("DEPRECATION")
    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val configuration = LocalConfiguration.current
        if (default == null) default = Locale.getDefault()
        val new = if (value == null) default!! else Locale.forLanguageTag(value)
        Locale.setDefault(new)
        configuration.setLocale(new)
        val resources = LocalContext.current.resources
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return LocalConfiguration.provides(configuration)
    }
}
