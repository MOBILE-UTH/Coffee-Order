package com.coffee.order.repository

import com.coffee.order.network.ApiClient
import com.coffee.order.network.TokenManager
import com.coffee.order.network.withAuth
import com.coffee.shared.UserRole
import com.coffee.shared.dto.CreateMenuItemRequest
import com.coffee.shared.dto.CreateOrderRequest
import com.coffee.shared.dto.CreateTableRequest
import com.coffee.shared.dto.DashboardStatsDto
import com.coffee.shared.dto.LoginRequest
import com.coffee.shared.dto.LoginResponse
import com.coffee.shared.dto.MenuItemDto
import com.coffee.shared.dto.OrderDto
import com.coffee.shared.dto.OrderItemDto
import com.coffee.shared.dto.RegisterRequest
import com.coffee.shared.dto.TableDto
import com.coffee.shared.dto.UpdateMenuItemRequest
import com.coffee.shared.dto.UserDto
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.*

class CoffeeOrderRepository {

    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return try {
            val response = ApiClient.client.post(ApiClient.url("/api/auth/login")) {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(username, password))
            }
            if (response.status == HttpStatusCode.OK) {
                val loginResponse = response.body<LoginResponse>()
                TokenManager.saveSession(
                    token = loginResponse.token,
                    userId = loginResponse.userId,
                    username = loginResponse.username,
                    displayName = loginResponse.displayName,
                    role = loginResponse.role
                )
                Result.success(loginResponse)
            } else {
                Result.failure(Exception("Login failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAll(): Result<List<MenuItemDto>> = runCatching {
        ApiClient.client.get(ApiClient.url("/api/menu")) {
            withAuth()
        }.body()
    }

    suspend fun addMenuItem(name: String, category: String, price: Int): Result<MenuItemDto> =
        runCatching {
            ApiClient.client.post(ApiClient.url("/api/menu")) {
                withAuth()
                contentType(ContentType.Application.Json)
                setBody(CreateMenuItemRequest(name, category, price))
            }.body()
        }

    suspend fun update(
        id: Long,
        name: String?,
        category: String?,
        price: Int?
    ): Result<MenuItemDto> = runCatching {
        ApiClient.client.put(ApiClient.url("/api/menu/$id")) {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(UpdateMenuItemRequest(name, category, price))
        }.body()
    }

    suspend fun deleteMenuItem(id: Long): Result<Unit> = runCatching {
        ApiClient.client.delete(ApiClient.url("/api/menu/$id")) {
            withAuth()
        }
    }

    /**
     * Upload ảnh cho món ăn dưới dạng multipart/form-data.
     * @param menuItemId ID của món ăn
     * @param imageBytes nội dung ảnh dưới dạng ByteArray
     * @param mimeType loại MIME (ví dụ: "image/jpeg")
     * @param fileName tên file ảnh
     * @return MenuItemDto đã cập nhật imageUrl
     */
    suspend fun uploadImage(
        menuItemId: Long,
        imageBytes: ByteArray,
        mimeType: String,
        fileName: String
    ): Result<MenuItemDto> = runCatching {
        ApiClient.client.post(ApiClient.url("/api/menu/$menuItemId/image")) {
            withAuth()
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("image", imageBytes, Headers.build {
                            append(HttpHeaders.ContentType, mimeType)
                            append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                        })
                    }
                )
            )
        }.body()
    }

    suspend fun createOrUpdateOrder(tableId: Long, items: List<OrderItemDto>): Result<Unit> =
        runCatching {
            ApiClient.client.post(ApiClient.url("/api/orders")) {
                withAuth()
                contentType(ContentType.Application.Json)
                setBody(CreateOrderRequest(tableId, items))
            }
            Unit
        }

    suspend fun getAll(isPaid: Boolean? = null): Result<List<OrderDto>> = runCatching {
        val query = isPaid?.let { "?isPaid=$it" } ?: ""
        ApiClient.client.get(ApiClient.url("/api/orders$query")) {
            withAuth()
        }.body()
    }

    suspend fun pay(orderId: Long): Result<Unit> = runCatching {
        ApiClient.client.put(ApiClient.url("/api/orders/$orderId/pay")) {
            withAuth()
        }
    }

    suspend fun getAllTable(): Result<List<TableDto>> = runCatching {
        ApiClient.client.get(ApiClient.url("/api/tables")) {
            withAuth()
        }.body()
    }

    suspend fun addTable(name: String, maxPeople: Int): Result<TableDto> = runCatching {
        ApiClient.client.post(ApiClient.url("/api/tables")) {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(CreateTableRequest(name, maxPeople))
        }.body()
    }

    suspend fun deleteTable(id: Long): Result<Unit> = runCatching {
        ApiClient.client.delete(ApiClient.url("/api/tables/$id")) {
            withAuth()
        }
    }

    /**
     * Lấy dữ liệu thống kê cho Dashboard.
     */
    suspend fun getDashboardStats(): Result<DashboardStatsDto> = runCatching {
        ApiClient.client.get(ApiClient.url("/api/stats")) {
            withAuth()
        }.body()
    }

    /**
     * Lấy danh sách nhân viên (EMPLOYEE).
     * Chỉ OWNER mới được gọi endpoint này.
     */
    suspend fun getEmployees(): Result<List<UserDto>> = runCatching {
        ApiClient.client.get(ApiClient.url("/api/users")) {
            withAuth()
        }.body()
    }

    /**
     * Tạo tài khoản nhân viên mới.
     * @param username tên đăng nhập
     * @param password mật khẩu
     * @param displayName tên hiển thị
     */
    suspend fun createEmployee(
        username: String,
        password: String,
        displayName: String
    ): Result<Unit> = runCatching {
        val response = ApiClient.client.post(ApiClient.url("/api/auth/register")) {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(username, password, displayName, UserRole.EMPLOYEE))
        }
        if (!response.status.isSuccess()) {
            throw Exception("Tạo tài khoản thất bại: ${response.status}")
        }
    }

    /**
     * Xoá tài khoản nhân viên theo ID.
     */
    suspend fun deleteEmployee(id: Long): Result<Unit> = runCatching {
        val response = ApiClient.client.delete(ApiClient.url("/api/users/$id")) {
            withAuth()
        }
        if (!response.status.isSuccess()) {
            throw Exception("Xoá tài khoản thất bại: ${response.status}")
        }
    }
}
