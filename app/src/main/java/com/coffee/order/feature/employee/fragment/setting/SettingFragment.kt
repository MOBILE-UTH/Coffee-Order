package com.coffee.order.feature.employee.fragment.setting

import androidx.recyclerview.widget.LinearLayoutManager
import com.coffee.order.base.EmployeeBaseFragment
import com.coffee.order.databinding.FragmentSettingBinding
import com.coffee.order.network.TokenManager
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Tab Setting — dành riêng cho EMPLOYEE (nhân viên).
 *
 * Chức năng:
 *  - Hiển thị tên & role của nhân viên đang đăng nhập
 *  - Nút đăng xuất
 *  - Danh sách thực đơn (read-only): tên, danh mục, giá, ảnh (Glide)
 *
 * Lưu ý: Tab này KHÔNG có trong bottom nav của OWNER.
 * Mọi thao tác chỉnh sửa menu chỉ có ở AdminMenuFragment.
 */
class SettingFragment : EmployeeBaseFragment<FragmentSettingBinding>(
    FragmentSettingBinding::inflate
) {
    private val menuItemAdapter = MenuAdapter()

    override fun setUpEventListeners() {
        binding.apply {
            textViewUserName.text = TokenManager.getDisplayName()
            textViewUserRole.text = "Nhân viên"

            buttonLogout.setOnClickListener {
                employeeActivity.logout()
            }

            menuItemRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = menuItemAdapter
            }
        }
    }

    override fun collectStateAndUpdateUi() {
        collectFlow(appViewModel.uiState.map { it.menuItems }.distinctUntilChanged()) {
            menuItemAdapter.submitList(it)
        }
    }
}
