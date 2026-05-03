package com.coffee.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class DashboardStatsDto(
    val totalRevenueToday: Double,
    val totalOrdersToday: Int,
    val bestSellers: List<TopMenuItemDto>
)

@Serializable
data class TopMenuItemDto(
    val menuItemId: Long,
    val name: String,
    val totalQuantitySold: Int,
    val imageUrl: String? = null
)
