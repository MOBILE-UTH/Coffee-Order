package com.coffee.order.feature.employee.fragment.order

import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.coffee.order.R
import com.coffee.order.base.GlobalComposeHandler
import com.coffee.order.base.EmployeeBaseFragment
import com.coffee.order.feature.employee.component.SelectMenuItemBottomSheet
import com.coffee.order.databinding.FragmentOrderBinding
import com.coffee.order.util.formatPrice
import com.coffee.order.domain.model.MenuItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.core.graphics.drawable.toDrawable
import com.coffee.order.domain.model.TableInfo

class OrderFragment : EmployeeBaseFragment<FragmentOrderBinding>(
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
        private val _items = MutableStateFlow<Map<Long, Int>>(emptyMap())
        val items: StateFlow<Map<Long, Int>> = _items.asStateFlow()

        fun addItem(menuItemId: Long, quantity: Int = 1) {
            _items.value = _items.value.toMutableMap().apply {
                this[menuItemId] = (this[menuItemId] ?: 0) + quantity
            }
        }

        fun removeItem(menuItemId: Long, quantity: Int = 1) {
            _items.value = _items.value.toMutableMap().apply {
                val currentQuantity = this[menuItemId] ?: return@apply
                val newQuantity = currentQuantity - quantity
                if (newQuantity <= 0) {
                    this.remove(menuItemId)
                } else {
                    this[menuItemId] = newQuantity
                }
            }
        }

        fun getItemQuantity(menuItemId: Long): Int {
            return items.value[menuItemId] ?: 0
        }

        fun snapshotItems(): Map<Long, Int> {
            return items.value
        }

        fun clear() {
            _items.value = emptyMap()
        }
    }

    private lateinit var cart: Cart
    private lateinit var tableInfo: TableInfo
    private val activeOrderListAdapter = ActiveOrderListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tableId = arguments?.getLong(TABLE_ID_KEY)
        val existingTable = appViewModel.uiState.value.tableInfoList.find { it.tableId == tableId }
        if (tableId == null || existingTable == null) {
            employeeActivity.onBackPressedDispatcher.onBackPressed()
            return
        }
        // Initialize cart with existing order items if any
        cart = Cart(tableId = tableId)
        tableInfo = existingTable
        tableInfo.orderItems.forEach { orderItem ->
            cart.addItem(orderItem.menuItemId, orderItem.quantity)
        }
    }

    private fun updateTableInfoFromCart() {
        // Update local UI state; the actual server sync happens on submit/pay
        appViewModel.updateTableInfo(
            tableInfo.copy(
                orderItems = cart.snapshotItems().map {
                    TableInfo.OrderItem(it.key, it.value)
                }
            )
        )
    }

    override fun setUpEventListeners() {
        binding.apply {
            buttonAddItems.setOnClickListener {
                showSelectMenuItemBottomSheet(
                    cart = cart,
                    menuItems = appViewModel.uiState.value.menuItems
                )
            }
            cardViewSummary.visibility = View.GONE
            linearLayoutActions.visibility = View.GONE
            listViewOrderItems.adapter = activeOrderListAdapter
            listViewOrderItems.divider = Color.TRANSPARENT.toDrawable()
            listViewOrderItems.dividerHeight = 16 // px
            binding.textViewTableName.text = tableInfo.tableName

            buttonDeleteCart.setOnClickListener {
                showDeleteTableConfirmation()
            }

            buttonPayment.setOnClickListener {
                showPaymentConfirmation()
            }
        }
    }

    private fun showDeleteTableConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.confirm_delete_table_title)
            .setMessage(R.string.confirm_delete_table_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.confirm) { _, _ ->
                appViewModel.updateTableInfo(tableInfo.copy(orderItems = emptyList()))
                cart.clear()
                findNavController().popBackStack()
            }
            .show()
    }

    private fun showPaymentConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.confirm_payment_title)
            .setMessage(R.string.confirm_payment_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.confirm) { _, _ ->
                processPayment()
            }
            .show()
    }

    private fun processPayment() {
        val cartItems = cart.snapshotItems()
        if (cartItems.isEmpty()) return

        // First submit/update the order on the server, then pay it
        appViewModel.submitOrder(cart.tableId, cartItems) {
            appViewModel.payOrder(cart.tableId) {
                cart.clear()
                findNavController().popBackStack()
            }
        }
    }

    override fun collectStateAndUpdateUi() {
        collectFlow(cart.items) {
            val cartItems = cart.snapshotItems()
            val orderRows = appViewModel.uiState.value.menuItems.mapNotNull { menuItem ->
                val quantity = cartItems[menuItem.menuItemId] ?: 0
                if (quantity <= 0) return@mapNotNull null
                OrderItemUi(
                    menuItemId = menuItem.menuItemId,
                    name = menuItem.name,
                    imageUrl = menuItem.imageUrl ?: "",
                    category = menuItem.category,
                    quantity = quantity,
                    lineTotal = menuItem.price * quantity,
                )
            }

            activeOrderListAdapter.submitItems(orderRows)
            binding.listViewOrderItems.post { expandListHeight() }
            updateTableInfoFromCart()
            if (orderRows.isEmpty()) {
                binding.cardViewSummary.visibility = View.GONE
                binding.linearLayoutActions.visibility = View.GONE
                binding.textViewItemsServed.visibility = View.GONE
            } else {
                binding.cardViewSummary.visibility = View.VISIBLE
                binding.linearLayoutActions.visibility = View.VISIBLE
                binding.textViewItemsServed.visibility = View.VISIBLE
                binding.textViewItemsServed.text =
                    getString(R.string.items_served, orderRows.sumOf { it.quantity })
                val subTotal = orderRows.sumOf { it.lineTotal }
                val vat = (subTotal * 0.1).toInt()
                val total = subTotal + vat
                binding.textViewSubtotal.text = formatPrice(subTotal)
                binding.textViewVAT.text = formatPrice(vat)
                binding.textViewTotalDue.text = formatPrice(total)
            }
        }
    }

    fun showSelectMenuItemBottomSheet(cart: Cart, menuItems: List<MenuItem>) {
        GlobalComposeHandler.showGlobalBottomSheet {
            SelectMenuItemBottomSheet(
                cart = cart,
                menuItems = menuItems,
                onDismiss = { GlobalComposeHandler.hideGlobalBottomSheet() },
                onConfirm = {
                    GlobalComposeHandler.hideGlobalBottomSheet()
                }
            )
        }
    }

    private fun expandListHeight() {
        val adapter = binding.listViewOrderItems.adapter ?: return
        var totalHeight = 0
        for (i in 0 until adapter.count) {
            val listItem = adapter.getView(i, null, binding.listViewOrderItems)
            listItem.measure(
                View.MeasureSpec.makeMeasureSpec(
                    binding.listViewOrderItems.width,
                    View.MeasureSpec.AT_MOST
                ),
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