package com.thien203.comicmanagement.data.local.dao

import androidx.room.*
import com.thien203.comicmanagement.data.local.entity.WorkEntity
import com.thien203.comicmanagement.data.local.entity.WorkGenreCrossRef
import com.thien203.comicmanagement.data.local.relation.WorkDetail
import com.thien203.comicmanagement.data.local.relation.WorkWithGenres
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWork(work: WorkEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorks(works: List<WorkEntity>): List<Long>

    @Update
    suspend fun updateWork(work: WorkEntity)

    @Query("DELETE FROM works WHERE id = :workId")
    suspend fun deleteWorkById(workId: Long)

    @Transaction
    @Query("SELECT * FROM works ORDER BY createdAt DESC")
    fun getAllWorksWithGenres(): Flow<List<WorkWithGenres>>

    @Transaction
    @Query("SELECT * FROM works WHERE id = :workId")
    fun getWorkDetailById(workId: Long): Flow<WorkDetail?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkGenreCrossRefs(refs: List<WorkGenreCrossRef>)

    @Query("DELETE FROM work_genre_cross_ref WHERE workId = :workId")
    suspend fun deleteWorkGenreCrossRefsByWorkId(workId: Long)
}
