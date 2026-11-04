package com.thien203.comicmanagement.ui.screens.manga_detail

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thien203.comicmanagement.data.local.entity.FormatEntity
import com.thien203.comicmanagement.data.local.relation.WorkDetail
import com.thien203.comicmanagement.data.repository.WorkRepository
import com.thien203.comicmanagement.util.ImageStorageHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MangaDetailViewModel(
    private val workId: Long,
    private val repository: WorkRepository
) : ViewModel() {

    val workDetail: StateFlow<WorkDetail?> = repository.getWorkDetailById(workId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val allFormats: StateFlow<List<FormatEntity>> = repository.getAllFormats().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun toggleVolumeOwnership(volumeId: Long, isOwned: Boolean) {
        viewModelScope.launch {
            repository.updateVolumeOwnership(volumeId, isOwned)
        }
    }

    fun updateVolumePrice(volumeId: Long, newPrice: Double) {
        viewModelScope.launch {
            repository.updateVolumePrice(volumeId, newPrice)
        }
    }

    fun updateComicFormat(formatId: Long?) {
        val currentDetail = workDetail.value ?: return
        viewModelScope.launch {
            repository.updateWork(
                work = currentDetail.work.copy(formatId = formatId),
                genreIds = currentDetail.genres.map { it.id },
                newTotalVolumes = currentDetail.work.totalVolumes
            )
        }
    }

    fun addOneVolume() {
        val currentDetail = workDetail.value ?: return
        val currentCount = currentDetail.volumes.size

        viewModelScope.launch {
            repository.updateWork(
                work = currentDetail.work,
                genreIds = currentDetail.genres.map { it.id },
                newTotalVolumes = currentCount + 1
            )
        }
    }

    fun removeOneVolume() {
        val currentDetail = workDetail.value ?: return
        val currentCount = currentDetail.volumes.size
        if (currentCount <= 1) return

        viewModelScope.launch {
            repository.updateWork(
                work = currentDetail.work,
                genreIds = currentDetail.genres.map { it.id },
                newTotalVolumes = currentCount - 1
            )
        }
    }

    fun updateVolumeCover(context: Context, volumeId: Long, imageUri: Uri) {
        viewModelScope.launch {
            val savedCoverUri = ImageStorageHelper.saveCoverImageToInternalStorage(context, imageUri)
            if (savedCoverUri != null) {
                repository.updateVolumeCover(volumeId, savedCoverUri)
            }
        }
    }

    fun deleteWork(onDeleted: () -> Unit) {
        val current = workDetail.value
        viewModelScope.launch {
            repository.deleteWork(workId, current?.work?.coverUri)
            onDeleted()
        }
    }
}
