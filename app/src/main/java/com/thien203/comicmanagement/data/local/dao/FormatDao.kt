package com.thien203.comicmanagement.data.local.dao

import androidx.room.*
import com.thien203.comicmanagement.data.local.entity.FormatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FormatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFormat(format: FormatEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaultFormats(formats: List<FormatEntity>)

    @Update
    suspend fun updateFormat(format: FormatEntity)

    @Delete
    suspend fun deleteFormat(format: FormatEntity)

    @Query("SELECT * FROM formats ORDER BY name ASC")
    fun getAllFormats(): Flow<List<FormatEntity>>
}
