package com.coffee.order.fragment

import android.annotation.SuppressLint
import com.coffee.order.base.MainActivityBaseFragment
import com.coffee.order.databinding.FragmentManagementBinding
import com.coffee.order.viewmodel.model.TableInfo

class ManagementFragment : MainActivityBaseFragment<FragmentManagementBinding>(
    FragmentManagementBinding::inflate
) {
    override fun setUpEventListeners() {
        binding.apply {
            manageDataButton.setOnClickListener {
                appViewModel.updateTableInfo(
                    tableInfo = appViewModel.tableInfoList.value.firstOrNull()?.copy(
                        tableName = "Table ${appViewModel.tableInfoList.value.size + 1}",
                        status = TableInfo.Status.entries.random()
                    ) ?: TableInfo(
                        tableId = 1L, tableName = "Table 1", status = TableInfo.Status.OCCUPIED
                    )
                )
            }
            createOrderButton.setOnClickListener {
                mainActivity.navigateToCreateOrder(orderId = System.currentTimeMillis())
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun collectStateAndUpdateUi() {
        collectFlow(appViewModel.tableInfoList) {
            binding.managementInfoTextView.text = """
                Table Info:
                ${it.joinToString("\n") { tableInfo -> "- Table ${tableInfo.tableId}: ${tableInfo.status}" }}
            """.trimIndent()
        }
    }
}