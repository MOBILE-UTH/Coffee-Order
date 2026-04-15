package com.coffee.order.feature.employee.fragment.management

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.coffee.order.base.EmployeeBaseFragment
import com.coffee.order.base.GlobalComposeHandler
import com.coffee.order.databinding.FragmentManagementBinding
import com.coffee.order.feature.employee.component.TableActionBottomSheet

import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Màn hình quản lý thông tin về các bàn trong quán,
 * hiển thị trạng thái của từng bàn (trống, có khách,)
 * và cho phép nhân viên quản lý dễ dàng theo dõi tình hình phục vụ.
 */
class ManagementFragment : EmployeeBaseFragment<FragmentManagementBinding>(
    FragmentManagementBinding::inflate
) {
    private val tableGridAdapter = TableGridAdapter(
        onTableClick = { tableInfo ->
            GlobalComposeHandler.showGlobalBottomSheet {
                TableActionBottomSheet(
                    tableInfo = tableInfo,
                    onNavigateToOrder = {
                        GlobalComposeHandler.hideGlobalBottomSheet()
                        employeeActivity.navigateToCreateOrder(tableInfo.tableId)
                    },
                    onClearTable = {
                        GlobalComposeHandler.hideGlobalBottomSheet()
                        appViewModel.clearTable(tableInfo.tableId)
                    },
                )
            }
        }
    )

    override fun setUpEventListeners() {
        binding.apply {
            tableManagementGrid.apply {
                layoutManager = GridLayoutManager(context, 2)
                adapter = tableGridAdapter
            }

            buttonAddTable.visibility = View.VISIBLE
            buttonAddTable.setOnClickListener {
                AddTableBottomSheetDialogFragment().show(
                    childFragmentManager,
                    "AddTableBottomSheet"
                )
            }
        }
    }

    override fun collectStateAndUpdateUi() {
        collectFlow(appViewModel.uiState.map { it.tableInfoList }.distinctUntilChanged()) {
            tableGridAdapter.submitList(it)
        }
    }
}