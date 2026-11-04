package com.thien203.comicmanagement.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "genres",
    indices = [Index(value = ["name"], unique = true)]
)
data class GenreEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val shortName: String = name
)
