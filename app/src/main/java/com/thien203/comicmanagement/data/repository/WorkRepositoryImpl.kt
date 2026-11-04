package com.thien203.comicmanagement.data.repository

import com.thien203.comicmanagement.data.local.dao.FormatDao
import com.thien203.comicmanagement.data.local.dao.GenreDao
import com.thien203.comicmanagement.data.local.dao.WorkDao
import com.thien203.comicmanagement.data.local.dao.VolumeDao
import com.thien203.comicmanagement.data.local.entity.FormatEntity
import com.thien203.comicmanagement.data.local.entity.GenreEntity
import com.thien203.comicmanagement.data.local.entity.WorkEntity
import com.thien203.comicmanagement.data.local.entity.WorkGenreCrossRef
import com.thien203.comicmanagement.data.local.entity.VolumeEntity
import com.thien203.comicmanagement.data.local.relation.WorkDetail
import com.thien203.comicmanagement.data.local.relation.WorkWithGenres
import com.thien203.comicmanagement.util.ImageStorageHelper
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class WorkRepositoryImpl(
    private val workDao: WorkDao,
    private val volumeDao: VolumeDao,
    private val genreDao: GenreDao,
    private val formatDao: FormatDao
) : WorkRepository {

    override fun getAllWorksWithGenres(): Flow<List<WorkWithGenres>> {
        return workDao.getAllWorksWithGenres()
    }

    override fun getWorkDetailById(workId: Long): Flow<WorkDetail?> {
        return workDao.getWorkDetailById(workId)
    }

    override fun getAllGenres(): Flow<List<GenreEntity>> {
        return genreDao.getAllGenres()
    }

    override fun getAllFormats(): Flow<List<FormatEntity>> {
        return formatDao.getAllFormats()
    }

    override fun getVolumesByMangaId(mangaId: Long): Flow<List<VolumeEntity>> {
        return volumeDao.getVolumesByMangaId(mangaId)
    }

    override suspend fun saveWork(
        work: WorkEntity,
        genreIds: List<Long>
    ): Long {
        val workId = workDao.insertWork(work)

        // Save cross references for genres
        if (genreIds.isNotEmpty()) {
            val refs = genreIds.map { WorkGenreCrossRef(workId = workId, genreId = it) }
            workDao.insertWorkGenreCrossRefs(refs)
        }

        // Auto-generate volumes
        if (work.totalVolumes > 0) {
            val volumes = (1..work.totalVolumes).map { volNum ->
                VolumeEntity(
                    mangaId = workId,
                    volumeNumber = volNum,
                    isOwned = false,
                    price = work.defaultPrice
                )
            }
            volumeDao.insertVolumes(volumes)
        }

        return workId
    }

    override suspend fun updateWork(
        work: WorkEntity,
        genreIds: List<Long>,
        newTotalVolumes: Int
    ) {
        workDao.updateWork(work.copy(totalVolumes = newTotalVolumes))

        // Update genres
        workDao.deleteWorkGenreCrossRefsByWorkId(work.id)
        if (genreIds.isNotEmpty()) {
            val refs = genreIds.map { WorkGenreCrossRef(workId = work.id, genreId = it) }
            workDao.insertWorkGenreCrossRefs(refs)
        }

        // Update volumes if totalVolumes changed
        val currentVolumes = volumeDao.getVolumesByMangaId(work.id).firstOrNull() ?: emptyList()
        val currentCount = currentVolumes.size

        if (newTotalVolumes > currentCount) {
            // Append new volumes with defaultPrice
            val newVolumes = ((currentCount + 1)..newTotalVolumes).map { volNum ->
                VolumeEntity(
                    mangaId = work.id,
                    volumeNumber = volNum,
                    isOwned = false,
                    price = work.defaultPrice
                )
            }
            volumeDao.insertVolumes(newVolumes)
        } else if (newTotalVolumes < currentCount) {
            // Remove excess volumes when total volumes count is reduced
            volumeDao.deleteExcessVolumes(work.id, newTotalVolumes)
        }
    }

    override suspend fun deleteWork(workId: Long, coverUri: String?) {
        ImageStorageHelper.deleteCoverImageFile(coverUri)
        workDao.deleteWorkById(workId)
    }

    override suspend fun updateVolumeOwnership(volumeId: Long, isOwned: Boolean) {
        volumeDao.updateVolumeOwnership(volumeId, isOwned)
    }

    override suspend fun updateVolumePrice(volumeId: Long, price: Double) {
        volumeDao.updateVolumePrice(volumeId, price)
    }

    override suspend fun updateVolumeCover(volumeId: Long, coverUri: String?) {
        volumeDao.updateVolumeCover(volumeId, coverUri)
    }

    override suspend fun addGenre(name: String, shortName: String) {
        if (name.isNotBlank()) {
            val sName = if (shortName.isNotBlank()) shortName.trim() else name.trim()
            genreDao.insertGenre(GenreEntity(name = name.trim(), shortName = sName))
        }
    }

    override suspend fun updateGenre(genre: GenreEntity) {
        genreDao.updateGenre(genre)
    }

    override suspend fun deleteGenre(genre: GenreEntity) {
        genreDao.deleteGenre(genre)
    }

    override suspend fun addFormat(name: String, shortName: String) {
        if (name.isNotBlank()) {
            formatDao.insertFormat(
                FormatEntity(
                    name = name.trim(),
                    shortName = if (shortName.isNotBlank()) shortName.trim() else name.trim()
                )
            )
        }
    }

    override suspend fun updateFormat(format: FormatEntity) {
        formatDao.updateFormat(format)
    }

    override suspend fun deleteFormat(format: FormatEntity) {
        formatDao.deleteFormat(format)
    }

    override suspend fun generateMockData(count: Int, onProgress: (Float) -> Unit) {
        val genres = genreDao.getAllGenres().firstOrNull() ?: emptyList()
        val formats = formatDao.getAllFormats().firstOrNull() ?: emptyList()

        val prefixes = listOf("Thần Thoại", "Huyền Thoại", "Đại Chiến", "Cuộc Chiến", "Thiên Đường", "Thế Giới", "Vương Quốc", "Bí Mật", "Chiến Binh", "Anh Hùng", "Thất Lạc", "Rồng Thiêng", "Thần Giới", "Tiên Hiệp", "Ma Giới", "Thợ Săn", "Hệ Thống", "Chuyển Sinh", "Kỷ Nguyên", "Vũ Trụ", "Đế Quốc", "Thương Khung", "Phong Vân")
        val suffixes = listOf("Vô Song", "Truyền Kỳ", "Sử Thi", "Tối Cường", "Bất Hủ", "Cuối Cùng", "Tối Cao", "Hoàn Hảo", "Vô Địch", "Đỉnh Phong", "Toàn Năng", "Thần Cấp", "Bất Tử")
        val authors = listOf("Nguyễn Văn A", "Eiichiro Oda", "Akira Toriyama", "Masashi Kishimoto", "Tite Kubo", "Hajime Isayama", "Chugong", "Miya", "Trần Minh", "Mộ Dung")
        val publishers = listOf("NXB Kim Đồng", "NXB Trẻ", "NXB Giáo Dục", "NXB Hội Nhà Văn", "NXB Văn Học", "Shueisha", "Kodansha", "Kadokawa")
        val prices = listOf(20000.0, 25000.0, 30000.0, 35000.0, 40000.0, 50000.0)

        val batchSize = 20
        val totalBatches = (count + batchSize - 1) / batchSize

        for (b in 0 until totalBatches) {
            currentCoroutineContext().ensureActive()

            val currentBatchCount = minOf(batchSize, count - b * batchSize)

            for (i in 0 until currentBatchCount) {
                currentCoroutineContext().ensureActive()

                val index = b * batchSize + i + 1
                val title = "${prefixes.random()} ${suffixes.random()} #$index"
                val author = authors.random()
                val publisher = publishers.random()
                val totalVols = (100..10000).random()
                val defaultPrice = prices.random()
                val formatId = formats.randomOrNull()?.id

                val work = WorkEntity(
                    title = title,
                    author = author,
                    publisher = publisher,
                    totalVolumes = totalVols,
                    defaultPrice = defaultPrice,
                    formatId = formatId,
                    description = "Tác phẩm thử nghiệm $title gồm $totalVols tập."
                )

                val workId = workDao.insertWork(work)

                // Assign random 1..3 genres
                if (genres.isNotEmpty()) {
                    val randomGenres = genres.shuffled().take((1..3).random())
                    val refs = randomGenres.map { WorkGenreCrossRef(workId = workId, genreId = it.id) }
                    workDao.insertWorkGenreCrossRefs(refs)
                }

                // Insert volumes in sub-chunks of 1,000 items
                val volumeChunkSize = 1000
                for (vStart in 1..totalVols step volumeChunkSize) {
                    currentCoroutineContext().ensureActive()
                    val vEnd = minOf(vStart + volumeChunkSize - 1, totalVols)
                    val volumeChunk = (vStart..vEnd).map { volNum ->
                        VolumeEntity(
                            mangaId = workId,
                            volumeNumber = volNum,
                            isOwned = (volNum % 2 == 0),
                            price = defaultPrice
                        )
                    }
                    volumeDao.insertVolumes(volumeChunk)
                }
            }

            onProgress((b + 1).toFloat() / totalBatches)
        }
    }
}
