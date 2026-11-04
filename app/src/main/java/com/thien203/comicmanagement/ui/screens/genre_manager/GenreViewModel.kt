package com.thien203.comicmanagement.ui.screens.genre_manager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thien203.comicmanagement.data.local.entity.FormatEntity
import com.thien203.comicmanagement.data.local.entity.GenreEntity
import com.thien203.comicmanagement.data.repository.WorkRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GenreViewModel(
    private val repository: WorkRepository
) : ViewModel() {

    val genres: StateFlow<List<GenreEntity>> = repository.getAllGenres().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val formats: StateFlow<List<FormatEntity>> = repository.getAllFormats().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addGenre(name: String, shortName: String = "") {
        viewModelScope.launch {
            repository.addGenre(name, shortName)
        }
    }

    fun updateGenre(genre: GenreEntity) {
        viewModelScope.launch {
            repository.updateGenre(genre)
        }
    }

    fun deleteGenre(genre: GenreEntity) {
        viewModelScope.launch {
            repository.deleteGenre(genre)
        }
    }

    fun addFormat(name: String, shortName: String) {
        viewModelScope.launch {
            repository.addFormat(name, shortName)
        }
    }

    fun updateFormat(format: FormatEntity) {
        viewModelScope.launch {
            repository.updateFormat(format)
        }
    }

    fun deleteFormat(format: FormatEntity) {
        viewModelScope.launch {
            repository.deleteFormat(format)
        }
    }
}
