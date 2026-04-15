package com.coffee.server.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.coffee.shared.ApiError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import java.util.*

object JwtConfig {
    const val SECRET = "#izPZkmekXYifnZ!mE#ignHKd6%aBdph"
    const val ISSUER = "coffee-server"
    const val AUDIENCE = "coffee-order-app"
    const val REALM = "coffee-order"

    fun makeToken(userId: Long, username: String, displayName: String, role: String): String {
        return JWT.create()
            .withAudience(AUDIENCE)
            .withIssuer(ISSUER)
            .withClaim("userId", userId)
            .withClaim("username", username)
            .withClaim("displayName", displayName)
            .withClaim("role", role)
            .withExpiresAt(Date(System.currentTimeMillis() + 15 * 86_400_000)) // 15 days
            .sign(Algorithm.HMAC256(SECRET))
    }
}

fun Application.configureAuthentication() {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = JwtConfig.REALM
            verifier(
                JWT.require(Algorithm.HMAC256(JwtConfig.SECRET))
                    .withAudience(JwtConfig.AUDIENCE)
                    .withIssuer(JwtConfig.ISSUER)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(JwtConfig.AUDIENCE)) {
                    JWTPrincipal(credential.payload)
                } else null
            }
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiError("Token is not valid or has expired")
                )
            }
        }
    }
}
