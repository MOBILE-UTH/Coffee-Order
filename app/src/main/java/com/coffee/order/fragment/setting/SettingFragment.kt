package com.coffee.order.fragment.setting

import androidx.recyclerview.widget.LinearLayoutManager
import com.coffee.order.base.GlobalComposeHandler
import com.coffee.order.base.MainActivityBaseFragment
import com.coffee.order.base.components.AddMenuItemDialog
import com.coffee.order.base.components.ConfirmDeleteMenuItemDialog
import com.coffee.order.base.components.EditMenuItemDialog
import com.coffee.order.databinding.FragmentSettingBinding

class SettingFragment : MainActivityBaseFragment<FragmentSettingBinding>(
    FragmentSettingBinding::inflate
) {
    private val tableInfoAdapter = TableAdapter()
    private val menuItemAdapter = MenuAdapter()

    override fun setUpEventListeners() {

        binding.apply {
            menuItemRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = menuItemAdapter
            }
            fabAddItem.setOnClickListener {
                GlobalComposeHandler.showGlobalDialog {
                    AddMenuItemDialog(
                        onDismiss = { GlobalComposeHandler.hideGlobalDialog() },
                        onConfirm = { name, category, price ->
                            //TODO: Mai Dỗ Thành Nhơn xử lý logic thêm món vào menu ở đây
                            GlobalComposeHandler.hideGlobalDialog()
                        }
                    )
                }
            }
        }
    }

    override fun collectStateAndUpdateUi() {
        collectFlow(appViewModel.tableInfoList) {
            tableInfoAdapter.submitList(it)
        }

        collectFlow(appViewModel.menuItems) {
            menuItemAdapter.submitList(it)
        }
    }


    fun showConfirmDeleteMenuItemDialog(menuItemId: Int) {
        GlobalComposeHandler.showGlobalDialog {
            ConfirmDeleteMenuItemDialog(
                onDismissRequest = {
                    GlobalComposeHandler.hideGlobalDialog()
                },
                onConfirmDelete = {
                    // TODO: Mai Dỗ Thành Nhơn xử lý logic xoá món khỏi menu ở đây
                    GlobalComposeHandler.hideGlobalDialog()
                }
            )
        }
    }

    fun showEditMenuItemDialog(
        menuItemId: Int, initialName: String, initialCategory: String, initialPrice: Double
    ) {
        GlobalComposeHandler.showGlobalDialog {
            EditMenuItemDialog(
                initialName = initialName,
                initialCategory = initialCategory,
                initialPrice = initialPrice,
                onDismiss = { GlobalComposeHandler.hideGlobalDialog() },
                onConfirm = { name, category, price ->
                    // TODO: Mai Dỗ Thành Nhơn xử lý logic cập nhật món trong menu ở đây
                    GlobalComposeHandler.hideGlobalDialog()
                }
            )
        }
    }
}
