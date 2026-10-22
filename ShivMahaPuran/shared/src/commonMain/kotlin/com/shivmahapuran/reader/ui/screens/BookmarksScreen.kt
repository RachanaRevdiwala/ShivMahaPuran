package com.shivmahapuran.reader.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shivmahapuran.reader.domain.model.ShlokaScript
import com.shivmahapuran.reader.domain.model.resolve
import com.shivmahapuran.reader.presentation.BookmarksViewModel
import com.shivmahapuran.reader.resources.Res
import com.shivmahapuran.reader.resources.bookmark_remove
import com.shivmahapuran.reader.resources.chapter_verse
import com.shivmahapuran.reader.resources.no_bookmarks
import com.shivmahapuran.reader.resources.no_bookmarks_hint
import com.shivmahapuran.reader.ui.components.EmptyState
import com.shivmahapuran.reader.ui.components.Gap
import com.shivmahapuran.reader.ui.components.ParchmentCard
import com.shivmahapuran.reader.ui.theme.PuranTheme
import com.shivmahapuran.reader.util.Transliteration
import com.shivmahapuran.reader.util.localized
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BookmarksScreen(
    contentPadding: PaddingValues,
    onRead: (chapterId: Int, verse: Int) -> Unit,
    viewModel: BookmarksViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = PuranTheme.colors
    val language = state.settings.effectiveLanguage
    val gujarati = state.settings.shlokaScript.resolve(language) == ShlokaScript.GUJARATI
    if (!state.loading && state.bookmarks.isEmpty()) {
        EmptyState(stringResource(Res.string.no_bookmarks), stringResource(Res.string.no_bookmarks_hint), Modifier.padding(contentPadding))
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = contentPadding.calculateBottomPadding() + 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize().padding(top = contentPadding.calculateTopPadding()),
    ) {
        items(state.bookmarks, key = { "${it.chapter.id}-${it.verseNumber}" }) { item ->
            ParchmentCard(Modifier.fillMaxWidth().animateItem().clickable { onRead(item.chapter.id, item.verseNumber) }) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(item.part.name[language], style = MaterialTheme.typography.labelMedium, color = colors.inkSoft)
                            Text(
                                stringResource(Res.string.chapter_verse, item.chapter.number.localized(language), item.verseNumber.localized(language)),
                                style = MaterialTheme.typography.labelLarge, color = colors.saffron,
                            )
                        }
                        IconButton(onClick = { viewModel.remove(item) }) {
                            Icon(Icons.Filled.Bookmark, stringResource(Res.string.bookmark_remove), tint = colors.saffron)
                        }
                    }
                    Text(item.chapter.displayTitle(language), style = MaterialTheme.typography.titleSmall, color = colors.maroon)
                    item.verse?.let { verse ->
                        Gap(6.dp)
                        Text(
                            if (gujarati) Transliteration.toGujarati(verse.sanskrit) else verse.sanskrit,
                            fontFamily = if (gujarati) PuranTheme.fonts.sanskritGujarati else PuranTheme.fonts.sanskritDevanagari,
                            fontSize = 16.sp, lineHeight = 27.sp, color = colors.maroon, maxLines = 4, overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}
