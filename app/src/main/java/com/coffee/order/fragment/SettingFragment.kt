package com.coffee.order.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.coffee.order.base.MainActivityBaseFragment
import com.coffee.order.databinding.FragmentSettingBinding
import com.coffee.order.viewmodel.model.MenuItem
import com.coffee.order.viewmodel.model.TableInfo
import kotlin.random.Random

class SettingFragment : MainActivityBaseFragment<FragmentSettingBinding>(
    FragmentSettingBinding::inflate
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun setUpEventListeners() {
        binding.apply {
            addMenuItemButton.setOnClickListener {
                appViewModel.addMenuItem(
                    MenuItem(
                        menuItemId = Random.nextLong(),
                        name = "Espresso",
                        category = "Coffee",
                        price = 2.5
                    )
                )
            }

            addTableInfoButton.setOnClickListener {
                appViewModel.addTableInfo(
                    TableInfo(
                        tableId = Random.nextLong(),
                        tableName = "Table ${Random.nextInt(1, 10)}",
                        status = TableInfo.Status.entries.random()
                    )
                )
            }
        }

    }

    @SuppressLint("SetTextI18n")
    override fun collectStateAndUpdateUi() {
        collectFlow(appViewModel.menuItems) {
            binding.menuItemsTextView.text = """
                Menu Items:
                ${it.joinToString("\n") { menuItem -> "- ${menuItem.name} (${menuItem.category}): $${menuItem.price}" }}
            """.trimIndent()
        }

        collectFlow(appViewModel.tableInfoList) {
            binding.tableInfosTextView.text = """
                Table Info:
                ${it.joinToString("\n") { tableInfo -> "- Table ${tableInfo.tableId}: ${tableInfo.status}" }}
            """.trimIndent()
        }
    }
}