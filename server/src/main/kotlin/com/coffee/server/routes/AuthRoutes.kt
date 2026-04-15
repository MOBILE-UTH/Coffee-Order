package com.coffee.server.routes

import at.favre.lib.crypto.bcrypt.BCrypt
import com.coffee.server.auth.*
import com.coffee.server.plugins.JwtConfig
import com.coffee.server.repository.UserRepository
import com.coffee.shared.ApiError
import com.coffee.shared.ApiMessage
import com.coffee.shared.UserRole
import com.coffee.shared.dto.LoginRequest
import com.coffee.shared.dto.LoginResponse
import com.coffee.shared.dto.RegisterRequest
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(userRepository: UserRepository) {
    route("/api/auth") {

        // POST /api/auth/login — public
        post("/login") {
            val request = call.receive<LoginRequest>()
            val user = userRepository.findByUsername(request.username)

            if (user == null) {
                call.respond(HttpStatusCode.Unauthorized, ApiError("Invalid username or password"))
                return@post
            }

            val verified = BCrypt.verifyer().verify(
                request.password.toCharArray(),
                user[com.coffee.server.database.Users.passwordHash]
            ).verified

            if (!verified) {
                call.respond(HttpStatusCode.Unauthorized, ApiError("Invalid username or password"))
                return@post
            }

            val token = JwtConfig.makeToken(
                userId = user[com.coffee.server.database.Users.id].value,
                username = user[com.coffee.server.database.Users.username],
                displayName = user[com.coffee.server.database.Users.displayName],
                role = user[com.coffee.server.database.Users.role]
            )

            call.respond(
                LoginResponse(
                    token = token,
                    userId = user[com.coffee.server.database.Users.id].value,
                    username = user[com.coffee.server.database.Users.username],
                    displayName = user[com.coffee.server.database.Users.displayName],
                    role = UserRole.valueOf(user[com.coffee.server.database.Users.role])
                )
            )
        }

        // POST /api/auth/register — Owner only
        authenticate("auth-jwt") {
            ownerOnly {
                post("/register") {
                    val request = call.receive<RegisterRequest>()
                    val exists = userRepository.findByUsername(request.username)

                    if (exists != null) {
                        call.respond(HttpStatusCode.Conflict, ApiError("Username '${request.username}' already exists"))
                        return@post
                    }

                    userRepository.create(request)
                    call.respond(HttpStatusCode.Created, ApiMessage("User '${request.username}' created successfully"))
                }
            }
        }
    }
}
