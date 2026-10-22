package com.shivmahapuran.reader.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.shivmahapuran.reader.ui.screens.BookScreen
import com.shivmahapuran.reader.ui.screens.MainScreen
import com.shivmahapuran.reader.ui.screens.PartDetailScreen
import com.shivmahapuran.reader.ui.screens.ReaderScreen
import com.shivmahapuran.reader.ui.screens.SearchScreen
import kotlinx.serialization.Serializable

@Serializable data object MainRoute
@Serializable data class PartRoute(val id: Int)
@Serializable data class ReaderRoute(val chapterId: Int, val verse: Int)
@Serializable data object SearchRoute
@Serializable data class BookRoute(val id: String, val page: Int = 0)

@Composable
fun AppNavHost() {
    val nav = rememberNavController()
    val openReader: (Int, Int) -> Unit = { chapter, verse -> nav.navigate(ReaderRoute(chapter, verse)) }
    var selectedTab by rememberSaveable { mutableStateOf("home") }

    val pendingLink by DeepLinkBus.pending.collectAsStateWithLifecycle()
    LaunchedEffect(pendingLink) {
        val link = pendingLink ?: return@LaunchedEffect
        nav.popBackStack<MainRoute>(inclusive = false)
        when (link) {
            is DeepLink.Read -> openReader(link.chapter, link.verse)
            is DeepLink.OpenPart -> nav.navigate(PartRoute(link.part))
            DeepLink.Search -> nav.navigate(SearchRoute)
            is DeepLink.OpenBook -> nav.navigate(BookRoute(link.id))
            is DeepLink.Tab -> selectedTab = link.name
        }
        DeepLinkBus.consume()
    }

    NavHost(
        navController = nav,
        startDestination = MainRoute,
        enterTransition = { slideInHorizontally { it / 4 } + fadeIn() },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { slideOutHorizontally { it / 4 } + fadeOut() },
    ) {
        composable<MainRoute> {
            MainScreen(
                selectedTab = selectedTab,
                onSelectTab = { selectedTab = it },
                onOpenPart = { nav.navigate(PartRoute(it)) },
                onRead = openReader,
                onSearch = { nav.navigate(SearchRoute) },
                onOpenBook = { nav.navigate(BookRoute(it)) },
            )
        }
        composable<PartRoute> { entry ->
            val route = entry.toRoute<PartRoute>()
            PartDetailScreen(partId = route.id, onBack = { nav.popBackStack() }, onRead = openReader)
        }
        composable<ReaderRoute> { entry ->
            val route = entry.toRoute<ReaderRoute>()
            ReaderScreen(
                chapterId = route.chapterId,
                startVerse = route.verse,
                onBack = { nav.popBackStack() },
                onOpenChapter = { next ->
                    nav.navigate(ReaderRoute(next, 1)) {
                        popUpTo<ReaderRoute> { inclusive = true }
                    }
                },
                onBackToPart = { partId ->
                    if (!nav.popBackStack<PartRoute>(inclusive = false)) {
                        nav.popBackStack<MainRoute>(inclusive = false)
                        nav.navigate(PartRoute(partId))
                    }
                },
            )
        }
        composable<BookRoute> { entry ->
            val route = entry.toRoute<BookRoute>()
            BookScreen(bookId = route.id, startPage = route.page, onBack = { nav.popBackStack() })
        }
        composable<SearchRoute> {
            SearchScreen(onBack = { nav.popBackStack() }, onRead = openReader)
        }
    }
}
