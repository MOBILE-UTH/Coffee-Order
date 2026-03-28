package com.coffee.order.fragment

import android.annotation.SuppressLint
import com.coffee.order.base.MainActivityBaseFragment
import com.coffee.order.databinding.FragmentHistoryBinding
import com.coffee.order.viewmodel.model.HistoryOrder
import kotlin.random.Random

class HistoryFragment : MainActivityBaseFragment<FragmentHistoryBinding>(
    FragmentHistoryBinding::inflate
) {
    override fun setUpEventListeners() {
        binding.apply {
            addHistoryButton.setOnClickListener {
                appViewModel.addHistoryOrder(
                    HistoryOrder(
                        orderId = Random.nextLong(),
                        tableId = Random.nextLong(),
                        menuItems = listOf(
                            appViewModel.menuItems.value.firstOrNull()
                                ?: return@setOnClickListener
                        ),
                        totalPrice = appViewModel.menuItems.value.firstOrNull()?.price
                            ?: 0.0,
                        orderTime = "2024-06-01 12:00:00"
                    )
                )
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun collectStateAndUpdateUi() {
        collectFlow(appViewModel.historyOrders) {
            binding.historyInfoTextView.text = """
                        History Orders:
                        ${it.joinToString("\n") { historyOrder -> "- Order ${historyOrder.orderId} at Table ${historyOrder.tableId}: $${historyOrder.totalPrice} (${historyOrder.orderTime})" }}
                    """.trimIndent()

        }
    }
}