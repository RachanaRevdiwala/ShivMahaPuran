package com.shivmahapuran.reader.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.shivmahapuran.reader.ads.AdBanner
import com.shivmahapuran.reader.resources.Res
import com.shivmahapuran.reader.resources.nav_bookmarks
import com.shivmahapuran.reader.resources.nav_home
import com.shivmahapuran.reader.resources.nav_library
import com.shivmahapuran.reader.resources.nav_samhitas
import com.shivmahapuran.reader.resources.nav_settings
import com.shivmahapuran.reader.resources.search
import com.shivmahapuran.reader.ui.components.GitaTopBar
import com.shivmahapuran.reader.ui.components.ParchmentBackground
import com.shivmahapuran.reader.ui.theme.PuranTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private enum class Tab(val label: StringResource, val selected: ImageVector, val unselected: ImageVector) {
    HOME(Res.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home),
    SAMHITAS(Res.string.nav_samhitas, Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook),
    STOTRAS(Res.string.nav_library, Icons.Filled.LocalLibrary, Icons.Outlined.LocalLibrary),
    BOOKMARKS(Res.string.nav_bookmarks, Icons.Filled.Bookmarks, Icons.Outlined.Bookmarks),
    SETTINGS(Res.string.nav_settings, Icons.Filled.Settings, Icons.Outlined.Settings),
}

@Composable
fun MainScreen(
    selectedTab: String,
    onSelectTab: (String) -> Unit,
    onOpenPart: (Int) -> Unit,
    onRead: (chapter: Int, verse: Int) -> Unit,
    onSearch: () -> Unit,
    onOpenBook: (String) -> Unit,
) {
    val tab = Tab.entries.firstOrNull { it.name.equals(selectedTab, ignoreCase = true) } ?: Tab.HOME
    val tabIndex = tab.ordinal
    val colors = PuranTheme.colors

    ParchmentBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                if (tab != Tab.HOME) {
                    GitaTopBar(
                        title = stringResource(tab.label),
                        actions = {
                            if (tab == Tab.SAMHITAS || tab == Tab.BOOKMARKS) {
                                IconButton(onClick = onSearch) {
                                    Icon(Icons.Filled.Search, stringResource(Res.string.search), tint = colors.maroon)
                                }
                            }
                        },
                    )
                }
            },
            bottomBar = {
                Column {
                    AdBanner()
                    HorizontalDivider(color = colors.cardBorder, thickness = 0.8.dp)
                    NavigationBar(containerColor = colors.barSurface) {
                        Tab.entries.forEachIndexed { index, item ->
                            NavigationBarItem(
                                selected = index == tabIndex,
                                onClick = { onSelectTab(item.name.lowercase()) },
                                icon = { Icon(if (index == tabIndex) item.selected else item.unselected, null) },
                                label = { Text(stringResource(item.label)) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = colors.maroon,
                                    selectedTextColor = colors.maroon,
                                    indicatorColor = colors.saffron.copy(alpha = 0.22f),
                                    unselectedIconColor = colors.inkSoft,
                                    unselectedTextColor = colors.inkSoft,
                                ),
                            )
                        }
                    }
                }
            },
        ) { padding ->
            Crossfade(targetState = tab) { current ->
                when (current) {
                    Tab.HOME -> HomeScreen(padding, onOpenPart, onRead, onSearch, onOpenBook)
                    Tab.SAMHITAS -> PartsScreen(padding, onOpenPart)
                    Tab.STOTRAS -> LibraryScreen(padding, onOpenBook)
                    Tab.BOOKMARKS -> BookmarksScreen(padding, onRead)
                    Tab.SETTINGS -> SettingsScreen(padding)
                }
            }
        }
    }
}
