package com.thien203.comicmanagement.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "work_genre_cross_ref",
    primaryKeys = ["workId", "genreId"],
    indices = [Index("genreId")]
)
data class WorkGenreCrossRef(
    val workId: Long,
    val genreId: Long
)
