package com.coffee.server.database

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Table

object Users : LongIdTable("users") {
    val username = varchar("username", 50).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val displayName = varchar("display_name", 100)
    val role = varchar("role", 20) // OWNER or EMPLOYEE
}

object MenuItems : LongIdTable("menu_items") {
    val name = varchar("name", 100)
    val category = varchar("category", 50)
    val price = integer("price")
    val imageUrl = varchar("image_url", 255).nullable()
}

object CoffeeTables : LongIdTable("coffee_tables") {
    val name = varchar("name", 100)
    val maxPeople = integer("max_people")
}

object Orders : LongIdTable("orders") {
    val tableId = long("table_id")
    val staffId = long("staff_id")
    val totalPrice = double("total_price").default(0.0)
    val paymentMethod = varchar("payment_method", 20).default("CASH")
    val isPaid = bool("is_paid").default(false)
    val createdAt = varchar("created_at", 30)
}

object OrderLineItems : Table("order_line_items") {
    val orderId = long("order_id")
    val menuItemId = long("menu_item_id")
    val quantity = integer("quantity")
    val unitPrice = integer("unit_price")
    override val primaryKey = PrimaryKey(orderId, menuItemId)
}
