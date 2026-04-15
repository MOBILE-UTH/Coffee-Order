# BÁO CÁO TIỂU LUẬN
# Ứng dụng Quản lý Quán Cà phê — Coffee Order

> **Môn học:** Phát triển ứng dụng di động  
> **Công nghệ:** Kotlin · Android · Ktor · Jetpack Compose  
> **Ngày hoàn thành:** 22/04/2026

---

## Mục lục

1. [Giới thiệu](#1-giới-thiệu)
2. [Thiết kế hệ thống](#2-thiết-kế-hệ-thống)
   - 2.1. [Tổng quan cấu trúc dự án](#21-tổng-quan-cấu-trúc-dự-án)
   - 2.2. [Sơ đồ Use Case](#22-sơ-đồ-use-case)
   - 2.3. [Sơ đồ Data Model (ER)](#23-sơ-đồ-data-model-er)
   - 2.4. [Kiến trúc hệ thống](#24-kiến-trúc-hệ-thống)
   - 2.5. [Triển khai code](#25-triển-khai-code)
   - 2.6. [Kết quả — Giao diện ứng dụng](#26-kết-quả--giao-diện-ứng-dụng)
3. [Kết luận](#3-kết-luận)

---

## 1. Giới thiệu

**Coffee Order** là một ứng dụng quản lý quán cà phê toàn diện, được phát triển trên nền tảng **Android** với mục tiêu số hóa toàn bộ quy trình vận hành — từ tiếp nhận khách hàng, đặt món, thanh toán cho đến quản lý doanh thu. Ứng dụng hướng tới hai nhóm người dùng chính:

- **Nhân viên phục vụ (Employee):** Quản lý bàn, tiếp nhận order, xử lý thanh toán.
- **Chủ quán / Quản trị viên (Owner/Admin):** Quản lý menu sản phẩm, quản lý nhân sự, theo dõi thống kê doanh thu.

Ứng dụng được xây dựng theo kiến trúc **Client–Server** hiện đại:
- **Client (Android App):** Sử dụng mô hình **MVVM** với Kotlin, kết hợp **XML Layouts** cho nhân viên và **Jetpack Compose** cho trang quản trị.
- **Server (Ktor Backend):** API RESTful với xác thực **JWT**, sử dụng **JetBrains Exposed** ORM để thao tác cơ sở dữ liệu.
- **Shared Module:** Module dùng chung giữa App và Server, chứa các DTO (Data Transfer Objects) và models, đảm bảo tính nhất quán dữ liệu giữa hai nền tảng.

Điểm nổi bật của dự án là sử dụng **100% Kotlin** trên toàn bộ stack — từ giao diện người dùng, logic nghiệp vụ đến xử lý backend — tạo nên sự đồng nhất và giảm thiểu lỗi chuyển đổi dữ liệu giữa các tầng.

---

## 2. Thiết kế hệ thống

### 2.1. Tổng quan cấu trúc dự án

Dự án được tổ chức dưới dạng **Multi-Module Gradle Project** với 3 module chính:

| Module | Chức năng | Công nghệ chính |
|--------|-----------|------------------|
| `:app` | Ứng dụng Android (client) | Kotlin, MVVM, Compose, XML Fragments |
| `:server` | Backend API server | Ktor, Netty, Exposed ORM, JWT |
| `:shared` | Dữ liệu dùng chung (DTOs) | Kotlin Data Classes, Serialization |

#### Chi tiết từng module:

**Module `:shared`** — Trái tim kết nối:
- `AuthDto.kt` — LoginRequest, LoginResponse, RegisterRequest, UserDto
- `MenuDto.kt` — MenuItemDto, CreateMenuItemRequest, UpdateMenuItemRequest
- `TableDto.kt` — TableDto, CreateTableRequest, ActiveOrderDto, OrderItemDto
- `Models.kt` — UserRole (OWNER/EMPLOYEE), ApiError, ApiMessage

**Module `:server`** — Backend API:
- **Database:** `DatabaseFactory` (khởi tạo + seed data), `DatabaseTables` (5 bảng)
- **Authentication:** `JwtConfig` (tạo token), `RouteAuth` (authorized, ownerOnly)
- **Routes:** AuthRoutes, MenuRoutes, OrderRoutes, TableRoutes, UserRoutes, StatRoutes, HealthRoutes
- **Repositories:** UserRepository, MenuRepository, TableRepository, OrderRepository, StatRepository

**Module `:app`** — Android Client:
- **Auth:** LoginActivity (đăng nhập với JWT)
- **Employee Flow:** EmployeeActivity → ManagementFragment, OrderFragment, HistoryFragment, SettingFragment
- **Admin Flow:** AdminActivity → AdminDashboardScreen, AdminMenuScreen, AdminStaffScreen, AdminHistoryScreen
- **Network:** ApiClient (Ktor-Client), TokenManager (JWT session management)
- **ViewModel:** AppViewModel (single source of truth, StateFlow)

---

### 2.2. Sơ đồ Use Case

#### 2.2.1. Sơ đồ Use Case tổng quan

Sơ đồ dưới đây mô tả toàn bộ các trường hợp sử dụng của hệ thống, phân theo vai trò người dùng. Admin kế thừa tất cả quyền của nhân viên và bổ sung thêm quyền quản trị.

![Sơ đồ Use Case tổng quan](diagrams/usecase_overall.png)

> **Nguồn PlantUML:** [`diagrams/usecase_overall.puml`](diagrams/usecase_overall.puml)

**Tóm tắt quyền theo vai trò:**

| Chức năng | Nhân viên | Chủ quán |
|-----------|:---------:|:--------:|
| Đăng nhập / Đăng xuất | ✅ | ✅ |
| Cấu hình Server URL | ✅ | ✅ |
| Quản lý bàn (xem, thêm) | ✅ | ✅ |
| Đặt hàng & Thanh toán | ✅ | ✅ |
| Xem lịch sử đơn hàng | ✅ | ✅ |
| Quản lý menu (CRUD) | ❌ | ✅ |
| Quản lý nhân sự | ❌ | ✅ |
| Xem thống kê doanh thu | ❌ | ✅ |

---

#### 2.2.2. Use Case — Xác thực & Phân quyền

Chi tiết luồng xác thực từ khi nhập thông tin đến khi điều hướng vào trang chính:

![Use Case Xác thực](diagrams/usecase_auth.png)

> **Nguồn PlantUML:** [`diagrams/usecase_auth.puml`](diagrams/usecase_auth.puml)

**Luồng xử lý:**
1. Nhân viên nhập Server URL, username/password
2. App gửi `POST /api/auth/login` đến Ktor Server
3. Server xác minh mật khẩu bằng **BCrypt**
4. Tạo **JWT Token** chứa userId, username, role
5. App lưu token vào **SharedPreferences**
6. Kiểm tra role → điều hướng đến **EmployeeActivity** hoặc **AdminActivity**

---

#### 2.2.3. Use Case — Quy trình Đặt hàng & Thanh toán

Chi tiết luồng từ chọn bàn đến thanh toán hoàn tất:

![Use Case Đặt hàng](diagrams/usecase_order.png)

> **Nguồn PlantUML:** [`diagrams/usecase_order.puml`](diagrams/usecase_order.puml)

**Luồng xử lý:**
1. Nhân viên xem danh sách bàn (grid view) → chọn bàn
2. Mở **OrderFragment** → nhấn "Add Items" để mở menu (BottomSheet)
3. Chọn món từ menu → thêm vào giỏ hàng (Cart)
4. Có thể tăng/giảm số lượng hoặc xóa món
5. Nhấn "Submit" → `POST /api/orders` gửi đơn lên server
6. Nhấn "Payment" → xác nhận → `PUT /api/orders/{id}/pay` xử lý thanh toán
7. Hệ thống tính **VAT 10%** và cập nhật trạng thái bàn

---

#### 2.2.4. Use Case — Quản trị viên (Admin)

Các chức năng chỉ dành cho chủ quán (role = OWNER):

![Use Case Admin](diagrams/usecase_admin.png)

> **Nguồn PlantUML:** [`diagrams/usecase_admin.puml`](diagrams/usecase_admin.puml)

**3 nhóm chức năng chính:**

1. **Quản lý Menu:** Xem/Thêm/Sửa/Xóa sản phẩm, upload hình ảnh
2. **Quản lý Nhân sự:** Xem danh sách, tạo tài khoản, xóa nhân viên
3. **Thống kê:** Doanh thu hôm nay, tổng đơn hàng, top 5 sản phẩm bán chạy

---

### 2.3. Sơ đồ Data Model (ER)

Cơ sở dữ liệu được thiết kế với 5 bảng chính, sử dụng JetBrains Exposed ORM:

![Sơ đồ ER](diagrams/er_diagram.png)

> **Nguồn PlantUML:** [`diagrams/er_diagram.puml`](diagrams/er_diagram.puml)

**Chi tiết các bảng:**

#### Bảng `Users`
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| id | Long (PK) | Khóa chính, tự tăng |
| username | Varchar(50) | Tên đăng nhập, unique |
| password_hash | Varchar(255) | Mật khẩu mã hóa BCrypt |
| display_name | Varchar(100) | Tên hiển thị |
| role | Varchar(20) | "OWNER" hoặc "EMPLOYEE" |

#### Bảng `MenuItems`
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| id | Long (PK) | Khóa chính |
| name | Varchar(100) | Tên sản phẩm |
| category | Varchar(50) | Danh mục (Coffee, Tea, ...) |
| price | Integer | Giá (VNĐ) |
| image_url | Varchar(255)? | URL hình ảnh (nullable) |

#### Bảng `CoffeeTables`
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| id | Long (PK) | Khóa chính |
| name | Varchar(100) | Tên bàn |
| max_people | Integer | Sức chứa tối đa |

#### Bảng `Orders`
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| id | Long (PK) | Khóa chính |
| table_id | Long (FK) | Liên kết đến CoffeeTables |
| staff_id | Long (FK) | Liên kết đến Users |
| total_price | Double | Tổng tiền (bao gồm VAT) |
| payment_method | Varchar(20) | "CASH" hoặc "TRANSFER" |
| is_paid | Boolean | Trạng thái thanh toán |
| created_at | Varchar(30) | Thời gian tạo |

#### Bảng `OrderLineItems` (Composite PK)
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| order_id | Long (PK, FK) | Liên kết đến Orders |
| menu_item_id | Long (PK, FK) | Liên kết đến MenuItems |
| quantity | Integer | Số lượng |
| unit_price | Integer | Đơn giá tại thời điểm đặt |

**Quan hệ:**
- `Users` → `Orders`: Một nhân viên tạo nhiều đơn hàng **(1:N)**
- `CoffeeTables` → `Orders`: Một bàn có nhiều đơn hàng **(1:N)**
- `Orders` → `OrderLineItems`: Một đơn hàng chứa nhiều chi tiết **(1:N)**
- `MenuItems` → `OrderLineItems`: Một sản phẩm xuất hiện trong nhiều chi tiết **(1:N)**

---

### 2.4. Kiến trúc hệ thống

Hệ thống tuân theo kiến trúc **Layered Architecture** với sự phân tách rõ ràng:

![Kiến trúc hệ thống](diagrams/architecture.png)

> **Nguồn PlantUML:** [`diagrams/architecture.puml`](diagrams/architecture.puml)

| Tầng | Thành phần | Chức năng |
|------|-----------|-----------|
| **UI Layer** | Compose / XML UI | Hiển thị giao diện, nhận tương tác người dùng |
| **ViewModel Layer** | AppViewModel + StateFlow | Quản lý trạng thái UI, xử lý logic hiển thị |
| **Network Layer** | ApiClient (Ktor-Client) | Giao tiếp REST API với server, gắn JWT token |
| **Plugins Layer** | JWT, CORS, StatusPages | Xác thực, bảo mật, xử lý ngoại lệ |
| **Routes Layer** | Auth, Menu, Order, Table, User, Stat | Định tuyến API endpoints |
| **Repository Layer** | Repository Pattern | Truy vấn nghiệp vụ, thao tác dữ liệu |
| **Data Layer** | Exposed DAO | Mapping ORM, tương tác trực tiếp với DB |

**Luồng dữ liệu:**
1. **User tương tác** → UI gửi Event đến ViewModel
2. **ViewModel** xử lý logic → gọi ApiClient
3. **ApiClient** gửi HTTP request (bearer JWT) → Ktor Server
4. **Server** xác thực JWT → route đến handler tương ứng
5. **Handler** gọi Repository → Repository thực thi truy vấn qua Exposed
6. Kết quả truyền ngược về UI qua **StateFlow**

---

### 2.5. Triển khai code

#### 2.5.1. Authentication — Xác thực JWT

Server sử dụng JWT (JSON Web Token) với BCrypt hashing:

```kotlin
// server: JwtConfig.kt — Tạo token
object JwtConfig {
    fun makeToken(userId: Long, username: String, 
                  displayName: String, role: String): String =
        JWT.create()
            .withClaim("userId", userId)
            .withClaim("username", username)
            .withClaim("role", role)
            .withExpiresAt(Date(System.currentTimeMillis() + 86400000)) // 24h
            .sign(Algorithm.HMAC256(secret))
}
```

```kotlin
// server: AuthRoutes.kt — Xác minh đăng nhập
post("/login") {
    val request = call.receive<LoginRequest>()
    val user = userRepository.findByUsername(request.username)
    val verified = BCrypt.verifyer()
        .verify(request.password.toCharArray(), user.passwordHash).verified
    
    if (verified) {
        val token = JwtConfig.makeToken(user.id, user.username, ...)
        call.respond(LoginResponse(token = token, role = UserRole.valueOf(user.role)))
    }
}
```

```kotlin
// app: TokenManager.kt — Quản lý session
object TokenManager {
    fun saveSession(token: String, displayName: String, role: String) {
        prefs.edit()
            .putString("jwt_token", token)
            .putString("display_name", displayName)
            .putString("role", role)
            .apply()
    }
    fun isOwner(): Boolean = getRole() == "OWNER"
}
```

#### 2.5.2. ViewModel — Single Source of Truth

`AppViewModel` quản lý toàn bộ trạng thái UI với Kotlin Coroutines:

```kotlin
// app: AppViewModel.kt
class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    fun submitOrder(tableId: Long, items: Map<Long, Int>, onSuccess: () -> Unit) {
        launchWithLoading {
            val orderItems = items.map { (menuItemId, quantity) -> 
                OrderItemDto(menuItemId, quantity) 
            }
            coffeeOrderRepository.createOrUpdateOrder(tableId, orderItems)
                .onSuccess { fetchTables(force = true); onSuccess() }
                .onFailure { _uiState.update { it.copy(errorMessage = it.errorMessage) } }
        }
    }
}
```

#### 2.5.3. Route Protection — Phân quyền

Server bảo vệ API bằng middleware phân quyền theo vai trò:

```kotlin
// server: Routing.kt — Protected routes
routing {
    // Public routes (không cần JWT)
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

// server: RouteAuth.kt — Owner-only decorator
fun Route.ownerOnly(build: Route.() -> Unit) {
    authorized(UserRole.OWNER, build)
}
```

#### 2.5.4. Repository Pattern — Tầng truy cập dữ liệu

Mỗi entity có Repository riêng với Exposed ORM:

```kotlin
// server: MenuRepository.kt
class MenuRepository {
    suspend fun getAll(): List<MenuItemDto> = dbQuery {
        MenuItems.selectAll().map { toDto(it) }
    }
    
    suspend fun create(request: CreateMenuItemRequest): MenuItemDto = dbQuery {
        val id = MenuItems.insertAndGetId {
            it[name] = request.name
            it[category] = request.category
            it[price] = request.price
        }
        getById(id.value)!!
    }
}
```

---

### 2.6. Kết quả — Giao diện ứng dụng

Dưới đây là giao diện chính của ứng dụng:

![Giao diện Coffee Order](coffee_app_screens.png)

**Mô tả từng màn hình:**

| Màn hình | Mô tả |
|----------|-------|
| **Đăng nhập** | Giao diện glass-morphism gradient nâu. Input fields: Server URL, Username, Password. Hỗ trợ auto-fill credentials, toggle hiển thị mật khẩu. |
| **Quản lý Bàn** | Grid view hiển thị trạng thái bàn (trống/đang dùng). Nút "Thêm Bàn" với BottomSheet form. Mỗi bàn hiển thị tên, sức chứa, và order hiện tại nếu có. |
| **Chi tiết Đơn hàng** | Header: tên bàn. Body: danh sách món đã order + số lượng + đơn giá. Summary: Subtotal, VAT 10%, Total Due. Actions: Add Items (green), Delete Cart (red), Payment (green). |
| **Bảng điều khiển Admin** | Material 3 Compose UI. Gradient stat cards (doanh thu, đơn hàng). Top 5 sản phẩm bán chạy với ảnh. Bottom NavBar: Thống kê, Sản phẩm, Nhân sự, Lịch sử, Đăng xuất. |

---

## 3. Kết luận

### 3.1. Kết quả đạt được

Ứng dụng **Coffee Order** đã hoàn thành đầy đủ các chức năng cốt lõi cho việc vận hành một quán cà phê:

- ✅ **Xác thực đa vai trò** — Phân quyền rõ ràng giữa nhân viên và quản trị viên (JWT + BCrypt)
- ✅ **Quản lý bàn** — Thêm bàn, xem trạng thái bàn theo thời gian thực
- ✅ **Đặt hàng & Thanh toán** — Luồng order hoàn chỉnh với giỏ hàng, tính thuế VAT 10%
- ✅ **Quản lý menu** — CRUD sản phẩm với hỗ trợ upload hình ảnh
- ✅ **Quản lý nhân sự** — Tạo/xóa tài khoản nhân viên (Owner only)
- ✅ **Thống kê kinh doanh** — Dashboard doanh thu, đơn hàng, top sản phẩm

### 3.2. Điểm mạnh kỹ thuật

| Tiêu chí | Đánh giá |
|----------|----------|
| **Đồng nhất công nghệ** | 100% Kotlin trên toàn bộ stack (App + Server + Shared) |
| **Type Safety** | Module `:shared` đảm bảo DTO nhất quán giữa client và server |
| **Kiến trúc rõ ràng** | MVVM + Repository Pattern + Clean layered architecture |
| **UI hiện đại** | Kết hợp XML Layouts (Employee) và Jetpack Compose (Admin) |
| **Bảo mật** | JWT authentication + BCrypt password hashing + Role-based access control |
| **Tái sử dụng** | `EmployeeBaseFragment` base class, `AppViewModel` shared state management |
| **API Design** | RESTful routes với StatusPages error handling và CORS support |

### 3.3. Hướng phát triển

- **Real-time sync** — Tích hợp WebSocket để cập nhật trạng thái bàn theo thời gian thực giữa các thiết bị
- **Offline mode** — Cache dữ liệu menu cục bộ bằng Room Database để hoạt động khi mất kết nối
- **Báo cáo nâng cao** — Biểu đồ doanh thu theo tuần/tháng, export PDF, so sánh hiệu suất
- **Multi-platform** — Mở rộng module `:shared` sang Kotlin Multiplatform (KMP) cho iOS
- **Image CDN** — Tích hợp dịch vụ lưu trữ ảnh đám mây thay vì local storage

---

> *Tất cả sơ đồ trong báo cáo được tạo bằng PlantUML. File nguồn `.puml` nằm trong thư mục `diagrams/`.*  
> *Báo cáo được tạo với sự hỗ trợ phân tích code từ công cụ `code-review-graph`.*
