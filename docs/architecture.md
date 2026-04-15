# Tài Liệu Kiến Trúc & Dữ Liệu: Dự án Coffee-Order

Dự án **Coffee-Order** là một hệ thống quản lý quán cà phê toàn diện, ứng dụng mô hình **Client-Server** với sự đồng nhất ngôn ngữ Kotlin ở cả Front-end (Android) và Back-end (Ktor).

---

## 1. Chi Tiết Kiến Trúc Hệ Thống (Architecture Model)

Hệ thống được chia thành 3 cấu phần (module) chính để đảm bảo tính phân tách và khả năng tái sử dụng (Separation of Concerns).

👉 **[Xem Sơ Đồ Kiến Trúc Lớp (Architecture Diagram)](diagrams/architecture.puml)**

### A. Android Client (`/app`)
Ứng dụng Android (Client) chạy phía người dùng (Nhân viên, Quản lý). Áp dụng mô hình thiết kế **MVVM (Model-View-ViewModel)** với **Unidirectional Data Flow (Luồng dữ liệu một chiều)**:
- **UI Layer (View)**: Xây dựng bằng **Jetpack Compose** đối với các luồng Admin và kết hợp **Fragments / ViewBinding** đối với giao diện đặt món. UI đóng vai trò "câm" (Dumb component), chỉ hiển thị dữ liệu nhận được và phát ra Event do user thao tác.
- **ViewModel (`AppViewModel`)**: Giữ UI State ("Nguồn chân lý" - Source of Truth) bằng `StateFlow<AppUiState>`. Khi nhận sự kiện từ UI, ViewModel tự thay đổi cờ hiệu state (VD: `isLoading = true`), gọi Repository, đợi phản hồi rồi update dữ liệu. Lúc đó UI tự động re-render.
- **Repository (`CoffeeOrderRepository`)**: Gọi các Rest API bằng **Ktor Client**. Xử lý việc gắn chèn JWT Token bảo mật vào Request HTTP thông qua lớp `TokenManager`.
- **Image Caching**: Sử dụng **Coil / Glide** cho tác vụ nạp (load) ảnh từ URL, để tối ưu hiệu năng băng thông và bộ nhớ thiết bị.

👉 **[Xem Sơ Đồ Shared Module Diagram](diagrams/shared_module.puml)**

### B. Backend Server (`/server`)
Ứng dụng backend làm nhiệm vụ điều phối và lưu trữ, chạy trên nền **Ktor framework**:
- **Application Engine**: Chạy qua Embedded Server **Netty**, xử lý các I/O connections siêu tốc độ.
- **Plugin System**: 
  - `Authentication` định tuyến qua JWT, kiểm tra quyền truy cập ở các Routes nhạy cảm.
  - `ContentNegotiation` dùng Kotlinx Serialization tự động parse/encode Object sang chuỗi JSON và ngược lại.
- **Routing Layer**: Nơi định nghĩa các Endpoints (`OrderRoutes`, `MenuRoutes`...). Xử lý kiểm tra body và HTTP Error code. Lấy UserId qua Token Principal.
- **Service/Repository Layer**: Xử lý logic nghiệp vụ và tương tác query trực tiếp với ORM **Exposed** để lấy dữ liệu từ cơ sở dữ liệu **H2 DB**.
- **Security**: Cơ chế tạo password dùng thuật toán Hash **Bcrypt**.

### C. Shared Module (`/shared`)
Kho chứa các **DTOs (Data Transfer Objects)**. Vì cả Frontend và Backend đều viết bằng Kotlin, module này mang lại những ưu điểm cực lớn:
- Tránh trùng lặp code và tiết kiệm thời gian, không cần thiết lập công cụ sinh mã tự động (Swagger/OpenAPI code-gen).
- Đảm bảo Type-Safe tuyệt đối: Backend thay đổi trường dữ liệu thì Frontend lập tức báo lỗi thời gian dịch (Compile-time).

---

## 2. Chi Tiết Lược Đồ Cơ Sở Dữ Liệu (Data Model)

Mô hình dữ liệu (Database Schema) được thiết kế tối ưu với một lượng các bảng (tables) rất ít nhưng bao hàm trọn nghiệp vụ Order.

👉 **[Xem Sơ Đồ Cơ Sở Dữ Liệu (ERD Diagram)](diagrams/database_erd.puml)**

**Bối cảnh logic các bảng dữ liệu:**
1. **`users` (Tài khoản User)**: Chứa thông tin đăng nhập (`username`, `password_hash`) và thuộc tính quyền `role` (OWNER cho Admin và EMPLOYEE cho nhân viên).
2. **`coffee_tables` (Bàn)**: Chứa danh sách các bàn vật lý trong quán (tên bàn, sức chứa `max_people`).
3. **`menu_items` (Thực đơn món)**: Thông tin món ăn, đồ uống, kèm mức giá (`price`) và đường dẫn ảnh (`image_url`).
4. **`orders` (Đơn đặt hàng)**: Thực thể trung tâm để lưu phiên giao dịch. 
   - Có khóa ngoại liên kết tới **Bàn** (`table_id`) và **Nhân viên lập bill** (`staff_id`).
   - Cột **`is_paid`**: Cột quyết định vòng đời hoá đơn. Trạng thái `is_paid = false` cho biết đây là Active Order rập theo bàn. Khi `is_paid = true`, hóa đơn này thành bill đã thanh toán, được tính vào doanh thu.
5. **`order_line_items` (Chi tiết của đơn hàng)**: Bảng Mapping N-N. Ghi nhận chi tiết trong một `order_id` sẽ chứa các `menu_item_id` gì, với **Số lượng (`quantity`)** là bao nhiêu. Cột **`unit_price`** cố định mức giá lúc Order nhằm duy trì tính chính xác của hóa đơn lịch sử cả khi giá Menu tăng/giảm ở tương lai.

---

## 3. Chi Tiết Tuần Tự Gọi API (Sequence Diagram)

Sơ đồ tuần tự minh họa cách một luồng tín hiệu (Request & Response) đi qua các tần số giao tiếp của các Lớp (Layers).

👉 **[Xem Sơ Đồ Tuần Tự (Sequence Diagram)](diagrams/sequence_flow.puml)**

**Giải phẫu một luồng Fetch Table (Lấy trạng thái các Bàn) chi tiết:**
1. **Trigger sự kiện**: UI (Ví dụ một màn hình Fragment) cần load dữ liệu nền -> Gọi hàm `fetchTables()` ở ViewModel.
2. **Xử lý State Loading**: `AppViewModel` kích cờ `isLoading = true` bên trong `AppUiState`. Lập tức UI nhận tín hiệu và hiển thị vòng xoay đang Load.
3. **Thực thi gọi Mạng**: ViewModel chạy function trong luồng nền Coroutine: gọi vô `CoffeeOrderRepository.getAllTable()`.
4. **Chuẩn bị Request**: Repository dùng Ktor Client dựng HTTP GET Request (`/api/tables`). Kèm Header `Authorization: Bearer <Token_JWT>`.
5. **Tiếp nhận Backend**: Ktor Server chặn Request qua lớp Middleware JWT Plugin -> Giải mã token lấy User ID -> Hợp lệ -> Định tuyến qua `TableRoutes`.
6. **Query Database**: `TableRepository` ở Server tiến hành query SQL DSL Exposed: SELECT lấy tất cả bàn, sau đó SELECT các order có `is_paid = false` để map các bàn nào đang được order.
7. **Trả Response**: List các model Object được Server serialize về Array JSON trả qua HTTP 200.
8. **Deserialization**: Ktor Client nhận String JSON, phân rã (Parse) ngược lại thành List Model trong Kotlin App. 
9. **Render kết quả**: ViewModel cập nhật dữ liệu vào `AppUiState` (bàn nào trống bàn nào đầy), gạt cờ Loading xuống `false`. Interface Recomposes thành công trả hiển thị cho User.
