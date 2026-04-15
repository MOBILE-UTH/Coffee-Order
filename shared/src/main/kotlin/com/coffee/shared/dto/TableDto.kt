package com.coffee.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class TableDto(
    val id: Long = 0,
    val name: String,
    val maxPeople: Int,
    val status: String = "EMPTY",
    val activeOrder: ActiveOrderDto? = null
)

@Serializable
data class CreateTableRequest(
    val name: String,
    val maxPeople: Int
)

@Serializable
data class ActiveOrderDto(
    val orderId: Long,
    val items: List<OrderItemDto> = emptyList()
)

@Serializable
data class OrderItemDto(
    val menuItemId: Long,
    val quantity: Int
)
