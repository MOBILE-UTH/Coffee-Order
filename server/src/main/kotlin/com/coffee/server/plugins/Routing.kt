package com.coffee.server.plugins

import com.coffee.server.routes.*
import com.coffee.server.repository.MenuRepository
import com.coffee.server.repository.OrderRepository
import com.coffee.server.repository.StatRepository
import com.coffee.server.repository.TableRepository
import com.coffee.server.repository.UserRepository
import com.coffee.shared.ApiError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Application.configureRouting() {
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiError("Internal server error: ${cause.localizedMessage}")
            )
        }
    }

    routing {
        val userRepository = UserRepository()
        val menuRepository = MenuRepository()
        val tableRepository = TableRepository()
        val orderRepository = OrderRepository()
        val statRepository = StatRepository()

        // Serve uploaded images as static files: GET /uploads/menu/<filename>
        staticFiles("/uploads", File("uploads"))

        // Public routes
        healthRoutes()
        authRoutes(userRepository)

        // Protected routes (require JWT)
        authenticate("auth-jwt") {
            menuRoutes(menuRepository)
            tableRoutes(tableRepository)
            orderRoutes(orderRepository)
            userRoutes(userRepository)
            statRoutes(statRepository)
        }
    }
}
