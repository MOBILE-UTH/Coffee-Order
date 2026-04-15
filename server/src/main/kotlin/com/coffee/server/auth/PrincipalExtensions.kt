package com.coffee.server.auth

import com.coffee.shared.UserRole
import io.ktor.server.auth.jwt.*

val JWTPrincipal.userId: Long get() = payload.getClaim("userId").asLong()
val JWTPrincipal.username: String get() = payload.getClaim("username").asString()
val JWTPrincipal.displayName: String get() = payload.getClaim("displayName").asString()
val JWTPrincipal.role: UserRole?
    get() = try {
        payload.getClaim("role").asString()?.let { UserRole.valueOf(it.uppercase()) }
    } catch (e: Exception) {
        null
    }
