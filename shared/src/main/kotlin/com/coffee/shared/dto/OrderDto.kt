package com.coffee.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class OrderDto(
    val id: Long = 0,
    val tableId: Long,
    val tableName: String = "",
    val staffId: Long = 0,
    val staffName: String = "",
    val items: List<OrderItemDetail> = emptyList(),
    val totalPrice: Double = 0.0,
    val paymentMethod: String = "CASH",
    val isPaid: Boolean = false,
    val createdAt: String = ""
)

@Serializable
data class OrderItemDetail(
    val menuItemId: Long,
    val menuItemName: String,
    val quantity: Int,
    val unitPrice: Int,
    val lineTotal: Int
)

@Serializable
data class CreateOrderRequest(
    val tableId: Long,
    val items: List<OrderItemDto>
)

@Serializable
data class UpdateOrderRequest(
    val items: List<OrderItemDto>
)
