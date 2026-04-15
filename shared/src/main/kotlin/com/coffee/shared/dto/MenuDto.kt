package com.coffee.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class MenuItemDto(
    val id: Long = 0,
    val name: String,
    val category: String,
    val price: Int,
    val imagePath: String? = null
)

@Serializable
data class CreateMenuItemRequest(
    val name: String,
    val category: String,
    val price: Int
)

@Serializable
data class UpdateMenuItemRequest(
    val name: String? = null,
    val category: String? = null,
    val price: Int? = null
)
