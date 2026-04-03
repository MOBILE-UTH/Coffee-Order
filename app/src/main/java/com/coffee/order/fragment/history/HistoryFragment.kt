package com.coffee.order.fragment.history

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.coffee.order.base.MainActivityBaseFragment
import com.coffee.order.databinding.FragmentHistoryBinding

class HistoryFragment : MainActivityBaseFragment<FragmentHistoryBinding>(
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
        collectFlow(appViewModel.historyOrders) {
            historyAdapter.submitList(it)
            binding.historyEmptyTextView.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}