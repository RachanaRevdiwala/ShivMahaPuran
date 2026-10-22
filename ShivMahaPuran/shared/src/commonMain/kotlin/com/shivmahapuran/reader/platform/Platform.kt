package com.shivmahapuran.reader.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue

expect fun currentTimeMillis(): Long

/** Days since 1970-01-01 in the device's local time zone (drives "Verse of the Day"). */
expect fun localEpochDay(): Long

expect val appVersionName: String

/** Opens the native share sheet with plain text. */
expect fun shareText(text: String)

/** Keeps the display awake while this composable is in composition and [enabled] is true. */
@Composable
expect fun KeepScreenOn(enabled: Boolean)

/**
 * Overrides the locale used by Compose resources so the devotee can switch language in-app,
 * independent of the system language.
 */
expect object LocalAppLocale {
    val current: String @Composable get

    @Composable
    infix fun provides(value: String?): ProvidedValue<*>
}
