package com.coffee.order.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import com.coffee.order.base.MainActivityBaseFragment
import com.coffee.order.databinding.FragmentOrderBinding

class OrderFragment : MainActivityBaseFragment<FragmentOrderBinding>(
    FragmentOrderBinding::inflate
) {

    companion object {
        private const val ORDER_ID_KEY = "order_id"
        fun createBundle(orderId: Long) = Bundle().apply {
            putLong(ORDER_ID_KEY, orderId)
        }
    }

    private var orderId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        orderId = arguments?.getLong(ORDER_ID_KEY)
    }

    @SuppressLint("SetTextI18n")
    override fun setUpEventListeners() {
        binding.apply {
            textViewOrder.text = "Order ID: ${orderId.toTimeString()}"
        }
    }

    override fun collectStateAndUpdateUi() {
    }

    @SuppressLint("SimpleDateFormat")
    private fun Long?.toTimeString(): String {
        if (this == null) return "N/A"
        val date = java.util.Date(this)
        val format = java.text.SimpleDateFormat("HH:mm:ss")
        return format.format(date)
    }
}

