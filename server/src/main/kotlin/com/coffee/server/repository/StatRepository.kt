package com.coffee.server.repository

import com.coffee.server.database.*
import com.coffee.server.database.dbQuery
import com.coffee.shared.dto.DashboardStatsDto
import com.coffee.shared.dto.TopMenuItemDto
import org.jetbrains.exposed.sql.*
import java.text.SimpleDateFormat
import java.util.*

class StatRepository {

    /**
     * Lấy thống kê cho Dashboard: Doanh thu hôm nay, số đơn, món bán chạy.
     */
    suspend fun getDashboardStats(): DashboardStatsDto = dbQuery {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // 1. Lọc các đơn hàng đã thanh toán trong ngày hôm nay
        val todayOrders = Orders.selectAll()
            .where { (Orders.isPaid eq true) and (Orders.createdAt like "$todayStr%") }
            .toList()

        val totalRevenue = todayOrders.sumOf { it[Orders.totalPrice] }
        val totalOrdersCount = todayOrders.size

        // 2. Tìm các món bán chạy nhất (Top 5)
        val paidOrderIds = todayOrders.map { it[Orders.id].value }

        val bestSellers = if (paidOrderIds.isNotEmpty()) {
            val sumQuantity = OrderLineItems.quantity.sum()
            OrderLineItems
                .select(OrderLineItems.menuItemId, sumQuantity)
                .where { OrderLineItems.orderId inList paidOrderIds }
                .groupBy(OrderLineItems.menuItemId)
                .orderBy(sumQuantity, SortOrder.DESC)
                .limit(5)
                .map { row ->
                    val mId = row[OrderLineItems.menuItemId]
                    val qty = row[sumQuantity] ?: 0
                    
                    // Lấy tên và ảnh từ bảng MenuItems
                    val menuItem = MenuItems.selectAll().where { MenuItems.id eq mId }.singleOrNull()
                    
                    TopMenuItemDto(
                        menuItemId = mId,
                        name = menuItem?.get(MenuItems.name) ?: "Unknown",
                        totalQuantitySold = qty,
                        imageUrl = menuItem?.get(MenuItems.imageUrl)
                    )
                }
        } else {
            emptyList()
        }

        DashboardStatsDto(
            totalRevenueToday = totalRevenue,
            totalOrdersToday = totalOrdersCount,
            bestSellers = bestSellers
        )
    }
}
