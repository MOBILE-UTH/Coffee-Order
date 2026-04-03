package com.coffee.order.fragment.setting

import androidx.recyclerview.widget.LinearLayoutManager
import com.coffee.order.base.GlobalComposeHandler
import com.coffee.order.base.MainActivityBaseFragment
import com.coffee.order.base.components.AddMenuItemDialog
import com.coffee.order.base.components.ConfirmDeleteMenuItemDialog
import com.coffee.order.base.components.EditMenuItemDialog
import com.coffee.order.databinding.FragmentSettingBinding
import com.coffee.order.viewmodel.model.MenuItem

class SettingFragment : MainActivityBaseFragment<FragmentSettingBinding>(
    FragmentSettingBinding::inflate
) {
    private val tableInfoAdapter = TableAdapter()
    private val menuItemAdapter = MenuAdapter(onDeleteMenuItem = { menuItem ->
        showConfirmDeleteMenuItemDialog(menuItem.menuItemId)
    }, onEditMenuItem = { menuItem ->
        showEditMenuItemDialog(
            menuItemId = menuItem.menuItemId,
            initialName = menuItem.name,
            initialCategory = menuItem.category,
            initialPrice = menuItem.price.toDouble()
        )
    })

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
                            appViewModel.addMenuItem(
                                MenuItem(
                                    name = name,
                                    category = category,
                                    price = price
                                )
                            )
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


    fun showConfirmDeleteMenuItemDialog(menuItemId: Long) {
        GlobalComposeHandler.showGlobalDialog {
            ConfirmDeleteMenuItemDialog(onDismissRequest = {
                GlobalComposeHandler.hideGlobalDialog()
            }, onConfirmDelete = {
                appViewModel.deleteMenuItem(menuItemId)
                GlobalComposeHandler.hideGlobalDialog()
            })
        }
    }

    fun showEditMenuItemDialog(
        menuItemId: Long, initialName: String, initialCategory: String, initialPrice: Double
    ) {
        GlobalComposeHandler.showGlobalDialog {
            EditMenuItemDialog(
                initialName = initialName,
                initialCategory = initialCategory,
                initialPrice = initialPrice,
                onDismiss = { GlobalComposeHandler.hideGlobalDialog() },
                onConfirm = { name, category, price ->
                    appViewModel.updateMenuItem(
                        MenuItem(
                            menuItemId = menuItemId,
                            name = name,
                            category = category,
                            price = price.toInt()
                        )
                    )
                    GlobalComposeHandler.hideGlobalDialog()
                })
        }
    }
}
