# Kiến trúc phần mã EmployeeActivity và Fragments

Phần code dành cho nhân viên (`EmployeeActivity`) được xây dựng trên một bộ khung kiến trúc rất mạnh mẽ và vững chắc của Android, kết hợp truyền thống (Activity/XML UI) với các best practices (Luồng Flow / Lifecycle).

Dưới đây là chi tiết cách hệ thống UI này hoạt động từ tầng gốc lên tầng ngọn.

---

## 1. Tầng Host (EmployeeActivity)

`EmployeeActivity` không chứa trực tiếp giao diện các nút bấm, mà nó đóng vai trò là một **Navigation Host (Trạm trung chuyển)**. 
- Nó quản lý một `BottomNavigationView` để di chuyển giữa các màn hình (Quản lý bàn, Lịch sử, Cài đặt).
- **Jetpack Navigation Component**: Điều hướng thông qua một `NavHostFragment`. Mọi fragment con (`OrderFragment`, `ManagementFragment`, ...) đều sống bên trong `EmployeeActivity`.
- **ViewBinding**: Khởi tạo bằng `ActivityMainBinding.inflate(layoutInflater)`. Điều này giúp bắt trực tiếp các views (vd `binding.bottomNav`) mà không sợ NullPointer hay phải ép kiểu `findViewById`.
- Ngoài ra, `EmployeeActivity` còn kết nối một tệp Compose ảo ( `GlobalComposeHandler.GlobalComposeContent()`) để giúp nó có thể "giao tiếp" với các hộp thoại Compose Dialog / BottomSheet được kích hoạt từ ViewModel trên nền View cổ điển.

---

## 2. Kế thừa linh hoạt với `EmployeeBaseFragment<T>`

Thay vì mỗi Screen Fragment phải lặp đi lặp lại code bắt ViewBinding hay ViewModel, dự án dùng một class trừu tượng gốc có tên `EmployeeBaseFragment<T : ViewBinding>`.

Tất cả Fragment dành cho nhân viên đều kế thừa class này.

### Cơ chế hoạt động của ViewBinding:
```kotlin
abstract class EmployeeBaseFragment<T : ViewBinding>(
    private val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> T,
) : Fragment()
```
- Khi định nghĩa các fragment con (Ví dụ `ManagementFragment : EmployeeBaseFragment<FragmentManagementBinding>(FragmentManagementBinding::inflate)`), nó tự động bơm (inject) cơ chế inflate xml layout vào class cha thông qua Lambda.
- Biến **`_binding`** giữ liên kết view. Khi vòng đời của fragment kết thúc qua hàm `onDestroyView()`, `_binding` lập tức được ép về `null` để ngăn tình trạng Rò rỉ bộ nhớ (Memory Leaks - do Fragment bị chết nhưng View con vẫn còn giữ reference).

### Quản lý ViewModel (Chia sẻ dữ liệu)
```kotlin
val appViewModel: AppViewModel by activityViewModels()
```
Nhờ từ khoá `activityViewModels()`, tất cả Fragment mở trong `EmployeeActivity` đều gọi chung **MỘT** bộ nhớ `AppViewModel`. Khi `OrderFragment` thêm món và báo cho ViewModel nó làm thay đổi `Total Price`, lập tức `ManagementFragment` cũng thấy sự thay đổi.

---

## 3. Thu thập dữ liệu (Collect State) & Cập nhật UI

Để đảm bảo hiệu năng, Fragments chỉ được phép thu thập (collect) trạng thái StateFlow khi Fragment đang "hiển thị" (nằm giữa `onStart` và `onStop`), nếu ứng dụng bị thu nhỏ xuống nền, quá trình update UI cần tạm dừng để tránh tốn pin và crash.

### Cơ chế `collectFlow`
File `EmployeeBaseFragment` cung cấp hàm lõi cơ bản này:
```kotlin
protected fun <T> collectFlow(
    flow: Flow<T>,
    action: suspend (T) -> Unit,
) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect { action(it) }
        }
    }
}
```
* **Ý nghĩa**: Bọc cái Flow bên trong `repeatOnLifecycle(STARTED)`. Nếu người dùng tắt màn hình hoặc chuyển tab khác, `Flow` lập tức dừng kéo data (`cancel`). Khi user quay lại tab đó, Flow tự động nối lại `collect`. (Đây là Google Recommended Practice tốt nhất hiện nay).
### Kỹ thuật Tối ưu hóa UI `distinctUntilChanged` (Tránh Re-render thừa)
Trạng thái tổng `AppUiState` chứa rất nhiều trường khác nhau (ví dụ: `isLoading`, `tableInfoList`, `menuItems`, v.v.). Khi gọi `appViewModel.uiState`, nếu có bất kỳ biến nào bên trong thay đổi, nó sẽ phát (emit) lại dữ liệu. Nếu Fragment A không quan tâm tới cập nhật đó, nó vẫn bị trigger hàm update dẫn đến lãng phí CPU hoặc gây chớp giật (Flickering).

Vì vậy, thay vì hứng toàn bộ `AppUiState`, chúng ta sử dụng Flow operators gồm **`map`** và **`distinctUntilChanged`** truyền thẳng vào hàm `collectFlow` như sau:
```kotlin
override fun collectStateAndUpdateUi() {
    // Chỉ hứng sự kiện khi danh sách "menuItems" bị thay đổi 
    // Các biến khác như isLoading, tableInfoList có đổi cũng bị BỎ QUA.
    collectFlow(
        appViewModel.uiState
            .map { it.menuItems }
            .distinctUntilChanged()
    ) { menuItems ->
        // Cập nhật adapter ở đây
        menuAdapter.submitList(menuItems)
    }
}
```

**Chi tiết giải nghĩa:**
* **`map { it.menuItems }`**: Trích xuất ra duy nhất một trường dữ liệu (Property) mà UI này bận tâm. Khiến cho Flow lúc này từ kiểu giá trị `Flow<AppUiState>` thu hẹp lại thành `Flow<List<MenuItem>>`.
* **`distinctUntilChanged()`**: Đóng vai trò là thanh chắn tuyệt đối. Khi State tổng sinh ra bản sao mới, nhưng property `menuItems` này vẫn ĐƯỢC GIỮ NGUYÊN (không thay đổi so với lần trước), thì toán tử này sẽ **Cản lại** không cho block `action` đằng sau thực thi. 
-> **Kết quả**: Luồng thu thập dữ liệu bị cô lập, App duy trì mượt mà và render chính xác một chiều ở những điểm UI nhất định.

### Hai Override Bắt Buộc (Hai hợp đồng lập trình)
Khi một Fragment con kế thừa `EmployeeBaseFragment`, nó buộc phải override 2 hàm để rạch ròi 2 luồng công việc của chuẩn MVI/MVVM:

1. **`setUpEventListeners()`**: Chuyên chứa các logic người dùng tác động như Bấm Nút, Gõ text, Click Item.
    - *UI phát sinh Event -> Đẩy vào ViewModel.*
2. **`collectStateAndUpdateUi()`**: Chuyên chứa các block `collectFlow`.
    - *ViewModel tính toán ra State -> Đẩy lại về Fragment con.*

**Ví dụ thực tế trong OrderFragment:**
```kotlin
override fun collectStateAndUpdateUi() {
    collectFlow(appViewModel.uiState) { state ->
        // Trạng thái Loading spinner
        binding.loadingProgressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        
        // 1. Phân tách danh sách các bàn ra từ state
        val currentTable = state.tableInfoList.find { it.tableId == this.tableId }
        
        // 2. Dùng dữ liệu mới re-render lại UI
        adapter.submitList(currentTable.items)
    }
}
```

**Tổng kết:** `EmployeeActivity` và các Fragment của nó được gắn kết hoàn hảo, không có code tạp, chống thất thoát RAM tốt và hoạt động liền mạch một chiều (One-Way Data Binding) cực kỳ đáng tin cậy.
