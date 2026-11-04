package com.thien203.comicmanagement.ui.screens.manga_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thien203.comicmanagement.data.local.entity.FormatEntity
import com.thien203.comicmanagement.data.local.entity.GenreEntity
import com.thien203.comicmanagement.data.local.relation.WorkWithGenres
import com.thien203.comicmanagement.data.model.SortOrder
import com.thien203.comicmanagement.data.repository.WorkRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MangaListUiState(
    val works: List<WorkWithGenres> = emptyList(),
    val genres: List<GenreEntity> = emptyList(),
    val formats: List<FormatEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedGenreIds: Set<Long> = emptySet(),
    val selectedFormatId: Long? = null,
    val sortOrder: SortOrder = SortOrder.TITLE_ASC,
    val isLoading: Boolean = false
)

class MangaListViewModel(
    private val repository: WorkRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedGenreIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedGenreIds = _selectedGenreIds.asStateFlow()

    private val _selectedFormatId = MutableStateFlow<Long?>(null)
    val selectedFormatId = _selectedFormatId.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.TITLE_ASC)
    val sortOrder = _sortOrder.asStateFlow()

    private val _genres = repository.getAllGenres().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _formats = repository.getAllFormats().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _worksGenresFormats = combine(
        repository.getAllWorksWithGenres(),
        _genres,
        _formats
    ) { works, genres, formats -> Triple(works, genres, formats) }

    val uiState: StateFlow<MangaListUiState> = combine(
        _worksGenresFormats,
        _searchQuery,
        _selectedGenreIds,
        _selectedFormatId,
        _sortOrder
    ) { (allWorks, genres, formats), query, genreIds, formatId, sort ->
        val filtered = allWorks.filter { workWithGenres ->
            val matchesQuery = query.isBlank() ||
                    workWithGenres.work.title.contains(query, ignoreCase = true) ||
                    workWithGenres.work.author.contains(query, ignoreCase = true) ||
                    workWithGenres.work.publisher.contains(query, ignoreCase = true)

            val workGenreIds = workWithGenres.genres.map { it.id }.toSet()
            val matchesGenre = genreIds.isEmpty() || workGenreIds.containsAll(genreIds)

            val matchesFormat = formatId == null ||
                    workWithGenres.work.formatId == formatId

            matchesQuery && matchesGenre && matchesFormat
        }

        val sorted = filtered.sortedWith { a, b ->
            val avgPriceA = if (a.volumes.isNotEmpty()) a.volumes.map { it.price }.average() else 0.0
            val avgPriceB = if (b.volumes.isNotEmpty()) b.volumes.map { it.price }.average() else 0.0

            val totalSpentA = a.volumes.filter { it.isOwned }.sumOf { it.price }
            val totalSpentB = b.volumes.filter { it.isOwned }.sumOf { it.price }

            when (sort) {
                SortOrder.TITLE_ASC -> a.work.title.compareTo(b.work.title, ignoreCase = true)
                SortOrder.TITLE_DESC -> b.work.title.compareTo(a.work.title, ignoreCase = true)
                SortOrder.NEWEST -> b.work.createdAt.compareTo(a.work.createdAt)
                SortOrder.OLDEST -> a.work.createdAt.compareTo(b.work.createdAt)
                SortOrder.VOLUMES_DESC -> b.work.totalVolumes.compareTo(a.work.totalVolumes)
                SortOrder.VOLUMES_ASC -> a.work.totalVolumes.compareTo(a.work.totalVolumes)
                SortOrder.PRICE_DESC -> avgPriceB.compareTo(avgPriceA)
                SortOrder.PRICE_ASC -> avgPriceA.compareTo(avgPriceB)
                SortOrder.TOTAL_PRICE_DESC -> totalSpentB.compareTo(totalSpentA)
            }
        }

        MangaListUiState(
            works = sorted,
            genres = genres,
            formats = formats,
            searchQuery = query,
            selectedGenreIds = genreIds,
            selectedFormatId = formatId,
            sortOrder = sort,
            isLoading = false
        )
    }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MangaListUiState(isLoading = true)
        )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleGenreFilter(genreId: Long) {
        val current = _selectedGenreIds.value.toMutableSet()
        if (current.contains(genreId)) {
            current.remove(genreId)
        } else {
            current.add(genreId)
        }
        _selectedGenreIds.value = current
    }

    fun clearGenreFilters() {
        _selectedGenreIds.value = emptySet()
    }

    fun onFormatSelected(formatId: Long?) {
        _selectedFormatId.value = if (_selectedFormatId.value == formatId) null else formatId
    }

    fun onSortOrderSelected(order: SortOrder) {
        _sortOrder.value = order
    }

    fun deleteWork(workId: Long, coverUri: String?) {
        viewModelScope.launch {
            repository.deleteWork(workId, coverUri)
        }
    }
}
