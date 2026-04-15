package com.coffee.server.routes

import com.coffee.server.auth.ownerOnly
import com.coffee.server.repository.StatRepository
import io.ktor.http.*
import io.ktor.server.auth.authenticate
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Các endpoint thống kê (OWNER only).
 */
fun Route.statRoutes(statRepository: StatRepository) {
    authenticate("auth-jwt") {
        route("/api/stats") {
            ownerOnly {
                get {
                    val stats = statRepository.getDashboardStats()
                    call.respond(HttpStatusCode.OK, stats)
                }
            }
        }
    }
}
