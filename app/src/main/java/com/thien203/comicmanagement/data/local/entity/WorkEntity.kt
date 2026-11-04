package com.thien203.comicmanagement.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "works",
    foreignKeys = [
        ForeignKey(
            entity = FormatEntity::class,
            parentColumns = ["id"],
            childColumns = ["formatId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("formatId")]
)
data class WorkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val author: String = "",
    val publisher: String = "",
    val totalVolumes: Int = 1,
    val defaultPrice: Double = 0.0,
    val description: String = "",
    val coverUri: String? = null,
    val formatId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
