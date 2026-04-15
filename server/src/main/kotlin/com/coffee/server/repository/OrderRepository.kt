package com.coffee.server.repository

import com.coffee.server.database.*
import com.coffee.server.database.dbQuery
import com.coffee.shared.dto.CreateOrderRequest
import com.coffee.shared.dto.OrderDto
import com.coffee.shared.dto.OrderItemDetail
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.text.SimpleDateFormat
import java.util.*

class OrderRepository {
    suspend fun getOrders(isPaid: Boolean?): List<OrderDto> = dbQuery {
        val query = if (isPaid != null) {
            Orders.selectAll().where { Orders.isPaid eq isPaid }
        } else {
            Orders.selectAll()
        }

        query.orderBy(Orders.id, SortOrder.DESC).map { row ->
            val orderId = row[Orders.id].value
            val items = getOrderLineItems(orderId)
            val tableName = CoffeeTables.selectAll().where { CoffeeTables.id eq row[Orders.tableId] }
                .singleOrNull()?.get(CoffeeTables.name) ?: "Unknown"
            val staffName = Users.selectAll().where { Users.id eq row[Orders.staffId] }
                .singleOrNull()?.get(Users.displayName) ?: "Unknown"

            OrderDto(
                id = orderId,
                tableId = row[Orders.tableId],
                tableName = tableName,
                staffId = row[Orders.staffId],
                staffName = staffName,
                items = items,
                totalPrice = row[Orders.totalPrice],
                paymentMethod = row[Orders.paymentMethod],
                isPaid = row[Orders.isPaid],
                createdAt = row[Orders.createdAt]
            )
        }
    }

    suspend fun getOrderById(id: Long): OrderDto? = dbQuery {
        Orders.selectAll().where { Orders.id eq id }.singleOrNull()?.let { row ->
            val items = getOrderLineItems(id)
            val tableName = CoffeeTables.selectAll().where { CoffeeTables.id eq row[Orders.tableId] }
                .singleOrNull()?.get(CoffeeTables.name) ?: "Unknown"
            val staffName = Users.selectAll().where { Users.id eq row[Orders.staffId] }
                .singleOrNull()?.get(Users.displayName) ?: "Unknown"

            OrderDto(
                id = id,
                tableId = row[Orders.tableId],
                tableName = tableName,
                staffId = row[Orders.staffId],
                staffName = staffName,
                items = items,
                totalPrice = row[Orders.totalPrice],
                paymentMethod = row[Orders.paymentMethod],
                isPaid = row[Orders.isPaid],
                createdAt = row[Orders.createdAt]
            )
        }
    }

    suspend fun createOrUpdateOrder(staffId: Long, request: CreateOrderRequest): String = dbQuery {
        val existingOrder = Orders.selectAll()
            .where { (Orders.tableId eq request.tableId) and (Orders.isPaid eq false) }
            .singleOrNull()

        if (existingOrder != null) {
            val orderId = existingOrder[Orders.id].value
            updateOrderItems(orderId, request.items)
            "Order updated for table ${request.tableId}"
        } else {
            val orderId = Orders.insert {
                it[tableId] = request.tableId
                it[Orders.staffId] = staffId
                it[createdAt] = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            } get Orders.id
            updateOrderItems(orderId.value, request.items)
            "Order ${orderId.value} created for table ${request.tableId}"
        }
    }

    suspend fun processPayment(orderId: Long): Double? = dbQuery {
        val order = Orders.selectAll().where { Orders.id eq orderId }.singleOrNull() ?: return@dbQuery null
        if (order[Orders.isPaid]) return@dbQuery null

        val subtotal = OrderLineItems.selectAll()
            .where { OrderLineItems.orderId eq orderId }
            .sumOf { it[OrderLineItems.unitPrice] * it[OrderLineItems.quantity] }
        
        val total = subtotal * 1.1 // 10% VAT
        
        Orders.update({ Orders.id eq orderId }) {
            it[isPaid] = true
            it[totalPrice] = total
        }
        total
    }

    private fun getOrderLineItems(orderId: Long): List<OrderItemDetail> {
        return OrderLineItems.selectAll()
            .where { OrderLineItems.orderId eq orderId }
            .map { item ->
                val menuName = MenuItems.selectAll()
                    .where { MenuItems.id eq item[OrderLineItems.menuItemId] }
                    .singleOrNull()?.get(MenuItems.name) ?: "Unknown"

                OrderItemDetail(
                    menuItemId = item[OrderLineItems.menuItemId],
                    menuItemName = menuName,
                    quantity = item[OrderLineItems.quantity],
                    unitPrice = item[OrderLineItems.unitPrice],
                    lineTotal = item[OrderLineItems.unitPrice] * item[OrderLineItems.quantity]
                )
            }
    }

    private fun updateOrderItems(orderId: Long, items: List<com.coffee.shared.dto.OrderItemDto>) {
        OrderLineItems.deleteWhere { OrderLineItems.orderId eq orderId }
        for (item in items) {
            val menuItem = MenuItems.selectAll()
                .where { MenuItems.id eq item.menuItemId }
                .singleOrNull() ?: continue

            OrderLineItems.insert {
                it[OrderLineItems.orderId] = orderId
                it[menuItemId] = item.menuItemId
                it[quantity] = item.quantity
                it[unitPrice] = menuItem[MenuItems.price]
            }
        }
    }
}
