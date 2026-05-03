package com.coffee.order.feature.employee.fragment.history

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.coffee.order.base.EmployeeBaseFragment
import com.coffee.order.databinding.FragmentHistoryBinding

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged

class HistoryFragment : EmployeeBaseFragment<FragmentHistoryBinding>(
    FragmentHistoryBinding::inflate
) {
    private val historyAdapter = HistoryAdapter()

    override fun setUpEventListeners() {
        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = historyAdapter
        }
    }

    override fun collectStateAndUpdateUi() {
        collectFlow(appViewModel.uiState.map { it.historyOrders }.distinctUntilChanged()) {
            historyAdapter.submitList(it)
            binding.historyEmptyTextView.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}