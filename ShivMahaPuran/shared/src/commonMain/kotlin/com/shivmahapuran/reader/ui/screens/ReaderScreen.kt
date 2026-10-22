package com.shivmahapuran.reader.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shivmahapuran.reader.ads.AdBanner
import com.shivmahapuran.reader.ads.InterstitialAds
import com.shivmahapuran.reader.domain.model.AppSettings
import com.shivmahapuran.reader.domain.model.Chapter
import com.shivmahapuran.reader.domain.model.Part
import com.shivmahapuran.reader.domain.model.ShlokaScript
import com.shivmahapuran.reader.domain.model.Verse
import com.shivmahapuran.reader.domain.model.resolve
import com.shivmahapuran.reader.platform.KeepScreenOn
import com.shivmahapuran.reader.presentation.ReaderPage
import com.shivmahapuran.reader.presentation.ReaderUiState
import com.shivmahapuran.reader.presentation.ReaderViewModel
import com.shivmahapuran.reader.resources.Res
import com.shivmahapuran.reader.resources.back_to_part
import com.shivmahapuran.reader.resources.bookmark_add
import com.shivmahapuran.reader.resources.bookmark_remove
import com.shivmahapuran.reader.resources.chapter_complete
import com.shivmahapuran.reader.resources.chapter_complete_message
import com.shivmahapuran.reader.resources.chapter_n
import com.shivmahapuran.reader.resources.copied
import com.shivmahapuran.reader.resources.copy
import com.shivmahapuran.reader.resources.meaning
import com.shivmahapuran.reader.resources.next
import com.shivmahapuran.reader.resources.next_chapter
import com.shivmahapuran.reader.resources.page_of
import com.shivmahapuran.reader.resources.previous
import com.shivmahapuran.reader.resources.puran_complete
import com.shivmahapuran.reader.resources.puran_complete_message
import com.shivmahapuran.reader.resources.reading_options
import com.shivmahapuran.reader.resources.sanskrit_synopsis
import com.shivmahapuran.reader.resources.share
import com.shivmahapuran.reader.resources.summary
import com.shivmahapuran.reader.resources.summary_pending
import com.shivmahapuran.reader.resources.verses_count
import com.shivmahapuran.reader.ui.components.Gap
import com.shivmahapuran.reader.ui.components.GitaTopBar
import com.shivmahapuran.reader.ui.components.OmGlyph
import com.shivmahapuran.reader.ui.components.OrnamentDivider
import com.shivmahapuran.reader.ui.components.ParchmentBackground
import com.shivmahapuran.reader.ui.components.bookLeaf
import com.shivmahapuran.reader.ui.theme.PuranTheme
import com.shivmahapuran.reader.util.Transliteration
import com.shivmahapuran.reader.util.localized
import com.shivmahapuran.reader.util.rememberVerseCopier
import com.shivmahapuran.reader.util.rememberVerseSharer
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.abs

@Composable
fun ReaderScreen(
    chapterId: Int,
    startVerse: Int,
    onBack: () -> Unit,
    onOpenChapter: (Int) -> Unit,
    onBackToPart: (Int) -> Unit,
    viewModel: ReaderViewModel = koinViewModel(key = "reader-$chapterId") { parametersOf(chapterId, startVerse) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    KeepScreenOn(state.settings.keepScreenOn)
    LaunchedEffect(Unit) { InterstitialAds.preload() }

    val chapter = state.chapter
    val part = state.part
    if (state.loading || chapter == null || part == null) {
        ParchmentBackground { CircularProgressIndicator(Modifier.align(Alignment.Center), color = PuranTheme.colors.saffron) }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = viewModel.initialPage(state.settings.versesPerPage).coerceIn(0, state.pages.size - 1),
    ) { state.pages.size }
    LaunchedEffect(pagerState, state.pages) {
        snapshotFlow { pagerState.settledPage }.collect { page -> state.pages.getOrNull(page)?.let(viewModel::onPageSettled) }
    }

    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val language = state.settings.effectiveLanguage
    val colors = PuranTheme.colors
    val currentPage = state.pages.getOrNull(pagerState.currentPage)
    val firstVerseOnPage = (currentPage as? ReaderPage.Shlokas)?.verses?.first()

    ParchmentBackground {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = {
                GitaTopBar(
                    title = part.name[language] + " · " + chapter.number.localized(language),
                    onBack = onBack,
                    actions = {
                        if (firstVerseOnPage != null) {
                            val bookmarked = (chapter.id to firstVerseOnPage.number) in state.bookmarkKeys
                            IconButton(onClick = { viewModel.toggleBookmark(firstVerseOnPage.number) }) {
                                Icon(
                                    if (bookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                    stringResource(if (bookmarked) Res.string.bookmark_remove else Res.string.bookmark_add),
                                    tint = if (bookmarked) colors.saffron else colors.maroon,
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.setShowOptions(true) }) {
                            Icon(Icons.Filled.TextFields, stringResource(Res.string.reading_options), tint = colors.maroon)
                        }
                    },
                )
            },
            bottomBar = {
                Column(Modifier.navigationBarsPadding()) {
                    PageControls(pagerState, state.pages.size, language)
                    AdBanner()
                }
            },
        ) { padding ->
            BookPager(pagerState = pagerState, modifier = Modifier.padding(padding).fillMaxSize()) { index ->
                when (val page = state.pages[index]) {
                    ReaderPage.Opening -> OpeningPage(chapter, part, state.settings)
                    is ReaderPage.Shlokas -> ShlokaPage(
                        verses = page.verses, chapter = chapter, part = part, settings = state.settings,
                        onCopied = { scope.launch { snackbar.showSnackbar(getString(Res.string.copied)) } },
                    )
                    ReaderPage.Closing -> ClosingPage(
                        chapter = chapter, state = state,
                        onNext = { next -> InterstitialAds.show { onOpenChapter(next) } },
                        onBackToPart = { InterstitialAds.show { onBackToPart(part.id) } },
                    )
                }
            }
        }
    }

    if (state.showOptions) {
        ModalBottomSheet(onDismissRequest = { viewModel.setShowOptions(false) }, containerColor = MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 28.dp)) {
                Text(stringResource(Res.string.reading_options), style = MaterialTheme.typography.titleLarge, color = colors.maroon)
                Gap(8.dp)
                ReadingPreferences(
                    settings = state.settings,
                    onFontScale = viewModel::setFontScale,
                    onTransliteration = viewModel::setShowTransliteration,
                    onMeanings = viewModel::setShowMeanings,
                    onNightMode = viewModel::setNightMode,
                    onScript = viewModel::setScript,
                    onVersesPerPage = viewModel::setVersesPerPage,
                )
            }
        }
    }
}

/**
 * Horizontal pager where the outgoing page folds away on its left edge (the spine) while the next
 * page waits underneath. The pager position is read only in the placement and draw phases (never
 * captured during composition) so pages stay aligned during a swipe; the area is clipped so a
 * turning page never draws over the toolbar or controls; the turning leaf stays opaque and the page
 * underneath fades in only once uncovered, so text from two pages never blends.
 */
@Composable
internal fun BookPager(pagerState: PagerState, modifier: Modifier = Modifier, pageContent: @Composable (Int) -> Unit) {
    HorizontalPager(state = pagerState, modifier = modifier.clipToBounds(), beyondViewportPageCount = 1) { page ->
        fun offset() = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
        Box(
            Modifier
                .fillMaxSize()
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    layout(placeable.width, placeable.height) { placeable.place(0, 0, zIndex = if (offset() > 0f) 1f else 0f) }
                }
                .graphicsLayer {
                    val o = offset()
                    if (abs(o) >= 1f) {
                        alpha = 0f
                        return@graphicsLayer
                    }
                    translationX = o * size.width
                    transformOrigin = TransformOrigin(0f, 0.5f)
                    cameraDistance = 24f * density
                    rotationY = if (o > 0f) 90f * o else 0f
                    alpha = if (o < 0f) ((1f + o - 0.4f) / 0.6f).coerceIn(0f, 1f) else 1f
                },
        ) { pageContent(page) }
    }
}

@Composable
private fun Leaf(content: @Composable () -> Unit) {
    val colors = PuranTheme.colors
    Box(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 6.dp), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = 640.dp).fillMaxSize().bookLeaf().padding(horizontal = 22.dp, vertical = 18.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) { content() }
    }
}

@Composable
private fun OpeningPage(chapter: Chapter, part: Part, settings: AppSettings) {
    val colors = PuranTheme.colors
    val fonts = PuranTheme.fonts
    val language = settings.effectiveLanguage
    val scale = settings.fontScale
    Leaf {
        Text(part.nameSanskrit, fontFamily = fonts.sanskritDevanagari, fontSize = 15.sp, color = colors.inkSoft, textAlign = TextAlign.Center)
        Gap(10.dp)
        OmGlyph(44.dp)
        Text(stringResource(Res.string.chapter_n, chapter.number.localized(language)).uppercase(), style = MaterialTheme.typography.labelLarge, color = colors.saffron)
        Gap(6.dp)
        Text(chapter.displayTitle(language), style = MaterialTheme.typography.headlineSmall, color = colors.maroon, textAlign = TextAlign.Center)
        if (chapter.titleSanskrit.isNotBlank()) {
            Gap(4.dp)
            Text(chapter.titleSanskrit, fontFamily = fonts.sanskritDevanagari, fontSize = 18.sp, color = colors.inkSoft, textAlign = TextAlign.Center)
        }
        Text(stringResource(Res.string.verses_count, chapter.verseCount.localized(language)), style = MaterialTheme.typography.labelMedium, color = colors.gold)
        Gap(12.dp)
        OrnamentDivider()
        Gap(12.dp)
        val summary = chapter.summary[language]
        if (summary != null) {
            Text(stringResource(Res.string.summary).uppercase(), style = MaterialTheme.typography.labelSmall, color = colors.gold)
            Gap(6.dp)
            Text(
                summary, fontFamily = fonts.forLanguage(language), fontSize = (17 * scale).sp, lineHeight = (28 * scale).sp,
                color = colors.ink, modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Text(stringResource(Res.string.summary_pending), style = MaterialTheme.typography.bodySmall, color = colors.inkSoft, textAlign = TextAlign.Center)
        }
        if (chapter.synopsisSanskrit.isNotBlank() && !chapter.synopsisIsTitle(language)) {
            Gap(16.dp)
            Text(stringResource(Res.string.sanskrit_synopsis).uppercase(), style = MaterialTheme.typography.labelSmall, color = colors.gold)
            Gap(6.dp)
            Text(
                chapter.synopsisSanskrit, fontFamily = fonts.sanskritDevanagari, fontSize = (16 * scale).sp, lineHeight = (27 * scale).sp,
                color = colors.inkSoft, textAlign = TextAlign.Center,
            )
        }
        Gap(12.dp)
    }
}

@Composable
private fun ShlokaPage(verses: List<Verse>, chapter: Chapter, part: Part, settings: AppSettings, onCopied: () -> Unit) {
    val colors = PuranTheme.colors
    val fonts = PuranTheme.fonts
    val language = settings.effectiveLanguage
    val scale = settings.fontScale
    val gujarati = settings.shlokaScript.resolve(language) == ShlokaScript.GUJARATI
    val share = rememberVerseSharer()
    val copy = rememberVerseCopier()
    Leaf {
        Text(
            part.name[language] + "  ·  " + stringResource(Res.string.chapter_n, chapter.number.localized(language)),
            style = MaterialTheme.typography.labelMedium, color = colors.inkSoft, textAlign = TextAlign.Center,
        )
        Gap(6.dp)
        verses.forEachIndexed { i, verse ->
            if (i > 0) {
                Gap(10.dp)
                OrnamentDivider(Modifier.padding(horizontal = 56.dp))
                Gap(10.dp)
            } else Gap(6.dp)
            verse.speaker?.let {
                Text(
                    if (gujarati) Transliteration.toGujarati(it) else it,
                    fontFamily = if (gujarati) fonts.sanskritGujarati else fonts.sanskritDevanagari,
                    fontSize = (16 * scale).sp, color = colors.saffron, textAlign = TextAlign.Center,
                )
                Gap(4.dp)
            }
            Text(
                if (gujarati) Transliteration.toGujarati(verse.sanskrit) else verse.sanskrit,
                fontFamily = if (gujarati) fonts.sanskritGujarati else fonts.sanskritDevanagari,
                fontSize = (19 * scale).sp, lineHeight = (33 * scale).sp,
                color = colors.maroon, textAlign = TextAlign.Center,
            )
            Text(
                "॥ " + verse.number.localized(language) + " ॥",
                fontFamily = fonts.sanskritDevanagari, fontSize = (13 * scale).sp, color = colors.gold,
            )
            if (settings.showTransliteration) {
                Gap(4.dp)
                Text(
                    Transliteration.toIast(verse.sanskrit), fontFamily = fonts.latin, fontStyle = FontStyle.Italic,
                    fontSize = (15 * scale).sp, lineHeight = (23 * scale).sp, color = colors.inkSoft, textAlign = TextAlign.Center,
                )
            }
            val meaning = verse.meaning[language]
            if (settings.showMeanings && meaning != null) {
                Gap(6.dp)
                Text(stringResource(Res.string.meaning).uppercase(), style = MaterialTheme.typography.labelSmall, color = colors.gold)
                Text(
                    meaning, fontFamily = fonts.forLanguage(language), fontSize = (17 * scale).sp, lineHeight = (27 * scale).sp,
                    color = colors.ink, modifier = Modifier.fillMaxWidth(),
                )
            }
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { copy(verse, chapter, part, settings); onCopied() }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Outlined.ContentCopy, stringResource(Res.string.copy), tint = colors.inkSoft, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = { share(verse, chapter, part, settings) }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Outlined.Share, stringResource(Res.string.share), tint = colors.inkSoft, modifier = Modifier.size(18.dp))
                }
            }
        }
        Gap(8.dp)
    }
}

@Composable
private fun ClosingPage(chapter: Chapter, state: ReaderUiState, onNext: (Int) -> Unit, onBackToPart: () -> Unit) {
    val colors = PuranTheme.colors
    val language = state.settings.effectiveLanguage
    val gujarati = state.settings.shlokaScript.resolve(language) == ShlokaScript.GUJARATI
    val next = state.nextChapter
    Leaf {
        Gap(24.dp)
        OmGlyph(72.dp)
        Gap(12.dp)
        if (chapter.colophon.isNotBlank()) {
            Text(
                if (gujarati) Transliteration.toGujarati(chapter.colophon) else chapter.colophon,
                fontFamily = if (gujarati) PuranTheme.fonts.sanskritGujarati else PuranTheme.fonts.sanskritDevanagari,
                fontSize = (18 * state.settings.fontScale).sp, lineHeight = (30 * state.settings.fontScale).sp,
                color = colors.maroon, textAlign = TextAlign.Center,
            )
            Gap(16.dp)
            OrnamentDivider()
            Gap(16.dp)
        }
        Text(
            stringResource(if (next != null) Res.string.chapter_complete else Res.string.puran_complete),
            style = MaterialTheme.typography.headlineSmall, color = colors.saffron, textAlign = TextAlign.Center,
        )
        Gap(6.dp)
        Text(
            stringResource(if (next != null) Res.string.chapter_complete_message else Res.string.puran_complete_message),
            style = MaterialTheme.typography.bodyLarge, color = colors.ink, textAlign = TextAlign.Center,
        )
        Gap(24.dp)
        if (next != null) {
            Button(
                onClick = { onNext(next.id) },
                colors = ButtonDefaults.buttonColors(containerColor = colors.maroon, contentColor = colors.onAccent),
                shape = RoundedCornerShape(26.dp), modifier = Modifier.fillMaxWidth(),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(stringResource(Res.string.next_chapter))
                    Text(
                        (state.nextChapterPart?.name?.get(language)?.let { "$it · " } ?: "") + next.displayTitle(language),
                        style = MaterialTheme.typography.labelSmall, maxLines = 1,
                    )
                }
            }
            Gap(10.dp)
        }
        OutlinedButton(onClick = onBackToPart, shape = RoundedCornerShape(26.dp), modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.back_to_part), color = colors.maroon, modifier = Modifier.padding(vertical = 4.dp))
        }
    }
}

@Composable
private fun PageControls(pagerState: PagerState, pageCount: Int, language: com.shivmahapuran.reader.domain.model.AppLanguage) {
    val scope = rememberCoroutineScope()
    val colors = PuranTheme.colors
    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            enabled = pagerState.currentPage > 0,
            onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
        ) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, stringResource(Res.string.previous), tint = colors.maroon) }
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text(
                stringResource(Res.string.page_of, (pagerState.currentPage + 1).localized(language), pageCount.localized(language)),
                style = MaterialTheme.typography.labelLarge, color = colors.inkSoft,
                modifier = Modifier.background(colors.card, RoundedCornerShape(12.dp)).padding(horizontal = 14.dp, vertical = 4.dp),
            )
        }
        IconButton(
            enabled = pagerState.currentPage < pageCount - 1,
            onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
        ) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, stringResource(Res.string.next), tint = colors.maroon) }
    }
}
