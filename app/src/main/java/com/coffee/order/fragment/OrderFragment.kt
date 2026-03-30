package com.coffee.order.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import com.coffee.order.base.MainActivityBaseFragment
import com.coffee.order.databinding.FragmentOrderBinding

class OrderFragment : MainActivityBaseFragment<FragmentOrderBinding>(
    FragmentOrderBinding::inflate
) {

    companion object {
        private const val TABLE_ID_KEY = "table_id"
        fun createBundle(tableId: Long) = Bundle().apply {
            putLong(TABLE_ID_KEY, tableId)
        }
    }

    class Cart(
        val tableId: Long,
    ) {
        /**
         * key: menuItemId, value: quantity
         */
        private val items = mutableMapOf<Long, Int>()

        fun addItem(menuItemId: Long, quantity: Int = 1) {
            items[menuItemId] = (items[menuItemId] ?: 0) + quantity
        }

        fun removeItem(menuItemId: Long, quantity: Int = 1) {
            val currentQuantity = items[menuItemId] ?: return
            val newQuantity = currentQuantity - quantity
            if (newQuantity <= 0) {
                items.remove(menuItemId)
            } else {
                items[menuItemId] = newQuantity
            }
        }
    }

    private lateinit var cart: Cart
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tableId = arguments?.getLong(TABLE_ID_KEY)
        if (tableId == null) {
            mainActivity.onBackPressedDispatcher.onBackPressed()
            return
        }
        cart = Cart(tableId = tableId)
    }

    @SuppressLint("SetTextI18n")
    override fun setUpEventListeners() {
        binding.apply {
            textViewOrder.text = """
                Create Order for Table ${cart.tableId}
            """.trimIndent()
        }
    }

    override fun collectStateAndUpdateUi() {
    }
}

