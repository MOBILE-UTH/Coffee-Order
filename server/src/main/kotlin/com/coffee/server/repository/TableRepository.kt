package com.coffee.server.repository

import com.coffee.server.database.CoffeeTables
import com.coffee.server.database.OrderLineItems
import com.coffee.server.database.Orders
import com.coffee.server.database.dbQuery
import com.coffee.shared.dto.ActiveOrderDto
import com.coffee.shared.dto.CreateTableRequest
import com.coffee.shared.dto.OrderItemDto
import com.coffee.shared.dto.TableDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class TableRepository {
    suspend fun getAll(): List<TableDto> = dbQuery {
        CoffeeTables.selectAll().map { row ->
            val tableId = row[CoffeeTables.id].value
            val activeOrder = Orders.selectAll()
                .where { (Orders.tableId eq tableId) and (Orders.isPaid eq false) }
                .singleOrNull()
                ?.let { order ->
                    val items = OrderLineItems.selectAll()
                        .where { OrderLineItems.orderId eq order[Orders.id].value }
                        .map { lineItem ->
                            OrderItemDto(
                                menuItemId = lineItem[OrderLineItems.menuItemId],
                                quantity = lineItem[OrderLineItems.quantity]
                            )
                        }

                    ActiveOrderDto(
                        orderId = order[Orders.id].value,
                        items = items
                    )
                }

            TableDto(
                id = tableId,
                name = row[CoffeeTables.name],
                maxPeople = row[CoffeeTables.maxPeople],
                status = if (activeOrder != null) "OCCUPIED" else "EMPTY",
                activeOrder = activeOrder
            )
        }
    }

    suspend fun create(request: CreateTableRequest): Long = dbQuery {
        CoffeeTables.insert {
            it[name] = request.name
            it[maxPeople] = request.maxPeople
        } get CoffeeTables.id
    }.value

    suspend fun delete(id: Long): Boolean = dbQuery {
        CoffeeTables.deleteWhere { CoffeeTables.id eq id } > 0
    }
}
