package com.thien203.comicmanagement.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.thien203.comicmanagement.data.local.entity.FormatEntity
import com.thien203.comicmanagement.data.local.entity.GenreEntity
import com.thien203.comicmanagement.data.local.entity.WorkEntity
import com.thien203.comicmanagement.data.local.entity.WorkGenreCrossRef
import com.thien203.comicmanagement.data.local.entity.VolumeEntity

data class WorkDetail(
    @Embedded val work: WorkEntity,
    @Relation(
        parentColumn = "formatId",
        entityColumn = "id"
    )
    val format: FormatEntity? = null,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = WorkGenreCrossRef::class,
            parentColumn = "workId",
            entityColumn = "genreId"
        )
    )
    val genres: List<GenreEntity> = emptyList(),
    @Relation(
        parentColumn = "id",
        entityColumn = "mangaId"
    )
    val volumes: List<VolumeEntity> = emptyList()
)
