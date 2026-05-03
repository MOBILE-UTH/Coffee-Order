package com.coffee.order.feature.admin.graph

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AdminScreen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Dashboard : AdminScreen("home", "Trang chủ", Icons.Default.Dashboard)
    object Staff : AdminScreen("staff", "Nhân sự", Icons.Default.People)
    object History : AdminScreen("history", "Lịch sử", Icons.Default.History)
}