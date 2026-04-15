package com.coffee.order.domain

import com.coffee.order.domain.model.HistoryOrder
import com.coffee.order.domain.model.MenuItem
import com.coffee.order.domain.model.TableInfo
import com.coffee.shared.dto.UserDto
import com.coffee.shared.dto.DashboardStatsDto

data class AppUiState(
    val tableInfoList: List<TableInfo> = emptyList(),
    val historyOrders: List<HistoryOrder> = emptyList(),
    val menuItems: List<MenuItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val staffList: List<UserDto> = emptyList(),
    val dashboardStats: DashboardStatsDto? = null
)
