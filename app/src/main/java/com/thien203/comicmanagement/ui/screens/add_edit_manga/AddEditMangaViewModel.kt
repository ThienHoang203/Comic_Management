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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FieldWithError {
    TITLE,
    TOTAL_VOLUMES
}

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
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val titleError: String? = null,
    val totalVolumesError: String? = null
)

class AddEditMangaViewModel(
    private val workId: Long?,
    private val repository: WorkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditMangaUiState())
    val uiState: StateFlow<AddEditMangaUiState> = _uiState.asStateFlow()

    val allGenres: StateFlow<List<GenreEntity>> = repository.getAllGenres().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allFormats: StateFlow<List<FormatEntity>> = repository.getAllFormats().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
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
                val avgPrice = if (detail.volumes.isNotEmpty()) {
                    detail.volumes.map { it.price }.average()
                } else 0.0

                _uiState.value = AddEditMangaUiState(
                    title = detail.work.title,
                    author = detail.work.author,
                    publisher = detail.work.publisher,
                    totalVolumes = detail.work.totalVolumes.toString(),
                    defaultPrice = if (avgPrice > 0) String.format("%.0f", avgPrice) else "",
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
        _uiState.value = _uiState.value.copy(
            title = v,
            titleError = if (v.isNotBlank()) null else _uiState.value.titleError
        )
    }

    fun onAuthorChanged(v: String) {
        _uiState.value = _uiState.value.copy(author = v)
    }

    fun onPublisherChanged(v: String) {
        _uiState.value = _uiState.value.copy(publisher = v)
    }

    fun onTotalVolumesChanged(v: String) {
        val isValid = v.toIntOrNull()?.let { it > 0 } == true
        _uiState.value = _uiState.value.copy(
            totalVolumes = v,
            totalVolumesError = if (isValid) null else _uiState.value.totalVolumesError
        )
    }

    fun onDefaultPriceChanged(v: String) {
        _uiState.value = _uiState.value.copy(defaultPrice = v)
    }

    fun onDescriptionChanged(v: String) {
        _uiState.value = _uiState.value.copy(description = v)
    }

    fun onCoverUriSelected(uri: String?) {
        _uiState.value = _uiState.value.copy(coverUri = uri)
    }

    fun onFormatSelected(formatId: Long?) {
        _uiState.value = _uiState.value.copy(selectedFormatId = formatId)
    }

    fun toggleGenre(genreId: Long) {
        val current = _uiState.value.selectedGenreIds.toMutableSet()
        if (current.contains(genreId)) {
            current.remove(genreId)
        } else {
            current.add(genreId)
        }
        _uiState.value = _uiState.value.copy(selectedGenreIds = current)
    }

    fun saveManga(
        context: Context,
        newPickerUri: Uri?,
        onSuccess: () -> Unit,
        onErrorFound: (FieldWithError) -> Unit
    ) {
        val state = _uiState.value
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
            _uiState.value = state.copy(
                titleError = titleErr,
                totalVolumesError = volumesErr
            )
            val firstErrorField = if (titleErr != null) FieldWithError.TITLE else FieldWithError.TOTAL_VOLUMES
            onErrorFound(firstErrorField)
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(
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
                _uiState.value = state.copy(isSaving = false)
            } else {
                repository.saveWork(entity, state.selectedGenreIds.toList())
                _uiState.value = AddEditMangaUiState() // Reset form state after adding new
            }

            onSuccess()
        }
    }
}
