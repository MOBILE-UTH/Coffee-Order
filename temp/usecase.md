# Biểu Đồ Usecase Hệ Thống Coffee Order

Biểu đồ này biểu diễn các tác nhân (Actor) tham gia vào hệ thống và các chức năng (Use Case) cụ thể mà mỗi tác nhân được phép thao tác. Hệ thống được chia cho 2 đối tượng người dùng chính:
1. **Employee (Nhân viên)**: Nhân viên phục vụ khách tại bàn.
2. **Owner (Quản lý/Chủ quán)**: Mang đầy đủ quyền hạn của nhân viên và bổ sung chức năng quản lý, thiết lập hệ thống và báo cáo.

👉 **[Xem Sơ Đồ Usecase (Usecase Diagram)](diagrams/usecase.puml)**

## Các Use Case Chính:

### 1. Phân quyền chung (Nhân viên)
- **Đăng nhập**: User nhập username/mật khẩu, hệ thống sinh ra một chuỗi JWT để xác thực các request kế tiếp.
- **Quản lý Đơn hàng & Bàn**: Có thể xem danh sách bàn. Tại một bàn, nhân viên thêm món vào giỏ (cart), điều chỉnh số lượng và gửi yêu cầu tạo/cập nhật đơn hàng về Server.
- **Thanh toán đơn hàng**: Đánh dấu một bàn đã thanh toán.
- **Xem lịch sử**: Xem lại các hóa đơn và các món đã bán trong quá khứ.

### 2. Phân quyền bổ sung (Quản lý - Owner)
Kế thừa toàn quyền của Nhân viên cộng thêm:
- **Quản lý Bàn**: Định nghĩa các danh mục bàn, số người tối đa, layout chỗ ngồi mới.
- **Quản lý Menu**: Thêm, sửa, xóa các món ăn thức uống, tùy chỉnh giá bán và cập nhật hình ảnh của sản phẩm.
- **Quản lý Nhân Viên**: Tạo tài khoản cho nhân viên mới và xóa tài khoản của nhân viên đã nghỉ.
- **Xem Báo Cáo Thống Kê**: Thấy được doanh thu, số lượng hóa đơn, và danh sách top món ăn bán chạy nhất trên Dashboard (Home).
