package com.coffee.server.repository

import com.coffee.server.database.MenuItems
import com.coffee.server.database.dbQuery
import com.coffee.shared.dto.CreateMenuItemRequest
import com.coffee.shared.dto.MenuItemDto
import com.coffee.shared.dto.UpdateMenuItemRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class MenuRepository {
    suspend fun getAll(): List<MenuItemDto> = dbQuery {
        MenuItems.selectAll().map { it.toDto() }
    }

    suspend fun getById(id: Long): MenuItemDto? = dbQuery {
        MenuItems.selectAll().where { MenuItems.id eq id }
            .map { it.toDto() }
            .singleOrNull()
    }

    suspend fun create(request: CreateMenuItemRequest): MenuItemDto = dbQuery {
        val id = MenuItems.insert {
            it[name] = request.name
            it[category] = request.category
            it[price] = request.price
        } get MenuItems.id
        
        MenuItemDto(
            id = id.value,
            name = request.name,
            category = request.category,
            price = request.price
        )
    }

    suspend fun update(id: Long, request: UpdateMenuItemRequest): Boolean = dbQuery {
        MenuItems.update({ MenuItems.id eq id }) {
            request.name?.let { name -> it[MenuItems.name] = name }
            request.category?.let { cat -> it[MenuItems.category] = cat }
            request.price?.let { price -> it[MenuItems.price] = price }
        } > 0
    }

    suspend fun delete(id: Long): Boolean = dbQuery {
        MenuItems.deleteWhere { MenuItems.id eq id } > 0
    }

    suspend fun updateImageUrl(id: Long, imageUrl: String): Boolean = dbQuery {
        MenuItems.update({ MenuItems.id eq id }) {
            it[MenuItems.imageUrl] = imageUrl
        } > 0
    }

    private fun ResultRow.toDto() = MenuItemDto(
        id = this[MenuItems.id].value,
        name = this[MenuItems.name],
        category = this[MenuItems.category],
        price = this[MenuItems.price],
        imagePath = this[MenuItems.imageUrl]
    )
}
