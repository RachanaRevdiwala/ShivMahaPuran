package com.shivmahapuran.reader.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shivmahapuran.reader.ads.AdBanner
import com.shivmahapuran.reader.domain.model.AppLanguage
import com.shivmahapuran.reader.domain.model.languageName
import com.shivmahapuran.reader.domain.model.AppSettings
import com.shivmahapuran.reader.domain.model.Book
import com.shivmahapuran.reader.domain.model.BookSection
import com.shivmahapuran.reader.domain.model.ShlokaScript
import com.shivmahapuran.reader.domain.model.resolve
import com.shivmahapuran.reader.platform.KeepScreenOn
import com.shivmahapuran.reader.presentation.BookViewModel
import com.shivmahapuran.reader.resources.Res
import com.shivmahapuran.reader.resources.book_complete
import com.shivmahapuran.reader.resources.next
import com.shivmahapuran.reader.resources.page_of
import com.shivmahapuran.reader.resources.previous
import com.shivmahapuran.reader.resources.shown_in
import com.shivmahapuran.reader.resources.source_label
import com.shivmahapuran.reader.ui.components.Gap
import com.shivmahapuran.reader.ui.components.GitaTopBar
import com.shivmahapuran.reader.ui.components.OmGlyph
import com.shivmahapuran.reader.ui.components.OrnamentDivider
import com.shivmahapuran.reader.ui.components.ParchmentBackground
import com.shivmahapuran.reader.ui.components.bookLeaf
import com.shivmahapuran.reader.ui.theme.PuranTheme
import com.shivmahapuran.reader.util.Transliteration
import com.shivmahapuran.reader.util.localized
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Book-style reader for Library books: one framed page per section, turned like the Purana reader. */
@Composable
fun BookScreen(
    bookId: String,
    startPage: Int,
    onBack: () -> Unit,
    viewModel: BookViewModel = koinViewModel(key = "book-$bookId") { parametersOf(bookId, startPage) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    KeepScreenOn(state.settings.keepScreenOn)
    val book = state.book
    if (state.loading || book == null || state.sections.isEmpty()) {
        ParchmentBackground {
            CircularProgressIndicator(Modifier.align(Alignment.Center), color = PuranTheme.colors.saffron)
        }
        return
    }
    val pagerState = rememberPagerState(initialPage = startPage.coerceIn(0, state.sections.size - 1)) { state.sections.size }
    val language = state.settings.effectiveLanguage

    ParchmentBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { GitaTopBar(title = book.title[language], onBack = onBack) },
            bottomBar = {
                Column(Modifier.navigationBarsPadding()) {
                    BookPageControls(pagerState, state.sections.size, language)
                    AdBanner()
                }
            },
        ) { padding ->
            BookPager(pagerState = pagerState, modifier = Modifier.padding(padding).fillMaxSize()) { page ->
                BookPage(
                    book = book,
                    section = state.sections[page],
                    isLast = page == state.sections.lastIndex,
                    settings = state.settings,
                )
            }
        }
    }
}

@Composable
private fun BookPage(book: Book, section: BookSection, isLast: Boolean, settings: AppSettings) {
    val colors = PuranTheme.colors
    val fonts = PuranTheme.fonts
    val language = settings.effectiveLanguage
    val scale = settings.fontScale
    val gujaratiScript = settings.shlokaScript.resolve(language) == ShlokaScript.GUJARATI
    val text = section.textFor(language)

    Box(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 6.dp), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize()
                .bookLeaf()
                .padding(horizontal = 22.dp, vertical = 18.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                book.title[language] + "  ·  " + (section.index + 1).localized(language),
                style = MaterialTheme.typography.labelMedium, color = colors.inkSoft, textAlign = TextAlign.Center,
            )
            Gap(10.dp)
            section.titleFor(language)?.let { title ->
                Text(
                    title,
                    fontFamily = fonts.forLanguage(section.title.keys.firstOrNull { section.title[it] == title } ?: language),
                    style = MaterialTheme.typography.titleLarge, color = colors.maroon, textAlign = TextAlign.Center,
                )
                Gap(6.dp)
                OrnamentDivider()
                Gap(10.dp)
            }
            section.sanskrit?.let { sanskrit ->
                OmGlyph(28.dp, color = colors.gold)
                Gap(6.dp)
                Text(
                    if (gujaratiScript) Transliteration.toGujarati(sanskrit) else sanskrit,
                    fontFamily = if (gujaratiScript) fonts.sanskritGujarati else fonts.sanskritDevanagari,
                    fontSize = (19 * scale).sp,
                    lineHeight = (33 * scale).sp,
                    color = colors.maroon,
                    textAlign = TextAlign.Center,
                )
                Gap(14.dp)
                OrnamentDivider()
                Gap(10.dp)
            }
            if (text != null) {
                val (textLanguage, body) = text
                if (textLanguage != language) {
                    Text(
                        stringResource(Res.string.shown_in, languageName(textLanguage.code, language)),
                        style = MaterialTheme.typography.labelSmall, color = colors.gold,
                    )
                    Gap(6.dp)
                }
                Text(
                    body,
                    fontFamily = fonts.forLanguage(textLanguage),
                    fontSize = (17 * scale).sp,
                    lineHeight = (28 * scale).sp,
                    color = colors.ink,
                    textAlign = if (section.sanskrit != null) TextAlign.Center else TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (isLast) {
                Gap(24.dp)
                OrnamentDivider()
                Gap(10.dp)
                OmGlyph(40.dp)
                Text(
                    stringResource(Res.string.book_complete),
                    style = MaterialTheme.typography.titleMedium, color = colors.saffron, textAlign = TextAlign.Center,
                )
                Gap(12.dp)
                Text(
                    stringResource(Res.string.source_label, book.source),
                    style = MaterialTheme.typography.bodySmall, color = colors.inkSoft, textAlign = TextAlign.Center,
                )
            }
            Gap(12.dp)
        }
    }
}

@Composable
private fun BookPageControls(pagerState: PagerState, pageCount: Int, language: AppLanguage) {
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
