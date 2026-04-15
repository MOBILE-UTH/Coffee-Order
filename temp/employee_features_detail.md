# Phân Tích Logic Chức Năng Các Fragment (Employee Flow)

Nhánh giao diện dành cho Nhân viên phục vụ (`EmployeeActivity`) bao gồm 4 tính năng lõi. Mỗi tính năng được module hoá vào một thư mục riêng đặt trong `app/src/main/java/com/coffee/order/fragment/`.

Dưới đây là giải thích luồng code chi tiết của các Fragment và các Class phụ trợ (Adapter, Dialog) đi kèm.

---

## 1. Tính năng Quản lý Bàn (`/management`)

Chức năng đầu tiên khi Nhân viên mở màn hình. Hiển thị danh sách bàn và trạng thái trống/đầy.

### `ManagementFragment.kt`
- **Vai trò**: Điểm bắt đầu (Start Destination) của NavGraph. Lắng nghe `appViewModel.uiState.map { it.tableInfoList }` để vẽ danh sách bàn.
- **Tiện ích Thêm bàn**: Cho phép bất kỳ Nhân viên nào cũng có thể linh hoạt bấm dấu Cộng `+` để thêm bàn phụ khi khách đông thông qua `AddTableBottomSheetDialogFragment`.
- **Điều hướng**: Gắn `onTableClick` khởi tạo một bó dữ liệu xuyên không (Bundle) chứa `tableId` và kích hoạt lệnh chuyển màn hình sang `OrderFragment` để thao tác cụ thể lên cái bàn đó.

### `TableGridAdapter.kt`
- **Vai trò**: Cầu nối nội dung hiển thị Bàn. Kỹ thuật ở đây rất hay vì nó sử dụng đa giao diện (**Multiple ViewTypes**).
- Thay vì dùng 1 file giao diện XML duy nhất rồi ẩn/hiện view chạy bằng lệnh `if/else`, lập trình viên đã viết hàm `getItemViewType` để bóc tách trạng thái `TableInfo.Status.EMPTY` chạy vào `LayoutEmptyTableBinding` và `OCCUPIED` chạy `LayoutOccupiedTableBinding`. Giúp code an toàn không bao giờ nhầm lẫn biến giao diện.

### `AddTableBottomSheetDialogFragment.kt`
- **Vai trò**: Một Pop-up Dialog trượt từ dưới lên (Dành cho chức năng Thêm Bàn). Được kích hoạt độc lập khi Owner bấm dấu cộng.

---

## 2. Tính năng Lên Đơn / Thanh Toán (`/order`)

Màn hình chức năng đồ sộ nhất và quan trọng nhất của App.

### `OrderFragment.kt`
- **Vai trò**: Cung cấp giao diện thanh toán độc lập nội bộ cho tính năng (Một Fragment ứng với một Bàn).
- **Cart State Machine (Lớp `Cart` nhúng bên trong)**: 
  - Khác biệt so với các Fragment khác phụ thuộc 100% vào ViewModel, `OrderFragment` xây dựng một lớp tĩnh nội bộ là `class Cart()`.
  - Class này tạo ra biến `_items = MutableStateFlow` dạng Key-Value (Map) để lưu số lượng *Id_Món_Ăn -> Số_Lượng*. Nó thao tác hoàn toàn dưới RAM (optimistic UI), tăng tốc độ thêm/bớt món phản hồi cực nhanh mà không cần gửi chờ Server lưu.
- **Tính năng mở rộng độ cao List (`expandListHeight`)**: Kỹ thuật can thiệp LayoutParams ép Height RecyclerView thủ công, dùng khi app sử dụng ListView kiểu cũ nằm lồng trong ScrollView (chống bug mất scroll hiển thị màn hình nhỏ).
- **Luồng Payment Pipeline (`processPayment()`)**: Dùng kỹ thuật gọi Async kép: Gọi `appViewModel.submitOrder` (lưu hoá đơn vào DB) ngay trong block call-back sẽ móc nối tiếp hàm `appViewModel.payOrder` (gạt tick chuyển sang Hoá đơn Lịch sử) để đảm bảo đồng bộ hoàn hảo mọi rủi ro thao tác sai.

### `ActiveOrderListAdapter.kt`
- **Vai trò**: Chỉ adapter danh sách chữ thông thường hiển thị trực tiếp danh sách rổ món ăn đang order ngay trên chính cái bảng tóm tắt màn hình Giỏ Hàng.

---

## 3. Tính năng Xem Lịch Sử (`/history`)

Tab theo dõi lại thông tin.

### `HistoryFragment.kt`
- **Vai trò**: Cơ bản, hứng `appViewModel.uiState.map { it.historyOrders }`.
- Nó tích hợp xử lý thông minh `historyEmptyTextView.visibility = if (it.isEmpty()) View.VISIBLE` để hiện chữ "Chưa có đơn hàng nào trong ngày" khi List trống, để bù lấp giao diện tránh khoảng trắng khó hiểu cho User.

### `HistoryAdapter.kt`
- **Vai trò**: Liên kết giao diện `LayoutOrderSummaryItemBinding`.
- Điểm thú vị là Adapter này có nhúng sẵn Object tĩnh `currencyFormatter: NumberFormat`. Chuyển đổi một số thực (Double) `25000.0` thành chuỗi hiển thị đúng nguyên lý địa phương như `25.000 ₫` thông qua Locate `vi-VN`.

---

## 4. Tính năng Cài đặt Cá Nhân (`/setting`)

Tab cài đặt và tổng quan.

### `SettingFragment.kt`
- **Vai trò**: Hiện tên và chức năng của người đăng nhập thông qua bộ nhớ tĩnh bằng lệnh `TokenManager.getDisplayName()`.
- Nút đăng xuất (Logout) sẽ xoá token và kích hoạt Hàm ép văng chuyển sang màn hình Login bắt buộc.
- Chứa sẵn một `MenuAdapter` dùng để hiển thị Danh sách menu các đồ uống quán đang bán (Chế độ Read-only) để nhân viên nhẩm lại tên món học việc.

### `MenuAdapter.kt`
- **Vai trò**: Đóng danh sách Menu. Dùng thư viện bên thứ 3 (Glide hoặc Coil được code ở ViewModel) nạp hình ảnh lên để UI nhìn sinh động đẹp mắt. Sử dụng toán tử ListAdapter và DiffCallback để chỉ update duy nhất cái ảnh/tên của món có thay đổi chứ không load lại nguyên danh sách.
