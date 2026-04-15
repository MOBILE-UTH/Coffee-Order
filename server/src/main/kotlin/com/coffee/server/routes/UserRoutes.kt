package com.coffee.server.routes

import com.coffee.server.auth.*
import com.coffee.server.repository.UserRepository
import com.coffee.shared.ApiError
import com.coffee.shared.ApiMessage
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Routes quản lý nhân viên (OWNER-only).
 *
 * GET    /api/users          — danh sách tất cả nhân viên (EMPLOYEE)
 * DELETE /api/users/{id}     — xoá tài khoản nhân viên
 *
 * Đăng ký tài khoản vẫn ở POST /api/auth/register
 */
fun Route.userRoutes(userRepository: UserRepository) {
    authenticate("auth-jwt") {
        route("/api/users") {

            // GET /api/users — danh sách tất cả EMPLOYEE
            ownerOnly {
                get {
                    val employees = userRepository.getEmployees()
                    call.respond(HttpStatusCode.OK, employees)
                }
            }

            // DELETE /api/users/{id}
            ownerOnly {
                delete("/{id}") {
                    val id = call.parameters["id"]?.toLongOrNull()
                        ?: return@delete call.respond(
                            HttpStatusCode.BadRequest,
                            ApiError("ID tài khoản không hợp lệ")
                        )

                    // Không cho phép xoá chính mình
                    val requesterId = call.principal<JWTPrincipal>()?.userId
                    if (requesterId == id) {
                        return@delete call.respond(
                            HttpStatusCode.Forbidden,
                            ApiError("Không thể xoá tài khoản của chính mình")
                        )
                    }

                    val deleted = userRepository.delete(id)
                    if (deleted) {
                        call.respond(HttpStatusCode.OK, ApiMessage("Đã xoá tài khoản #$id"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ApiError("Không tìm thấy tài khoản #$id"))
                    }
                }
            }
        }
    }
}
