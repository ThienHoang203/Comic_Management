package com.thien203.comicmanagement.ui.navigation

sealed class Screen {
    data object MangaList : Screen()
    data class MangaDetail(val mangaId: Long) : Screen()
    data class AddEditManga(val mangaId: Long? = null, val returnToDetail: Boolean = false) : Screen()
    data object GenreManager : Screen()
}
