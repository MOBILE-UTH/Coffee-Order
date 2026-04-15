# Phân Tích Luồng Nghiệp Vụ Chuyên Sâu (Flow Diagram)

Ứng dụng Coffee-Order có nhiều chức năng thiết bị, nhưng cốt lõi nhất luôn là hành trình trực tiếp tương tác Mở Bàn - Gọi Món - Thanh Toán tại quầy.

👉 **[Xem Sơ Đồ Hoạt Động (Activity Diagram)](diagrams/activity.puml)**

---

## Giải Nghĩa Chi Tiết Luồng Đặt Món (Order Core Flow)

Dưới đây diễn giải quy trình và trạng thái hệ thống trải qua ở từng thao tác hành động trên Activity Flow.

### Bước 1: Khởi Tạo Trạng Thái và Giỏ Hàng (Cart Initialization/Status check)
- Tác nhân: Nhân viên đứng ở sảnh chính (Danh sách Bàn). 
- View kiểm tra mảng DTOs (Danh sách Table). 
  - Nếu `orderItems` của bàn trống: bàn sẽ ở trạng thái **Trống (Empty)**, UI hiện màu Xám nhạt/Trắng. 
  - Nếu `orderItems` chứa items: bàn ở trạng thái **Đang có khách (Occupied)**, UI hiện màu Highlight.
- Nếu nhân viên bấm vào một bàn Trống, hệ thống khởi tạo Giỏ Hàng (Cart Virtual) nội bộ ở RAM hoàn toàn trống trịnh trạng.
- Nếu chọn bàn Đang Phục Vụ, Cart khởi tạo kèm nạp danh sách các món hiện có của khách vào RAM để tiện chỉnh sửa tăng giảm.

### Bước 2: Tương Tác Giỏ Hàng Nội Bộ (Cart Manipulation via Optimistic UI)
- Nhân viên thao tác thêm/bớt, chỉnh số lượng các món.
- Ở giai đoạn này: Hành vi này **KHÔNG** hề làm thay đổi hay bắn request liên hồi về Database. Nó được giữ nguyên dưới dạng Local State (Trạng thái cục bộ ở App Client). Nhằm mang đến độ phản hồi tương tác UI tức thì mà không cần mạng mạnh (Optimistic rendering).

### Bước 3: Xác Nhận Đơn Hàng / Đồng Bộ Cục Bộ (Order Synchronization)
- Khách chốt món. Nhân viên bấm **Xác Nhận (Submit Order)**.
- Client gom các item đang nằm ở Cart xây nên một Payload API (DTO: `CreateOrderRequest`) cực kỳ tinh gọn.
  ```json
  {
    "tableId": 1,
    "items": [{"menuItemId": 5, "quantity": 2}, {"menuItemId": 3, "quantity": 1}]
  }
  ```
- **Hành động xử lý phía Server**: Ktor Server gọi route Order.
  - Check xem bàn ID 1 này đã có hóa đơn nào mang flag `is_paid = false` chưa?
  - **Nhánh A (Bàn lần đầu gọi món - Empty -> Occupied)**: Insert mới hóa đơn vào bảng `orders`.
  - **Nhánh B (Khách tại bàn cũ gọi thêm món)**: Kéo hóa đơn cũ lên để Modify.
  - Server xóa các dòng dữ liệu ở `order_line_items` và tiến hành INSERT BATCH loạt dòng Line Items mới (dựa trên danh sách JSON nhận về) vào DB, tiếp tục chạy code để Update `totalPrice` = sum(quantity * unit_price). 
- Database Commit. Trả về cho Client => UI Client thoát màn hình cập nhật UI bàn thành Màu Occupied.

### Bước 4: Thanh Toán Trả Phòng (Checkout / Release Execute)
- Khi khách yêu cầu tính tiền và rời khỏi quán.
- Nhân viên vào màn hình Đơn bàn đó. Bấm **Thanh toán (Pay Order)**.
- Client gọi HTTP PUT `/api/orders/{id}/pay`.
- Tại Server Base (Backend), record Order được gán biến số `is_paid = true`. 
- Logic thanh toán này sẽ vô hiệu hóa việc truy xuất đơn hàng lên màn hình Live Table. Đồng thời biến toàn bộ Invoice (Hóa đơn) hóa thành tệp tĩnh "Chỉ Đọc" Read-only.
- Bàn không còn bị phụ thuộc bởi hóa đơn `is_paid=false` nào cả, nên API tự động báo Bàn trở với trạng thái trống ban đầu (**Empty**), và đón chu kỳ quy trình khách mới.
