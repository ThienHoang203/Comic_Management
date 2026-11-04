package com.thien203.comicmanagement.data.model

enum class SortOrder(val displayName: String) {
    TITLE_ASC("Tên A - Z"),
    TITLE_DESC("Tên Z - A"),
    NEWEST("Mới thêm gần đây"),
    OLDEST("Cũ nhất"),
    VOLUMES_DESC("Số tập nhiều nhất"),
    VOLUMES_ASC("Số tập ít nhất"),
    PRICE_DESC("Giá tập cao nhất"),
    PRICE_ASC("Giá tập thấp nhất"),
    TOTAL_PRICE_DESC("Tổng chi tiêu cao nhất")
}
