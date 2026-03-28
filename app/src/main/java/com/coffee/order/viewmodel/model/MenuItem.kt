package com.coffee.order.viewmodel.model

data class MenuItem(
    val menuItemId: Long = 0L,
    val name: String,
    val category: String,
    val price: Double,
)
