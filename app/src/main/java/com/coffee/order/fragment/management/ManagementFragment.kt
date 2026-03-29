package com.coffee.order.fragment.management

import androidx.recyclerview.widget.GridLayoutManager
import com.coffee.order.base.MainActivityBaseFragment
import com.coffee.order.databinding.FragmentManagementBinding

/**
 * Màn hình quản lý thông tin về các bàn trong quán,
 * hiển thị trạng thái của từng bàn (trống, có khách, đang chờ thanh toán)
 * và cho phép nhân viên quản lý dễ dàng theo dõi tình hình phục vụ.
 */
class ManagementFragment : MainActivityBaseFragment<FragmentManagementBinding>(
    FragmentManagementBinding::inflate
) {
    private val tableGridAdapter = TableGridAdapter(
        onTableClick = { tableInfo ->
            mainActivity.navigateToCreateOrder(tableInfo.tableId)
        }
    )

    override fun setUpEventListeners() {
        binding.tableManagementGrid.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = tableGridAdapter

        }
    }

    override fun collectStateAndUpdateUi() {
        collectFlow(appViewModel.tableInfoList) {
            tableGridAdapter.submitList(it)
        }
    }
}