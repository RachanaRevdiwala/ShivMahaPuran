package com.shivmahapuran.reader.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shivmahapuran.reader.ads.showPrivacyOptionsForm
import com.shivmahapuran.reader.domain.model.AppLanguage
import com.shivmahapuran.reader.domain.model.AppSettings
import com.shivmahapuran.reader.domain.model.ShlokaScript
import com.shivmahapuran.reader.platform.appVersionName
import com.shivmahapuran.reader.presentation.SettingsViewModel
import com.shivmahapuran.reader.resources.Res
import com.shivmahapuran.reader.resources.about_text
import com.shivmahapuran.reader.resources.content_source
import com.shivmahapuran.reader.resources.font_size
import com.shivmahapuran.reader.resources.keep_screen_on
import com.shivmahapuran.reader.resources.night_mode
import com.shivmahapuran.reader.resources.night_mode_hint
import com.shivmahapuran.reader.resources.privacy_options
import com.shivmahapuran.reader.resources.sanskrit_script
import com.shivmahapuran.reader.resources.script_auto
import com.shivmahapuran.reader.resources.script_devanagari
import com.shivmahapuran.reader.resources.script_gujarati
import com.shivmahapuran.reader.resources.settings_about
import com.shivmahapuran.reader.resources.settings_display
import com.shivmahapuran.reader.resources.settings_language
import com.shivmahapuran.reader.resources.settings_reading
import com.shivmahapuran.reader.resources.show_meanings
import com.shivmahapuran.reader.resources.show_transliteration
import com.shivmahapuran.reader.resources.verses_per_page
import com.shivmahapuran.reader.resources.version
import com.shivmahapuran.reader.ui.components.Gap
import com.shivmahapuran.reader.ui.components.OmGlyph
import com.shivmahapuran.reader.ui.components.OrnamentDivider
import com.shivmahapuran.reader.ui.components.ParchmentCard
import com.shivmahapuran.reader.ui.components.SectionTitle
import com.shivmahapuran.reader.ui.theme.PuranTheme
import com.shivmahapuran.reader.util.localized
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(contentPadding: PaddingValues, viewModel: SettingsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settings = state.settings
    val colors = PuranTheme.colors
    Column(
        Modifier.fillMaxSize()
            .padding(top = contentPadding.calculateTopPadding())
            .verticalScroll(rememberScrollState())
            .padding(bottom = contentPadding.calculateBottomPadding())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SettingsSection(stringResource(Res.string.settings_language)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppLanguage.entries.forEach { lang -> ChoiceChip(lang.nativeName, lang == settings.language) { viewModel.setLanguage(lang) } }
            }
        }
        SettingsSection(stringResource(Res.string.settings_reading)) {
            ReadingPreferences(
                settings = settings,
                onFontScale = viewModel::setFontScale,
                onTransliteration = viewModel::setShowTransliteration,
                onMeanings = viewModel::setShowMeanings,
                onNightMode = viewModel::setNightMode,
                onScript = viewModel::setScript,
                onVersesPerPage = viewModel::setVersesPerPage,
            )
        }
        SettingsSection(stringResource(Res.string.settings_display)) {
            SwitchRow(stringResource(Res.string.keep_screen_on), null, settings.keepScreenOn, viewModel::setKeepScreenOn)
            if (state.privacyOptionsRequired) {
                Row(Modifier.fillMaxWidth().clickable { showPrivacyOptionsForm() }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(Res.string.privacy_options), style = MaterialTheme.typography.bodyLarge, color = colors.ink, modifier = Modifier.weight(1f))
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = colors.inkSoft)
                }
            }
        }
        SettingsSection(stringResource(Res.string.settings_about)) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                OmGlyph(44.dp)
                Text(stringResource(Res.string.about_text), style = MaterialTheme.typography.bodyMedium, color = colors.ink)
                Gap(8.dp)
                OrnamentDivider(Modifier.padding(horizontal = 32.dp))
                Gap(8.dp)
                Text(stringResource(Res.string.content_source), style = MaterialTheme.typography.bodySmall, color = colors.inkSoft)
                Gap(8.dp)
                Text(stringResource(Res.string.version, appVersionName), style = MaterialTheme.typography.labelMedium, color = colors.inkSoft)
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    ParchmentCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            SectionTitle(title)
            Gap(8.dp)
            content()
        }
    }
}

/** Reading preferences shared by the Settings tab and the reader's options sheet. */
@Composable
fun ReadingPreferences(
    settings: AppSettings,
    onFontScale: (Float) -> Unit,
    onTransliteration: (Boolean) -> Unit,
    onMeanings: (Boolean) -> Unit,
    onNightMode: (Boolean) -> Unit,
    onScript: (ShlokaScript) -> Unit,
    onVersesPerPage: (Int) -> Unit,
) {
    val colors = PuranTheme.colors
    val language = settings.effectiveLanguage
    var scale by remember(settings.fontScale) { mutableFloatStateOf(settings.fontScale) }
    Text(stringResource(Res.string.font_size), style = MaterialTheme.typography.bodyLarge, color = colors.ink)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("अ", fontSize = 14.sp, color = colors.inkSoft)
        Slider(
            value = scale, onValueChange = { scale = it }, onValueChangeFinished = { onFontScale(scale) },
            valueRange = AppSettings.MIN_FONT_SCALE..AppSettings.MAX_FONT_SCALE, steps = 9,
            colors = SliderDefaults.colors(thumbColor = colors.saffron, activeTrackColor = colors.saffron, inactiveTrackColor = colors.goldSoft),
            modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
        )
        Text("अ", fontSize = 24.sp, color = colors.inkSoft)
    }
    Text(
        "ॐ नमः शिवाय", fontFamily = PuranTheme.fonts.sanskritDevanagari, fontSize = (20 * scale).sp, color = colors.maroon,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
    )
    SwitchRow(stringResource(Res.string.show_meanings), null, settings.showMeanings, onMeanings)
    SwitchRow(stringResource(Res.string.show_transliteration), null, settings.showTransliteration, onTransliteration)
    SwitchRow(stringResource(Res.string.night_mode), stringResource(Res.string.night_mode_hint), settings.nightMode, onNightMode)
    Gap(8.dp)
    Text(stringResource(Res.string.verses_per_page), style = MaterialTheme.typography.bodyLarge, color = colors.ink)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppSettings.VERSES_PER_PAGE_OPTIONS.forEach { n -> ChoiceChip(n.localized(language), settings.versesPerPage == n) { onVersesPerPage(n) } }
    }
    Gap(8.dp)
    Text(stringResource(Res.string.sanskrit_script), style = MaterialTheme.typography.bodyLarge, color = colors.ink)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ChoiceChip(stringResource(Res.string.script_auto), settings.shlokaScript == ShlokaScript.AUTO) { onScript(ShlokaScript.AUTO) }
        ChoiceChip(stringResource(Res.string.script_devanagari), settings.shlokaScript == ShlokaScript.DEVANAGARI) { onScript(ShlokaScript.DEVANAGARI) }
        ChoiceChip(stringResource(Res.string.script_gujarati), settings.shlokaScript == ShlokaScript.GUJARATI) { onScript(ShlokaScript.GUJARATI) }
    }
}

@Composable
internal fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = PuranTheme.colors
    FilterChip(
        selected = selected, onClick = onClick, label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = colors.saffron.copy(alpha = 0.22f), selectedLabelColor = colors.maroon, labelColor = colors.inkSoft,
        ),
    )
}

@Composable
private fun SwitchRow(title: String, subtitle: String?, checked: Boolean, onChange: (Boolean) -> Unit) {
    val colors = PuranTheme.colors
    Row(Modifier.fillMaxWidth().clickable { onChange(!checked) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = colors.ink)
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = colors.inkSoft) }
        }
        Switch(checked = checked, onCheckedChange = onChange, colors = SwitchDefaults.colors(checkedTrackColor = colors.saffron, checkedThumbColor = colors.onAccent))
    }
}
