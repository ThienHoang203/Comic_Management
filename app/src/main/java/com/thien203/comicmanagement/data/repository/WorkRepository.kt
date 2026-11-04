package com.thien203.comicmanagement.data.repository

import com.thien203.comicmanagement.data.local.entity.FormatEntity
import com.thien203.comicmanagement.data.local.entity.GenreEntity
import com.thien203.comicmanagement.data.local.entity.WorkEntity
import com.thien203.comicmanagement.data.local.entity.VolumeEntity
import com.thien203.comicmanagement.data.local.relation.WorkDetail
import com.thien203.comicmanagement.data.local.relation.WorkWithGenres
import kotlinx.coroutines.flow.Flow

interface WorkRepository {
    fun getAllWorksWithGenres(): Flow<List<WorkWithGenres>>
    fun getWorkDetailById(workId: Long): Flow<WorkDetail?>
    fun getAllGenres(): Flow<List<GenreEntity>>
    fun getAllFormats(): Flow<List<FormatEntity>>
    fun getVolumesByMangaId(mangaId: Long): Flow<List<VolumeEntity>>

    suspend fun saveWork(
        work: WorkEntity,
        genreIds: List<Long>
    ): Long

    suspend fun updateWork(
        work: WorkEntity,
        genreIds: List<Long>,
        newTotalVolumes: Int
    )

    suspend fun deleteWork(workId: Long, coverUri: String?)

    suspend fun updateVolumeOwnership(volumeId: Long, isOwned: Boolean)
    suspend fun updateVolumePrice(volumeId: Long, price: Double)
    suspend fun updateVolumeCover(volumeId: Long, coverUri: String?)

    suspend fun addGenre(name: String, shortName: String = "")
    suspend fun updateGenre(genre: GenreEntity)
    suspend fun deleteGenre(genre: GenreEntity)

    suspend fun addFormat(name: String, shortName: String)
    suspend fun updateFormat(format: FormatEntity)
    suspend fun deleteFormat(format: FormatEntity)

    suspend fun generateMockData(count: Int = 1000, onProgress: (Float) -> Unit = {})
}
