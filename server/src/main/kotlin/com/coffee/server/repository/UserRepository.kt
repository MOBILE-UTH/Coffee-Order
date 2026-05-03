package com.coffee.server.repository

import at.favre.lib.crypto.bcrypt.BCrypt
import com.coffee.server.database.Users
import com.coffee.server.database.dbQuery
import com.coffee.shared.UserRole
import com.coffee.shared.dto.LoginRequest
import com.coffee.shared.dto.RegisterRequest
import com.coffee.shared.dto.UserDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class UserRepository {
    suspend fun findByUsername(username: String) = dbQuery {
        Users.selectAll().where { Users.username eq username }.singleOrNull()
    }

    suspend fun findById(id: Long) = dbQuery {
        Users.selectAll().where { Users.id eq id }.singleOrNull()
    }

    suspend fun create(request: RegisterRequest): Boolean = dbQuery {
        Users.insert {
            it[username] = request.username
            it[passwordHash] = BCrypt.withDefaults()
                .hashToString(12, request.password.toCharArray())
            it[displayName] = request.displayName
            it[role] = request.role.name
        }
        true
    }

    /** Lấy toàn bộ tài khoản nhân viên (role = EMPLOYEE). */
    suspend fun getEmployees(): List<UserDto> = dbQuery {
        Users.selectAll()
            .where { Users.role eq UserRole.EMPLOYEE.name }
            .map { row ->
                UserDto(
                    id = row[Users.id].value,
                    username = row[Users.username],
                    displayName = row[Users.displayName],
                    role = UserRole.valueOf(row[Users.role])
                )
            }
    }

    /** Xoá tài khoản theo id. Trả về false nếu không tìm thấy. */
    suspend fun delete(id: Long): Boolean = dbQuery {
        val count = Users.deleteWhere { Users.id eq id }
        count > 0
    }
}
