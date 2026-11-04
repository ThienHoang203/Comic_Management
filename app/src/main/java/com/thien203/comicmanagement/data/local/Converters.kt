package com.thien203.comicmanagement.data.local

import androidx.room.TypeConverter
import com.thien203.comicmanagement.data.model.ComicFormat

class Converters {
    @TypeConverter
    fun fromComicFormat(value: ComicFormat): String = value.name

    @TypeConverter
    fun toComicFormat(value: String): ComicFormat = runCatching {
        ComicFormat.valueOf(value)
    }.getOrDefault(ComicFormat.MANGA)
}
