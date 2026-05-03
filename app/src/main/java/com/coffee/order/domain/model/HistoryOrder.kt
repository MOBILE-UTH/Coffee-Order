package com.coffee.order.domain.model

data class HistoryOrder(
    val orderId: Long = 0L,
    val staffName: String,
    val tableId: Long,
    val menuItems: List<MenuItem>,
    val totalPrice: Double?,
    val orderTime: String,
)
