package com.coffee.server.routes

import com.coffee.server.auth.ownerOnly
import com.coffee.server.repository.MenuRepository
import com.coffee.shared.ApiError
import com.coffee.shared.ApiMessage
import com.coffee.shared.dto.CreateMenuItemRequest
import com.coffee.shared.dto.UpdateMenuItemRequest
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.auth.authenticate
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File
import java.util.UUID

fun Route.menuRoutes(menuRepository: MenuRepository) {
    route("/api/menu") {

        // GET /api/menu
        get {
            val items = menuRepository.getAll()
            call.respond(items)
        }

        // Owner-only write operations
        authenticate("auth-jwt") {
            ownerOnly {
                // POST /api/menu
                post {
                    val request = call.receive<CreateMenuItemRequest>()
                    val item = menuRepository.create(request)
                    call.respond(HttpStatusCode.Created, item)
                }

                // PUT /api/menu/{id}
                put("/{id}") {
                    val menuItemId = call.parameters["id"]?.toLongOrNull()
                    if (menuItemId == null) {
                        call.respond(HttpStatusCode.BadRequest, ApiError("Invalid menu item ID"))
                        return@put
                    }

                    val request = call.receive<UpdateMenuItemRequest>()
                    val updated = menuRepository.update(menuItemId, request)

                    if (!updated) {
                        call.respond(HttpStatusCode.NotFound, ApiError("Menu item not found"))
                        return@put
                    }

                    val item = menuRepository.getById(menuItemId)!!
                    call.respond(item)
                }

                // DELETE /api/menu/{id}
                delete("/{id}") {
                    val menuItemId = call.parameters["id"]?.toLongOrNull()
                    if (menuItemId == null) {
                        call.respond(HttpStatusCode.BadRequest, ApiError("Invalid menu item ID"))
                        return@delete
                    }

                    val deleted = menuRepository.delete(menuItemId)
                    if (!deleted) {
                        call.respond(HttpStatusCode.NotFound, ApiError("Menu item not found"))
                        return@delete
                    }

                    call.respond(ApiMessage("Menu item deleted"))
                }

                // POST /api/menu/{id}/image — upload ảnh cho món ăn
                post("/{id}/image") {
                    val menuItemId = call.parameters["id"]?.toLongOrNull()
                    if (menuItemId == null) {
                        call.respond(HttpStatusCode.BadRequest, ApiError("Invalid menu item ID"))
                        return@post
                    }

                    val multipart = call.receiveMultipart()
                    var savedUrl: String? = null

                    // Đảm bảo thư mục uploads tồn tại
                    val uploadsDir = File("uploads/menu")
                    uploadsDir.mkdirs()

                    multipart.forEachPart { part ->
                        if (part is PartData.FileItem) {
                            val ext = part.originalFileName
                                ?.substringAfterLast('.', "jpg")
                                ?: "jpg"
                            val fileName = "${UUID.randomUUID()}.$ext"
                            val file = File(uploadsDir, fileName)
                            part.streamProvider().use { input ->
                                file.outputStream().buffered().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            savedUrl = "/uploads/menu/$fileName"
                        }
                        part.dispose()
                    }

                    if (savedUrl == null) {
                        call.respond(HttpStatusCode.BadRequest, ApiError("No image file provided"))
                        return@post
                    }

                    val updated = menuRepository.updateImageUrl(menuItemId, savedUrl!!)
                    if (!updated) {
                        call.respond(HttpStatusCode.NotFound, ApiError("Menu item not found"))
                        return@post
                    }

                    val item = menuRepository.getById(menuItemId)!!
                    call.respond(item)
                }
            }
        }
    }
}
