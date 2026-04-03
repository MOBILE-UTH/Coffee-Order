package com.coffee.order.viewmodel.model

data class TableInfo(
    val tableId: Long,
    val tableName: String,
    val status: Status,
    val maxPeople: Int,
) {
    enum class Status {
        EMPTY, // Trống
        OCCUPIED, // Có khách
    }
}
