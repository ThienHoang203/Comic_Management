package com.thien203.comicmanagement.ui.screens.add_edit_manga

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thien203.comicmanagement.data.local.entity.FormatEntity
import com.thien203.comicmanagement.data.local.entity.GenreEntity
import com.thien203.comicmanagement.data.local.entity.WorkEntity
import com.thien203.comicmanagement.data.repository.WorkRepository
import com.thien203.comicmanagement.util.ImageStorageHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class FieldWithError {
    TITLE,
    TOTAL_VOLUMES
}

private data class AddEditFormState(
    val title: String = "",
    val author: String = "",
    val publisher: String = "",
    val totalVolumes: String = "1",
    val defaultPrice: String = "",
    val description: String = "",
    val coverUri: String? = null,
    val selectedFormatId: Long? = null,
    val selectedGenreIds: Set<Long> = emptySet(),
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val titleError: String? = null,
    val totalVolumesError: String? = null
)

data class AddEditMangaUiState(
    val title: String = "",
    val author: String = "",
    val publisher: String = "",
    val totalVolumes: String = "1",
    val defaultPrice: String = "",
    val description: String = "",
    val coverUri: String? = null,
    val selectedFormatId: Long? = null,
    val selectedGenreIds: Set<Long> = emptySet(),
    val allGenres: List<GenreEntity> = emptyList(),
    val allFormats: List<FormatEntity> = emptyList(),
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val titleError: String? = null,
    val totalVolumesError: String? = null
)

class AddEditMangaViewModel(
    private val workId: Long?,
    private val repository: WorkRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(AddEditFormState())

    // Cached reactive stream for Genres
    val allGenres: StateFlow<List<GenreEntity>> = repository.getAllGenres()
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Cached reactive stream for Formats
    val allFormats: StateFlow<List<FormatEntity>> = repository.getAllFormats()
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Unified Atomic UI State combining form state with cached DB lists
    val uiState: StateFlow<AddEditMangaUiState> = combine(
        _formState,
        allGenres,
        allFormats
    ) { form, genres, formats ->
        // Self-healing cache: Filter out any selectedGenreIds that were deleted from DB
        val validGenreIds = form.selectedGenreIds.filter { id -> genres.any { it.id == id } }.toSet()
        // Self-healing cache: Reset selectedFormatId to null if it was deleted from DB
        val validFormatId = if (formats.any { it.id == form.selectedFormatId }) form.selectedFormatId else null

        AddEditMangaUiState(
            title = form.title,
            author = form.author,
            publisher = form.publisher,
            totalVolumes = form.totalVolumes,
            defaultPrice = form.defaultPrice,
            description = form.description,
            coverUri = form.coverUri,
            selectedFormatId = validFormatId,
            selectedGenreIds = validGenreIds,
            allGenres = genres,
            allFormats = formats,
            isEditing = form.isEditing,
            isSaving = form.isSaving,
            titleError = form.titleError,
            totalVolumesError = form.totalVolumesError
        )
    }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AddEditMangaUiState()
        )

    init {
        if (workId != null && workId > 0) {
            loadWork(workId)
        }
    }

    private fun loadWork(id: Long) {
        viewModelScope.launch {
            val detail = repository.getWorkDetailById(id).firstOrNull()
            if (detail != null) {
                _formState.value = AddEditFormState(
                    title = detail.work.title,
                    author = detail.work.author,
                    publisher = detail.work.publisher,
                    totalVolumes = detail.work.totalVolumes.toString(),
                    defaultPrice = if (detail.work.defaultPrice > 0) String.format("%.0f", detail.work.defaultPrice) else "",
                    description = detail.work.description,
                    coverUri = detail.work.coverUri,
                    selectedFormatId = detail.work.formatId,
                    selectedGenreIds = detail.genres.map { it.id }.toSet(),
                    isEditing = true
                )
            }
        }
    }

    fun onTitleChanged(v: String) {
        _formState.value = _formState.value.copy(
            title = v,
            titleError = if (v.isNotBlank()) null else _formState.value.titleError
        )
    }

    fun onAuthorChanged(v: String) {
        _formState.value = _formState.value.copy(author = v)
    }

    fun onPublisherChanged(v: String) {
        _formState.value = _formState.value.copy(publisher = v)
    }

    fun onTotalVolumesChanged(v: String) {
        val isValid = v.toIntOrNull()?.let { it > 0 } == true
        _formState.value = _formState.value.copy(
            totalVolumes = v,
            totalVolumesError = if (isValid) null else _formState.value.totalVolumesError
        )
    }

    fun onDefaultPriceChanged(v: String) {
        _formState.value = _formState.value.copy(defaultPrice = v)
    }

    fun onDescriptionChanged(v: String) {
        _formState.value = _formState.value.copy(description = v)
    }

    fun onCoverUriSelected(uri: String?) {
        _formState.value = _formState.value.copy(coverUri = uri)
    }

    fun onFormatSelected(formatId: Long?) {
        _formState.value = _formState.value.copy(selectedFormatId = formatId)
    }

    fun toggleGenre(genreId: Long) {
        val current = _formState.value.selectedGenreIds.toMutableSet()
        if (current.contains(genreId)) {
            current.remove(genreId)
        } else {
            current.add(genreId)
        }
        _formState.value = _formState.value.copy(selectedGenreIds = current)
    }

    fun saveManga(
        context: Context,
        newPickerUri: Uri?,
        onSuccess: () -> Unit,
        onErrorFound: (FieldWithError) -> Unit
    ) {
        val state = _formState.value
        var titleErr: String? = null
        var volumesErr: String? = null

        if (state.title.isBlank()) {
            titleErr = "Tên tác phẩm không được để trống"
        }

        val totalVols = state.totalVolumes.toIntOrNull()
        if (totalVols == null || totalVols <= 0) {
            volumesErr = "Tổng số tập phải là số nguyên lớn hơn 0"
        }

        if (titleErr != null || volumesErr != null) {
            _formState.value = state.copy(
                titleError = titleErr,
                totalVolumesError = volumesErr
            )
            val firstErrorField = if (titleErr != null) FieldWithError.TITLE else FieldWithError.TOTAL_VOLUMES
            onErrorFound(firstErrorField)
            return
        }

        viewModelScope.launch {
            _formState.value = state.copy(
                titleError = null,
                totalVolumesError = null,
                isSaving = true
            )

            // Save new photo if picked
            val finalCoverUri = if (newPickerUri != null) {
                ImageStorageHelper.saveCoverImageToInternalStorage(context, newPickerUri)
            } else {
                state.coverUri
            }

            val entity = WorkEntity(
                id = workId ?: 0,
                title = state.title.trim(),
                author = state.author.trim(),
                publisher = state.publisher.trim(),
                totalVolumes = totalVols!!,
                defaultPrice = state.defaultPrice.toDoubleOrNull() ?: 0.0,
                description = state.description.trim(),
                coverUri = finalCoverUri,
                formatId = state.selectedFormatId
            )

            if (workId != null && workId > 0) {
                repository.updateWork(entity, state.selectedGenreIds.toList(), totalVols)
                _formState.value = state.copy(isSaving = false)
            } else {
                repository.saveWork(entity, state.selectedGenreIds.toList())
                _formState.value = AddEditFormState() // Reset form state after adding new
            }

            onSuccess()
        }
    }
}
