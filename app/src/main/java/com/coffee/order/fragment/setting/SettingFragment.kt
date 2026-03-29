package com.coffee.order.fragment.setting

import androidx.recyclerview.widget.LinearLayoutManager
import com.coffee.order.base.MainActivityBaseFragment
import com.coffee.order.databinding.FragmentSettingBinding

class SettingFragment : MainActivityBaseFragment<FragmentSettingBinding>(
    FragmentSettingBinding::inflate
) {
    private val tableInfoAdapter = TableAdapter()
    private val menuItemAdapter = MenuAdapter()

    override fun setUpEventListeners() {
        binding.tableRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = tableInfoAdapter
        }

        binding.menuItemRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = menuItemAdapter
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


}