package com.thien203.comicmanagement.data.local.dao

import androidx.room.*
import com.thien203.comicmanagement.data.local.entity.VolumeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VolumeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVolumes(volumes: List<VolumeEntity>)

    @Update
    suspend fun updateVolume(volume: VolumeEntity)

    @Query("SELECT * FROM volumes WHERE mangaId = :mangaId ORDER BY volumeNumber ASC")
    fun getVolumesByMangaId(mangaId: Long): Flow<List<VolumeEntity>>

    @Query("UPDATE volumes SET isOwned = :isOwned WHERE id = :volumeId")
    suspend fun updateVolumeOwnership(volumeId: Long, isOwned: Boolean)

    @Query("UPDATE volumes SET price = :price WHERE id = :volumeId")
    suspend fun updateVolumePrice(volumeId: Long, price: Double)

    @Query("UPDATE volumes SET price = :price WHERE mangaId = :mangaId")
    suspend fun updateAllVolumesPrice(mangaId: Long, price: Double)

    @Query("UPDATE volumes SET coverUri = :coverUri WHERE id = :volumeId")
    suspend fun updateVolumeCover(volumeId: Long, coverUri: String?)

    @Query("DELETE FROM volumes WHERE mangaId = :mangaId")
    suspend fun deleteVolumesByMangaId(mangaId: Long)

    @Query("DELETE FROM volumes WHERE mangaId = :mangaId AND volumeNumber > :maxVolumeNumber")
    suspend fun deleteExcessVolumes(mangaId: Long, maxVolumeNumber: Int)
}
