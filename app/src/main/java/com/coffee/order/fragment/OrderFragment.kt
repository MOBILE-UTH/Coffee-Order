package com.coffee.order.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.compose.runtime.Composable
import com.coffee.order.base.GlobalComposeHandler
import com.coffee.order.base.MainActivityBaseFragment
import com.coffee.order.base.components.SelectMenuItemBottomSheet
import com.coffee.order.databinding.FragmentOrderBinding
import com.coffee.order.viewmodel.model.MenuItem

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

        fun getItemQuantity(menuItemId: Long): Int {
            return items[menuItemId] ?: 0
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

    fun showSelectMenuItemBottomSheet(cart: Cart, menuItems: List<MenuItem>) {
        GlobalComposeHandler.showGlobalBottomSheet {
            SelectMenuItemBottomSheet(
                cart = cart,
                menuItems = menuItems,
                onDismiss = { GlobalComposeHandler.hideGlobalBottomSheet() },
                onConfirm = {
                    // TODO: Dương Minh Nhân thực hiện cập nhật view theo cart đã chọn ở đây
                    GlobalComposeHandler.hideGlobalBottomSheet()
                }
            )
        }
    }
}