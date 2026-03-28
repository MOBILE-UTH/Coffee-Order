package com.coffee.order.viewmodel.model

data class TableInfo(
    val tableId: Long,
    val tableName: String,
    val status: Status,
) {
    enum class Status {
        EMPTY, // Trống
        OCCUPIED, // Có khách
        WAITING_FOR_PAYMENT, // Đang chờ thanh toán
    }
}
