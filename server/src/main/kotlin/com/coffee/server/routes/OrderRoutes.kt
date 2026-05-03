package com.coffee.server.routes

import com.coffee.server.auth.userId
import com.coffee.server.repository.OrderRepository
import com.coffee.shared.ApiError
import com.coffee.shared.ApiMessage
import com.coffee.shared.dto.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.orderRoutes(orderRepository: OrderRepository) {
    route("/api/orders") {

        // POST /api/orders
        post {
            val staffId = call.principal<JWTPrincipal>()!!.userId
            val request = call.receive<CreateOrderRequest>()

            if (request.items.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, ApiError("Order must have at least one item"))
                return@post
            }

            val message = orderRepository.createOrUpdateOrder(staffId, request)
            if (message.startsWith("Order updated")) {
                call.respond(ApiMessage(message))
            } else {
                call.respond(HttpStatusCode.Created, ApiMessage(message))
            }
        }

        // GET /api/orders
        get {
            val isPaidFilter = call.request.queryParameters["isPaid"]?.toBooleanStrictOrNull()

            call.respond(orderRepository.getOrders(isPaidFilter))
        }

        // PUT /api/orders/{id}/pay
        put("/{id}/pay") {
            val orderId = call.parameters["id"]?.toLongOrNull()
            if (orderId == null) {
                call.respond(HttpStatusCode.BadRequest, ApiError("Invalid order ID"))
                return@put
            }

            val order = orderRepository.getOrderById(orderId)

            if (order == null) {
                call.respond(HttpStatusCode.NotFound, ApiError("Order not found"))
                return@put
            }

            if (order.isPaid) {
                call.respond(HttpStatusCode.BadRequest, ApiError("Order is already paid"))
                return@put
            }

            val total = orderRepository.processPayment(orderId)
                ?: run {
                    call.respond(HttpStatusCode.BadRequest, ApiError("Order is already paid"))
                    return@put
                }

            call.respond(ApiMessage("Payment processed. Total: ${total.toLong()} VND"))
        }
    }
}
