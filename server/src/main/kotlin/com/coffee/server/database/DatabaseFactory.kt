package com.coffee.server.database

import at.favre.lib.crypto.bcrypt.BCrypt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        Database.connect(
            url = "jdbc:h2:file:./data/coffee-order",
            driver = "org.h2.Driver"
        )

        transaction {
            SchemaUtils.create(Users, MenuItems, CoffeeTables, Orders, OrderLineItems)

            // Seed data only if database is empty
            if (Users.selectAll().empty()) {
                seedData()
            }
        }
    }

    private fun seedData() {
        // Owner account: admin / 123456
        Users.insert {
            it[username] = "admin"
            it[passwordHash] = BCrypt.withDefaults().hashToString(12, "123456".toCharArray())
            it[displayName] = "Chủ quán"
            it[role] = "OWNER"
        }

        // Employee account: nhanvien1 / 123456
        Users.insert {
            it[username] = "nhanvien1"
            it[passwordHash] = BCrypt.withDefaults().hashToString(12, "123456".toCharArray())
            it[displayName] = "Nhân viên A"
            it[role] = "EMPLOYEE"
        }
    }
}

/** Run a database query on the IO dispatcher inside a transaction */
suspend fun <T> dbQuery(block: () -> T): T = withContext(Dispatchers.IO) {
    transaction { block() }
}
