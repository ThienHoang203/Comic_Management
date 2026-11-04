package com.thien203.comicmanagement.data.model

enum class ComicFormat(val displayName: String, val shortName: String) {
    MANGA("Manga (Truyện tranh Nhật)", "Manga"),
    LIGHT_NOVEL("Light Novel (Truyện chữ)", "Light Novel"),
    MANHWA("Manhwa (Truyện Hàn)", "Manhwa"),
    MANHUA("Manhua (Truyện Trung)", "Manhua"),
    COMIC("Comic (Truyện Âu Mỹ)", "Comic"),
    NOVEL("Tiểu thuyết chữ", "Novel")
}
