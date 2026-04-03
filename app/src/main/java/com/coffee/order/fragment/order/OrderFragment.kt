package com.coffee.order.fragment.order

import android.os.Bundle
import android.view.View
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

        fun snapshotItems(): Map<Long, Int> {
            return items.toMap()
        }
    }

    private lateinit var cart: Cart
    private val activeOrderListAdapter = ActiveOrderListAdapter()
    private var latestMenuItems: List<MenuItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tableId = arguments?.getLong(TABLE_ID_KEY)
        if (tableId == null) {
            mainActivity.onBackPressedDispatcher.onBackPressed()
            return
        }
        cart = Cart(tableId = tableId)
    }

    override fun setUpEventListeners() {
        binding.apply {
            buttonAddItems.setOnClickListener {
                showSelectMenuItemBottomSheet(
                    cart = cart,
                    menuItems = appViewModel.menuItems.value
                )
            }
            listViewOrderItems.adapter = activeOrderListAdapter
        }
    }

    override fun collectStateAndUpdateUi() {
        collectFlow(appViewModel.menuItems) { menuItems ->
            latestMenuItems = menuItems
            updateActiveOrderItems()
        }
    }

    fun showSelectMenuItemBottomSheet(cart: Cart, menuItems: List<MenuItem>) {
        GlobalComposeHandler.showGlobalBottomSheet {
            SelectMenuItemBottomSheet(
                cart = cart,
                menuItems = menuItems,
                onDismiss = { GlobalComposeHandler.hideGlobalBottomSheet() },
                onConfirm = {
                    updateActiveOrderItems()
                    GlobalComposeHandler.hideGlobalBottomSheet()
                }
            )
        }
    }

    private fun updateActiveOrderItems() {
        val cartItems = cart.snapshotItems()
        val orderRows = latestMenuItems.mapNotNull { menuItem ->
            val quantity = cartItems[menuItem.menuItemId] ?: 0
            if (quantity <= 0) return@mapNotNull null
            OrderItemUi(
                menuItemId = menuItem.menuItemId,
                name = menuItem.name,
                category = menuItem.category,
                quantity = quantity,
                lineTotal = menuItem.price * quantity,
            )
        }

        activeOrderListAdapter.submitItems(orderRows)
        binding.listViewOrderItems.post { expandListHeight() }
    }

    private fun expandListHeight() {
        val adapter = binding.listViewOrderItems.adapter ?: return
        var totalHeight = 0
        for (i in 0 until adapter.count) {
            val listItem = adapter.getView(i, null, binding.listViewOrderItems)
            listItem.measure(
                View.MeasureSpec.makeMeasureSpec(binding.listViewOrderItems.width, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.UNSPECIFIED
            )
            totalHeight += listItem.measuredHeight
        }
        binding.listViewOrderItems.layoutParams = binding.listViewOrderItems.layoutParams.apply {
            height = totalHeight +
                (binding.listViewOrderItems.dividerHeight * (adapter.count - 1).coerceAtLeast(0))
        }
        binding.listViewOrderItems.requestLayout()
    }
}