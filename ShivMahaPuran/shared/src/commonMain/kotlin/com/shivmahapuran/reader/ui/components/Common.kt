package com.shivmahapuran.reader.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shivmahapuran.reader.resources.Res
import com.shivmahapuran.reader.resources.back
import com.shivmahapuran.reader.ui.theme.PuranTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun GitaTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {},
) {
    val colors = PuranTheme.colors
    CenterAlignedTopAppBar(
        title = {
            Text(
                title, maxLines = 1, overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge, color = colors.maroon,
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.back), tint = colors.maroon)
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        ),
    )
}

@Composable
fun EmptyState(title: String, message: String, modifier: Modifier = Modifier) {
    val colors = PuranTheme.colors
    Column(
        modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OmGlyph(56.dp, color = colors.gold)
        Gap(12.dp)
        if (title.isNotBlank()) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = colors.maroon, textAlign = TextAlign.Center)
            Gap(8.dp)
        }
        if (message.isNotBlank()) {
            Text(message, style = MaterialTheme.typography.bodyMedium, color = colors.inkSoft, textAlign = TextAlign.Center)
        }
        Gap(8.dp)
        OrnamentDivider(Modifier.padding(horizontal = 48.dp))
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = PuranTheme.colors.maroon,
        modifier = modifier,
    )
}
