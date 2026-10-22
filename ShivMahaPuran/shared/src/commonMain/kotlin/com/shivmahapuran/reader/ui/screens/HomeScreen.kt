package com.shivmahapuran.reader.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shivmahapuran.reader.domain.model.AppLanguage
import com.shivmahapuran.reader.domain.model.Chapter
import com.shivmahapuran.reader.domain.model.Part
import com.shivmahapuran.reader.domain.model.PartProgress
import com.shivmahapuran.reader.presentation.HomeUiState
import com.shivmahapuran.reader.presentation.HomeViewModel
import com.shivmahapuran.reader.resources.Res
import com.shivmahapuran.reader.resources.app_name
import com.shivmahapuran.reader.resources.chapter_n
import com.shivmahapuran.reader.resources.chapter_of_the_day
import com.shivmahapuran.reader.resources.chapter_verse
import com.shivmahapuran.reader.resources.chapters_count
import com.shivmahapuran.reader.resources.chapters_read
import com.shivmahapuran.reader.resources.continue_reading
import com.shivmahapuran.reader.resources.greeting
import com.shivmahapuran.reader.resources.invocation
import com.shivmahapuran.reader.resources.overall_progress
import com.shivmahapuran.reader.resources.read_now
import com.shivmahapuran.reader.resources.recite_daily
import com.shivmahapuran.reader.resources.samhita_n
import com.shivmahapuran.reader.resources.samhitas_title
import com.shivmahapuran.reader.resources.search
import com.shivmahapuran.reader.resources.start_reading
import com.shivmahapuran.reader.resources.start_reading_hint
import com.shivmahapuran.reader.resources.tagline
import com.shivmahapuran.reader.ui.components.ChapterMedallion
import com.shivmahapuran.reader.ui.components.Gap
import com.shivmahapuran.reader.ui.components.OmGlyph
import com.shivmahapuran.reader.ui.components.OrnamentDivider
import com.shivmahapuran.reader.ui.components.ParchmentCard
import com.shivmahapuran.reader.ui.components.SectionTitle
import com.shivmahapuran.reader.ui.components.ThinProgress
import com.shivmahapuran.reader.ui.theme.PuranTheme
import com.shivmahapuran.reader.util.localized
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    onOpenPart: (Int) -> Unit,
    onRead: (chapterId: Int, verse: Int) -> Unit,
    onSearch: () -> Unit,
    onOpenBook: (String) -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val language = state.settings.effectiveLanguage
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = contentPadding.calculateBottomPadding() + 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        // Top inset as real padding so content is clipped below the status bar.
        modifier = Modifier.fillMaxSize().padding(top = contentPadding.calculateTopPadding()),
    ) {
        item { HomeHeader(onSearch) }
        val today = state.chapterOfTheDay
        val todayPart = state.chapterOfTheDayPart
        if (today != null && todayPart != null) {
            item { ChapterOfTheDayCard(today, todayPart, language) { onRead(today.id, 1) } }
        }
        item { ContinueCard(state, language, onRead) }
        state.featuredStotra?.let { book ->
            item {
                ParchmentCard(Modifier.fillMaxWidth().clickable { onOpenBook(book.id) }) {
                    Row(Modifier.padding(horizontal = 18.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        OmGlyph(34.dp)
                        Gap(width = 12.dp)
                        Column(Modifier.weight(1f)) {
                            Text(book.title[language], style = MaterialTheme.typography.titleMedium, color = PuranTheme.colors.maroon)
                            Text(stringResource(Res.string.recite_daily), style = MaterialTheme.typography.bodyMedium, color = PuranTheme.colors.inkSoft)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = PuranTheme.colors.maroon)
                    }
                }
            }
        }
        item { SectionTitle(stringResource(Res.string.samhitas_title), Modifier.padding(top = 6.dp, start = 4.dp)) }
        items(state.parts, key = { it.part.id }) { progress ->
            PartRow(progress, language) { onOpenPart(progress.part.id) }
        }
    }
}

@Composable
private fun HomeHeader(onSearch: () -> Unit) {
    val colors = PuranTheme.colors
    Box(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(Res.string.invocation), color = colors.maroon, fontFamily = PuranTheme.fonts.sanskritDevanagari, fontSize = 18.sp)
            OmGlyph(64.dp)
            Text(stringResource(Res.string.app_name), style = MaterialTheme.typography.headlineMedium, color = colors.maroon, textAlign = TextAlign.Center)
            Text(stringResource(Res.string.tagline), style = MaterialTheme.typography.bodyMedium, color = colors.inkSoft, textAlign = TextAlign.Center)
            Gap(4.dp)
            Text(stringResource(Res.string.greeting), style = MaterialTheme.typography.titleSmall, color = colors.saffron)
            Gap(6.dp)
            OrnamentDivider(Modifier.padding(horizontal = 24.dp))
        }
        IconButton(onClick = onSearch, modifier = Modifier.align(Alignment.TopEnd)) {
            Icon(Icons.Filled.Search, stringResource(Res.string.search), tint = colors.maroon)
        }
    }
}

@Composable
private fun ChapterOfTheDayCard(chapter: Chapter, part: Part, language: AppLanguage, onClick: () -> Unit) {
    val colors = PuranTheme.colors
    ParchmentCard(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(18.dp)) {
            Text(stringResource(Res.string.chapter_of_the_day).uppercase(), style = MaterialTheme.typography.labelLarge, color = colors.saffron)
            Gap(4.dp)
            Text(
                part.name[language] + " · " + stringResource(Res.string.chapter_n, chapter.number.localized(language)),
                style = MaterialTheme.typography.labelMedium, color = colors.inkSoft,
            )
            Gap(6.dp)
            Text(chapter.displayTitle(language), style = MaterialTheme.typography.titleLarge, color = colors.maroon)
            if (chapter.titleSanskrit.isNotBlank()) {
                Text(chapter.titleSanskrit, fontFamily = PuranTheme.fonts.sanskritDevanagari, fontSize = 15.sp, color = colors.inkSoft)
            }
            val preview = chapter.summary[language]
                ?: chapter.synopsisSanskrit.takeUnless { chapter.synopsisIsTitle(language) }.orEmpty()
            if (preview.isNotBlank()) Gap(8.dp)
            if (preview.isNotBlank()) Text(
                preview,
                fontFamily = if (chapter.summary[language] != null) PuranTheme.fonts.forLanguage(language) else PuranTheme.fonts.sanskritDevanagari,
                style = MaterialTheme.typography.bodyMedium, color = colors.ink,
                maxLines = 5, overflow = TextOverflow.Ellipsis,
            )
            Gap(10.dp)
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = colors.maroon, contentColor = colors.onAccent),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.align(Alignment.End),
            ) { Text(stringResource(Res.string.read_now)) }
        }
    }
}

@Composable
private fun ContinueCard(state: HomeUiState, language: AppLanguage, onRead: (Int, Int) -> Unit) {
    val colors = PuranTheme.colors
    val last = state.lastPosition
    ParchmentCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = colors.saffron)
                Gap(width = 10.dp)
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(if (last == null) Res.string.start_reading else Res.string.continue_reading),
                        style = MaterialTheme.typography.titleMedium, color = colors.maroon,
                    )
                    Text(
                        if (last != null) {
                            last.part.name[language] + " · " +
                                stringResource(Res.string.chapter_verse, last.chapter.number.localized(language), last.verseNumber.localized(language))
                        } else stringResource(Res.string.start_reading_hint),
                        style = MaterialTheme.typography.bodyMedium, color = colors.inkSoft,
                        maxLines = 2, overflow = TextOverflow.Ellipsis,
                    )
                }
                Button(
                    onClick = { if (last != null) onRead(last.chapter.id, last.verseNumber) else onRead(1, 1) },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.maroon, contentColor = colors.onAccent),
                    shape = RoundedCornerShape(20.dp),
                ) { Text(stringResource(Res.string.read_now)) }
            }
            if (state.totalChapters > 0) {
                Gap(14.dp)
                Row {
                    Text(stringResource(Res.string.overall_progress), style = MaterialTheme.typography.labelMedium, color = colors.inkSoft, modifier = Modifier.weight(1f))
                    Text(
                        stringResource(Res.string.chapters_read, state.chaptersRead.localized(language), state.totalChapters.localized(language)),
                        style = MaterialTheme.typography.labelMedium, color = colors.inkSoft,
                    )
                }
                Gap(6.dp)
                ThinProgress(state.chaptersRead.toFloat() / state.totalChapters, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun PartRow(progress: PartProgress, language: AppLanguage, onClick: () -> Unit) {
    val colors = PuranTheme.colors
    val part = progress.part
    ParchmentCard(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            ChapterMedallion(part.samhita, language)
            Gap(width = 14.dp)
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(Res.string.samhita_n, part.samhita.localized(language)).uppercase(),
                    style = MaterialTheme.typography.labelMedium, color = colors.saffron,
                )
                Text(part.name[language], style = MaterialTheme.typography.titleMedium, color = colors.maroon, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(part.nameSanskrit, fontFamily = PuranTheme.fonts.sanskritDevanagari, fontSize = 14.sp, color = colors.inkSoft, maxLines = 1)
                Gap(6.dp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ThinProgress(progress.fraction, Modifier.weight(1f))
                    Gap(width = 10.dp)
                    Text(
                        stringResource(Res.string.chapters_count, part.chapterCount.localized(language)),
                        style = MaterialTheme.typography.labelSmall, color = colors.inkSoft,
                    )
                }
            }
        }
    }
}
