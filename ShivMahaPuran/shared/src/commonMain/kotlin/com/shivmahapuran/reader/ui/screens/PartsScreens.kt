package com.shivmahapuran.reader.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shivmahapuran.reader.ads.AdBanner
import com.shivmahapuran.reader.domain.model.AppLanguage
import com.shivmahapuran.reader.domain.model.ChapterProgress
import com.shivmahapuran.reader.presentation.PartDetailViewModel
import com.shivmahapuran.reader.presentation.PartsViewModel
import com.shivmahapuran.reader.resources.Res
import com.shivmahapuran.reader.resources.begin_part
import com.shivmahapuran.reader.resources.chapter_n
import com.shivmahapuran.reader.resources.chapters_read
import com.shivmahapuran.reader.resources.continue_part
import com.shivmahapuran.reader.resources.samhita_n
import com.shivmahapuran.reader.resources.verses_count
import com.shivmahapuran.reader.resources.with_meanings
import com.shivmahapuran.reader.ui.components.ChapterMedallion
import com.shivmahapuran.reader.ui.components.Gap
import com.shivmahapuran.reader.ui.components.GitaTopBar
import com.shivmahapuran.reader.ui.components.OrnamentDivider
import com.shivmahapuran.reader.ui.components.ParchmentBackground
import com.shivmahapuran.reader.ui.components.ParchmentCard
import com.shivmahapuran.reader.ui.components.ThinProgress
import com.shivmahapuran.reader.ui.theme.PuranTheme
import com.shivmahapuran.reader.util.localized
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** The Samhitas tab: all twelve parts of the Purana. */
@Composable
fun PartsScreen(contentPadding: PaddingValues, onOpenPart: (Int) -> Unit, viewModel: PartsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val language = state.settings.effectiveLanguage
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = contentPadding.calculateBottomPadding() + 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize().padding(top = contentPadding.calculateTopPadding()),
    ) {
        items(state.parts, key = { it.part.id }) { progress -> PartRow(progress, language) { onOpenPart(progress.part.id) } }
    }
}

/** One samhita / khanda with its adhyayas. */
@Composable
fun PartDetailScreen(
    partId: Int,
    onBack: () -> Unit,
    onRead: (chapterId: Int, verse: Int) -> Unit,
    viewModel: PartDetailViewModel = koinViewModel(key = "part-$partId") { parametersOf(partId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = PuranTheme.colors
    val language = state.settings.effectiveLanguage
    ParchmentBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { GitaTopBar(state.part?.name?.get(language).orEmpty(), onBack = onBack) },
            bottomBar = { Box(Modifier.background(colors.barSurface).navigationBarsPadding()) { AdBanner() } },
        ) { padding ->
            val part = state.part ?: return@Scaffold
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = padding.calculateBottomPadding() + 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding()),
            ) {
                item {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        ChapterMedallion(part.samhita, language, size = 72.dp)
                        Gap(8.dp)
                        Text(
                            stringResource(Res.string.samhita_n, part.samhita.localized(language)).uppercase(),
                            style = MaterialTheme.typography.labelMedium, color = colors.saffron,
                        )
                        Text(part.nameSanskrit, fontFamily = PuranTheme.fonts.sanskritDevanagari, fontSize = 26.sp, color = colors.maroon, textAlign = TextAlign.Center)
                        Text(part.name[language], style = MaterialTheme.typography.titleMedium, color = colors.inkSoft, textAlign = TextAlign.Center)
                        Gap(10.dp)
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 24.dp)) {
                            ThinProgress(state.chaptersRead.toFloat() / part.chapterCount, Modifier.weight(1f))
                            Gap(width = 10.dp)
                            Text(
                                stringResource(Res.string.chapters_read, state.chaptersRead.localized(language), part.chapterCount.localized(language)),
                                style = MaterialTheme.typography.labelSmall, color = colors.inkSoft,
                            )
                        }
                        state.nextChapter?.let { next ->
                            Gap(14.dp)
                            Button(
                                onClick = { onRead(next.id, 1) },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.maroon, contentColor = colors.onAccent),
                                shape = RoundedCornerShape(26.dp),
                            ) {
                                Text(
                                    if (state.chaptersRead == 0) stringResource(Res.string.begin_part)
                                    else stringResource(Res.string.continue_part, next.number.localized(language)),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                )
                            }
                        }
                        Gap(8.dp)
                        OrnamentDivider()
                    }
                }
                if (part.description[language].isNotBlank()) {
                    item {
                        ParchmentCard(Modifier.fillMaxWidth()) {
                            Text(part.description[language], style = MaterialTheme.typography.bodyMedium, color = colors.ink, modifier = Modifier.padding(16.dp))
                        }
                    }
                }
                items(state.chapters, key = { it.chapter.id }) { item -> ChapterRow(item, language) { onRead(item.chapter.id, 1) } }
            }
        }
    }
}

@Composable
private fun ChapterRow(item: ChapterProgress, language: AppLanguage, onClick: () -> Unit) {
    val colors = PuranTheme.colors
    val chapter = item.chapter
    ParchmentCard(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(Res.string.chapter_n, chapter.number.localized(language)).uppercase(),
                    style = MaterialTheme.typography.labelMedium, color = colors.saffron, modifier = Modifier.weight(1f),
                )
                if (chapter.hasMeanings) {
                    Text(stringResource(Res.string.with_meanings), style = MaterialTheme.typography.labelSmall, color = colors.gold)
                    Gap(width = 8.dp)
                }
                Text(stringResource(Res.string.verses_count, chapter.verseCount.localized(language)), style = MaterialTheme.typography.labelSmall, color = colors.inkSoft)
                if (item.read) {
                    Gap(width = 6.dp)
                    Icon(Icons.Filled.CheckCircle, null, tint = colors.saffron, modifier = Modifier.padding(0.dp))
                }
            }
            Text(chapter.displayTitle(language), style = MaterialTheme.typography.titleMedium, color = colors.maroon, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (chapter.titleSanskrit.isNotBlank() && chapter.title[language] != null) {
                Text(chapter.titleSanskrit, fontFamily = PuranTheme.fonts.sanskritDevanagari, fontSize = 14.sp, color = colors.inkSoft, maxLines = 1)
            }
            val preview = chapter.summary[language]
            Gap(4.dp)
            Text(
                preview ?: chapter.synopsisSanskrit,
                fontFamily = if (preview != null) PuranTheme.fonts.forLanguage(language) else PuranTheme.fonts.sanskritDevanagari,
                style = MaterialTheme.typography.bodyMedium, color = colors.ink, maxLines = 3, overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
