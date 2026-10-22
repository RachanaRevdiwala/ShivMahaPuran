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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shivmahapuran.reader.domain.model.AppLanguage
import com.shivmahapuran.reader.domain.model.languageName
import com.shivmahapuran.reader.domain.model.Book
import com.shivmahapuran.reader.presentation.LibraryViewModel
import com.shivmahapuran.reader.resources.Res
import com.shivmahapuran.reader.resources.book_pages
import com.shivmahapuran.reader.resources.library_subtitle
import com.shivmahapuran.reader.resources.shown_in
import com.shivmahapuran.reader.ui.components.Gap
import com.shivmahapuran.reader.ui.components.OmGlyph
import com.shivmahapuran.reader.ui.components.ParchmentCard
import com.shivmahapuran.reader.ui.theme.PuranTheme
import com.shivmahapuran.reader.util.localized
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LibraryScreen(
    contentPadding: PaddingValues,
    onOpenBook: (String) -> Unit,
    viewModel: LibraryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val language = state.settings.effectiveLanguage
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = contentPadding.calculateBottomPadding() + 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize().padding(top = contentPadding.calculateTopPadding()),
    ) {
        item {
            Text(
                stringResource(Res.string.library_subtitle),
                style = MaterialTheme.typography.bodyMedium, color = PuranTheme.colors.inkSoft,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            )
        }
        items(state.books, key = { it.id }) { book -> BookCard(book, language) { onOpenBook(book.id) } }
    }
}

@Composable
fun BookCard(book: Book, language: AppLanguage, onClick: () -> Unit) {
    val colors = PuranTheme.colors
    ParchmentCard(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) {
                if (book.language == "sa") OmGlyph(40.dp, color = colors.saffron)
                else Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = colors.saffron, modifier = Modifier.size(34.dp))
            }
            Gap(width = 12.dp)
            Column(Modifier.weight(1f)) {
                Text(book.title[language], style = MaterialTheme.typography.titleMedium, color = colors.maroon)
                Text(book.subtitle[language], style = MaterialTheme.typography.bodyMedium, color = colors.ink)
                Gap(4.dp)
                Text(book.author[language], style = MaterialTheme.typography.labelMedium, color = colors.inkSoft)
                Row {
                    Text(
                        stringResource(Res.string.book_pages, book.sectionCount.localized(language)),
                        style = MaterialTheme.typography.labelSmall, color = colors.saffron,
                    )
                    val bookLanguage = AppLanguage.fromCode(book.language)
                    if (bookLanguage != null && bookLanguage != language) {
                        Text(
                            "  ·  " + stringResource(Res.string.shown_in, languageName(bookLanguage.code, language)),
                            style = MaterialTheme.typography.labelSmall, color = colors.inkSoft,
                        )
                    }
                }
            }
        }
    }
}
