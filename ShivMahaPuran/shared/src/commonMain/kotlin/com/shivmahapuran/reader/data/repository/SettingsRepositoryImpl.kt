package com.shivmahapuran.reader.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.shivmahapuran.reader.db.Preference
import com.shivmahapuran.reader.db.PuranDatabase
import com.shivmahapuran.reader.domain.model.AppLanguage
import com.shivmahapuran.reader.domain.model.AppSettings
import com.shivmahapuran.reader.domain.model.ShlokaScript
import com.shivmahapuran.reader.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** Stores preferences as key/value rows in SQLite so all app state lives in one local database. */
internal class SettingsRepositoryImpl(private val database: PuranDatabase) : SettingsRepository {

    private val queries get() = database.puranQueries

    override val settings: Flow<AppSettings> =
        queries.selectAllPreferences().asFlow().mapToList(Dispatchers.IO).map(::toSettings).distinctUntilChanged()

    override suspend fun current(): AppSettings =
        withContext(Dispatchers.IO) { toSettings(queries.selectAllPreferences().executeAsList()) }

    override suspend fun setLanguage(language: AppLanguage) = put(KEY_LANGUAGE, language.code)
    override suspend fun setFontScale(scale: Float) =
        put(KEY_FONT_SCALE, scale.coerceIn(AppSettings.MIN_FONT_SCALE, AppSettings.MAX_FONT_SCALE).toString())
    override suspend fun setShowTransliteration(show: Boolean) = put(KEY_TRANSLITERATION, show.toString())
    override suspend fun setShowMeanings(show: Boolean) = put(KEY_MEANINGS, show.toString())
    override suspend fun setShlokaScript(script: ShlokaScript) = put(KEY_SCRIPT, script.name)
    override suspend fun setVersesPerPage(count: Int) = put(KEY_VERSES_PER_PAGE, count.toString())
    override suspend fun setKeepScreenOn(enabled: Boolean) = put(KEY_KEEP_SCREEN_ON, enabled.toString())
    override suspend fun setNightMode(enabled: Boolean) = put(KEY_NIGHT_MODE, enabled.toString())

    private suspend fun put(key: String, value: String) {
        withContext(Dispatchers.IO) { queries.upsertPreference(key, value) }
    }

    private fun toSettings(rows: List<Preference>): AppSettings {
        val map = rows.associate { it.key to it.pref_value }
        val d = AppSettings()
        return AppSettings(
            language = AppLanguage.fromCode(map[KEY_LANGUAGE]),
            fontScale = map[KEY_FONT_SCALE]?.toFloatOrNull() ?: d.fontScale,
            showTransliteration = map[KEY_TRANSLITERATION]?.toBooleanStrictOrNull() ?: d.showTransliteration,
            showMeanings = map[KEY_MEANINGS]?.toBooleanStrictOrNull() ?: d.showMeanings,
            shlokaScript = map[KEY_SCRIPT]?.let { s -> ShlokaScript.entries.firstOrNull { it.name == s } } ?: d.shlokaScript,
            versesPerPage = map[KEY_VERSES_PER_PAGE]?.toIntOrNull()?.takeIf { it in AppSettings.VERSES_PER_PAGE_OPTIONS } ?: d.versesPerPage,
            keepScreenOn = map[KEY_KEEP_SCREEN_ON]?.toBooleanStrictOrNull() ?: d.keepScreenOn,
            nightMode = map[KEY_NIGHT_MODE]?.toBooleanStrictOrNull() ?: d.nightMode,
        )
    }

    private companion object {
        const val KEY_LANGUAGE = "language"
        const val KEY_FONT_SCALE = "font_scale"
        const val KEY_TRANSLITERATION = "show_transliteration"
        const val KEY_MEANINGS = "show_meanings"
        const val KEY_SCRIPT = "shloka_script"
        const val KEY_VERSES_PER_PAGE = "verses_per_page"
        const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
        const val KEY_NIGHT_MODE = "night_mode"
    }
}
