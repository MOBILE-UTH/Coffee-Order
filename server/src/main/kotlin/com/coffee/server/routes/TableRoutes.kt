package com.coffee.server.routes

import com.coffee.server.auth.ownerOnly
import com.coffee.server.repository.TableRepository
import com.coffee.shared.ApiError
import com.coffee.shared.ApiMessage
import com.coffee.shared.dto.CreateTableRequest
import com.coffee.shared.dto.TableDto
import io.ktor.http.*
import io.ktor.server.auth.authenticate
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.tableRoutes(tableRepository: TableRepository) {
    route("/api/tables") {

        // GET /api/tables
        get {
            call.respond(tableRepository.getAll())
        }

        // POST /api/tables
        authenticate("auth-jwt") {
            post {
                val request = call.receive<CreateTableRequest>()
                val id = tableRepository.create(request)

                call.respond(
                    HttpStatusCode.Created,
                    TableDto(id = id, name = request.name, maxPeople = request.maxPeople)
                )
            }

            ownerOnly {
                // DELETE /api/tables/{id}
                delete("/{id}") {
                    val tableId = call.parameters["id"]?.toLongOrNull()
                    if (tableId == null) {
                        call.respond(HttpStatusCode.BadRequest, ApiError("Invalid table ID"))
                        return@delete
                    }

                    val deleted = tableRepository.delete(tableId)

                    if (!deleted) {
                        call.respond(HttpStatusCode.NotFound, ApiError("Table not found"))
                        return@delete
                    }

                    call.respond(ApiMessage("Table deleted"))
                }
            }
        }
    }
}
