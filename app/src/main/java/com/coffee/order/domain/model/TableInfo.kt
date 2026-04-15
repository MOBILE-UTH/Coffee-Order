package com.coffee.order.domain.model

data class TableInfo(
    val tableId: Long,
    val tableName: String,
    val maxPeople: Int,
    val orderItems: List<OrderItem> = emptyList(),
    val isPayment: Boolean = false,
) {
    data class OrderItem(
        val menuItemId: Long,
        val quantity: Int,
    )

    val status: Status
        get() = when {
            orderItems.isEmpty() -> Status.EMPTY
            else -> Status.OCCUPIED
        }

    enum class Status {
        EMPTY, // Trống
        OCCUPIED, // Có khách
    }
}
