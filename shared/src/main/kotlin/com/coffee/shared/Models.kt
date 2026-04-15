package com.coffee.shared

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    OWNER,
    EMPLOYEE
}

@Serializable
data class ApiError(val message: String)

@Serializable
data class ApiMessage(val message: String)
