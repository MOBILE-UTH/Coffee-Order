package com.coffee.server.auth

import com.coffee.shared.ApiError
import com.coffee.shared.UserRole
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * A simple DSL wrapper for role-based authorization in Ktor routes.
 */
fun Route.authorized(role: UserRole, build: Route.() -> Unit): Route {
    val authorizedRoute = createChild(object : RouteSelector() {
        //        override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation =
//            RouteSelectorEvaluation.Constant
        override suspend fun evaluate(
            context: RoutingResolveContext,
            segmentIndex: Int
        ): RouteSelectorEvaluation {
            return RouteSelectorEvaluation.Constant
        }
    })
    
    authorizedRoute.intercept(ApplicationCallPipeline.Call) {
        val principal = call.principal<JWTPrincipal>()
        val userRole = principal?.role
        
        if (principal == null || userRole != role) {
            call.respond(
                HttpStatusCode.Forbidden, 
                ApiError("Access restricted. Required: $role, Current: ${userRole ?: "NONE"}")
            )
            finish()
        }
    }
    
    authorizedRoute.build()
    return authorizedRoute
}

/**
 * Convenience helper for Owner-only routes.
 */
fun Route.ownerOnly(build: Route.() -> Unit) = authorized(UserRole.OWNER, build)
