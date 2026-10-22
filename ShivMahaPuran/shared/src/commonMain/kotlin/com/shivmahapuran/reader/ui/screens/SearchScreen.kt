package com.shivmahapuran.reader.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shivmahapuran.reader.domain.model.SearchResult
import com.shivmahapuran.reader.domain.model.ShlokaScript
import com.shivmahapuran.reader.domain.model.resolve
import com.shivmahapuran.reader.presentation.SearchViewModel
import com.shivmahapuran.reader.resources.Res
import com.shivmahapuran.reader.resources.back
import com.shivmahapuran.reader.resources.chapter_n
import com.shivmahapuran.reader.resources.chapter_verse
import com.shivmahapuran.reader.resources.close
import com.shivmahapuran.reader.resources.search_chapters
import com.shivmahapuran.reader.resources.search_empty
import com.shivmahapuran.reader.resources.search_hint
import com.shivmahapuran.reader.resources.search_no_results
import com.shivmahapuran.reader.resources.search_verses
import com.shivmahapuran.reader.ui.components.EmptyState
import com.shivmahapuran.reader.ui.components.Gap
import com.shivmahapuran.reader.ui.components.ParchmentBackground
import com.shivmahapuran.reader.ui.components.ParchmentCard
import com.shivmahapuran.reader.ui.components.SectionTitle
import com.shivmahapuran.reader.ui.theme.PuranTheme
import com.shivmahapuran.reader.util.Transliteration
import com.shivmahapuran.reader.util.localized
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SearchScreen(onBack: () -> Unit, onRead: (chapterId: Int, verse: Int) -> Unit, viewModel: SearchViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = PuranTheme.colors
    val language = state.settings.effectiveLanguage
    val gujarati = state.settings.shlokaScript.resolve(language) == ShlokaScript.GUJARATI
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }

    ParchmentBackground {
        Column(Modifier.fillMaxSize().safeDrawingPadding().imePadding()) {
            Row(Modifier.padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.back), tint = colors.maroon) }
                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    placeholder = { Text(stringResource(Res.string.search_hint), color = colors.inkSoft) },
                    leadingIcon = { Icon(Icons.Filled.Search, null, tint = colors.inkSoft) },
                    trailingIcon = {
                        if (state.query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onQueryChange("") }) { Icon(Icons.Filled.Close, stringResource(Res.string.close), tint = colors.inkSoft) }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.saffron, unfocusedBorderColor = colors.cardBorder,
                        focusedContainerColor = colors.card, unfocusedContainerColor = colors.card,
                        focusedTextColor = colors.ink, unfocusedTextColor = colors.ink, cursorColor = colors.saffron,
                    ),
                    modifier = Modifier.weight(1f).focusRequester(focus),
                )
            }
            val chapters = state.results.filterIsInstance<SearchResult.ChapterMatch>()
            val verses = state.results.filterIsInstance<SearchResult.VerseMatch>()
            when {
                state.query.trim().length < 2 -> EmptyState("", stringResource(Res.string.search_empty))
                state.searching && state.results.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.saffron)
                }
                state.results.isEmpty() -> EmptyState(stringResource(Res.string.search_no_results), "")
                else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (chapters.isNotEmpty()) item { SectionTitle(stringResource(Res.string.search_chapters)) }
                    items(chapters, key = { "c" + it.chapter.id }) { r ->
                        ParchmentCard(Modifier.fillMaxWidth().clickable { onRead(r.chapter.id, 1) }) {
                            Column(Modifier.padding(14.dp)) {
                                Text(r.part.name[language] + " · " + stringResource(Res.string.chapter_n, r.chapter.number.localized(language)),
                                    style = MaterialTheme.typography.labelMedium, color = colors.saffron)
                                Text(r.chapter.displayTitle(language), style = MaterialTheme.typography.titleMedium, color = colors.maroon)
                                r.chapter.summary[language]?.let {
                                    Text(it, style = MaterialTheme.typography.bodySmall, color = colors.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                    if (verses.isNotEmpty()) item { Gap(4.dp); SectionTitle(stringResource(Res.string.search_verses)) }
                    items(verses, key = { "v" + it.verse.id }) { r ->
                        ParchmentCard(Modifier.fillMaxWidth().clickable { onRead(r.chapter.id, r.verse.number) }) {
                            Column(Modifier.padding(14.dp)) {
                                Text(r.part.name[language] + " · " + stringResource(Res.string.chapter_verse, r.chapter.number.localized(language), r.verse.number.localized(language)),
                                    style = MaterialTheme.typography.labelMedium, color = colors.saffron)
                                Text(
                                    if (gujarati) Transliteration.toGujarati(r.verse.sanskrit) else r.verse.sanskrit,
                                    fontFamily = if (gujarati) PuranTheme.fonts.sanskritGujarati else PuranTheme.fonts.sanskritDevanagari,
                                    fontSize = 16.sp, lineHeight = 26.sp, color = colors.maroon, maxLines = 3, overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
