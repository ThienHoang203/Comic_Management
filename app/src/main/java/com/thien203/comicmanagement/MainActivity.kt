package com.thien203.comicmanagement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thien203.comicmanagement.data.local.AppDatabase
import com.thien203.comicmanagement.data.repository.WorkRepositoryImpl
import com.thien203.comicmanagement.ui.navigation.Screen
import com.thien203.comicmanagement.ui.screens.add_edit_manga.AddEditMangaScreen
import com.thien203.comicmanagement.ui.screens.add_edit_manga.AddEditMangaViewModel
import com.thien203.comicmanagement.ui.screens.genre_manager.GenreViewModel
import com.thien203.comicmanagement.ui.screens.manga_detail.MangaDetailScreen
import com.thien203.comicmanagement.ui.screens.manga_detail.MangaDetailViewModel
import com.thien203.comicmanagement.ui.screens.manga_list.MangaListScreen
import com.thien203.comicmanagement.ui.screens.manga_list.MangaListViewModel
import com.thien203.comicmanagement.ui.theme.ComicManagementTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getInstance(applicationContext)
        val repository = WorkRepositoryImpl(
            workDao = db.workDao(),
            volumeDao = db.volumeDao(),
            genreDao = db.genreDao(),
            formatDao = db.formatDao()
        )

        setContent {
            ComicManagementTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.MangaList) }

                    // Back button handling when not on home screen
                    if (currentScreen != Screen.MangaList) {
                        BackHandler {
                            currentScreen = when (val screen = currentScreen) {
                                is Screen.MangaDetail -> Screen.MangaList
                                is Screen.AddEditManga -> {
                                    if (screen.returnToDetail && screen.mangaId != null) {
                                        Screen.MangaDetail(screen.mangaId)
                                    } else {
                                        Screen.MangaList
                                    }
                                }
                                else -> Screen.MangaList
                            }
                        }
                    }

                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                                    scaleIn(
                                        initialScale = 0.95f,
                                        animationSpec = tween(220, easing = FastOutSlowInEasing)
                                    )).togetherWith(
                                fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing)) +
                                        scaleOut(
                                            targetScale = 0.98f,
                                            animationSpec = tween(180, easing = FastOutSlowInEasing)
                                        )
                            )
                        },
                        label = "ScreenTransition"
                    ) { screen ->
                        when (screen) {
                            is Screen.MangaList -> {
                                val listViewModel: MangaListViewModel = viewModel(
                                    factory = object : ViewModelProvider.Factory {
                                        @Suppress("UNCHECKED_CAST")
                                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                            return MangaListViewModel(repository) as T
                                        }
                                    }
                                )
                                val genreViewModel: GenreViewModel = viewModel(
                                    factory = object : ViewModelProvider.Factory {
                                        @Suppress("UNCHECKED_CAST")
                                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                            return GenreViewModel(repository) as T
                                        }
                                    }
                                )

                                MangaListScreen(
                                    viewModel = listViewModel,
                                    genreViewModel = genreViewModel,
                                    onNavigateToAdd = {
                                        currentScreen = Screen.AddEditManga(mangaId = null, returnToDetail = false)
                                    },
                                    onNavigateToDetail = { mangaId ->
                                        currentScreen = Screen.MangaDetail(mangaId = mangaId)
                                    },
                                    onNavigateToEdit = { mangaId ->
                                        currentScreen = Screen.AddEditManga(mangaId = mangaId, returnToDetail = false)
                                    }
                                )
                            }

                            is Screen.MangaDetail -> {
                                val detailViewModel: MangaDetailViewModel = viewModel(
                                    key = "detail_${screen.mangaId}",
                                    factory = object : ViewModelProvider.Factory {
                                        @Suppress("UNCHECKED_CAST")
                                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                            return MangaDetailViewModel(screen.mangaId, repository) as T
                                        }
                                    }
                                )

                                MangaDetailScreen(
                                    viewModel = detailViewModel,
                                    onNavigateBack = {
                                        currentScreen = Screen.MangaList
                                    },
                                    onNavigateToEdit = { mangaId ->
                                        currentScreen = Screen.AddEditManga(mangaId = mangaId, returnToDetail = true)
                                    }
                                )
                            }

                            is Screen.AddEditManga -> {
                                val viewModelKey = "add_edit_${screen.mangaId ?: 0}_${System.currentTimeMillis()}"
                                val addEditViewModel: AddEditMangaViewModel = viewModel(
                                    key = viewModelKey,
                                    factory = object : ViewModelProvider.Factory {
                                        @Suppress("UNCHECKED_CAST")
                                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                            return AddEditMangaViewModel(screen.mangaId, repository) as T
                                        }
                                    }
                                )

                                AddEditMangaScreen(
                                    viewModel = addEditViewModel,
                                    onNavigateBack = {
                                        currentScreen = if (screen.returnToDetail && screen.mangaId != null) {
                                            Screen.MangaDetail(screen.mangaId)
                                        } else {
                                            Screen.MangaList
                                        }
                                    }
                                )
                            }

                            is Screen.GenreManager -> {
                                // Managed via Dialog in MangaListScreen
                                currentScreen = Screen.MangaList
                            }
                        }
                    }
                }
            }
        }
    }
}
