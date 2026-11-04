package com.thien203.comicmanagement.data.local.dao

import androidx.room.*
import com.thien203.comicmanagement.data.local.entity.GenreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GenreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenre(genre: GenreEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaultGenres(genres: List<GenreEntity>)

    @Update
    suspend fun updateGenre(genre: GenreEntity)

    @Delete
    suspend fun deleteGenre(genre: GenreEntity)

    @Query("SELECT * FROM genres ORDER BY name ASC")
    fun getAllGenres(): Flow<List<GenreEntity>>
}
