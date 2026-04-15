package com.coffee.shared.dto

import com.coffee.shared.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val token: String,
    val userId: Long,
    val username: String,
    val displayName: String,
    val role: UserRole
)

@Serializable
data class RegisterRequest(
    val username: String,
    val password: String,
    val displayName: String,
    val role: UserRole = UserRole.EMPLOYEE
)

@Serializable
data class UserDto(
    val id: Long,
    val username: String,
    val displayName: String,
    val role: UserRole
)
