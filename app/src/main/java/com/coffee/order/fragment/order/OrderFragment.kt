package com.coffee.order.fragment.order

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import com.coffee.order.R
import com.coffee.order.base.GlobalComposeHandler
import com.coffee.order.base.MainActivityBaseFragment
import com.coffee.order.base.components.SelectMenuItemBottomSheet
import com.coffee.order.databinding.FragmentOrderBinding
import com.coffee.order.util.formatPrice
import com.coffee.order.viewmodel.model.MenuItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.navigation.fragment.findNavController
import com.coffee.order.viewmodel.model.HistoryOrder
import com.coffee.order.viewmodel.model.TableInfo
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        val existingTable = appViewModel.tableInfoList.value.find { it.tableId == tableId }
        if (tableId == null || existingTable == null) {
            mainActivity.onBackPressedDispatcher.onBackPressed()
            return
        }
        // Initialize cart with existing order items if any
        cart = Cart(tableId = tableId)
        tableInfo = existingTable
        tableInfo.orderItems.forEach { orderItem ->
            cart.addItem(orderItem.menuItemId, orderItem.quantity)
        }


        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    updateTableInfoFromCart()
                    findNavController().popBackStack()
                }
            }
        )
    }

    private fun updateTableInfoFromCart() {
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
                    menuItems = appViewModel.menuItems.value
                )
            }
            cardViewSummary.visibility = View.GONE
            linearLayoutActions.visibility = View.GONE
            listViewOrderItems.adapter = activeOrderListAdapter
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
        val menuItems = appViewModel.menuItems.value
        val orderedMenuItems = cartItems.mapNotNull { (menuItemId, quantity) ->
            menuItems.find { it.menuItemId == menuItemId }?.copy() // copy to avoid reference issues
        }

        val subTotal = orderedMenuItems.sumOf { menuItem ->
            val quantity = cartItems[menuItem.menuItemId] ?: 0
            menuItem.price * quantity
        }
        val total = (subTotal * 1.1)

        val historyOrder = HistoryOrder(
            orderId = System.currentTimeMillis(),
            staffName = "Admin", // Currently hardcoded as we don't have auth
            tableId = cart.tableId,
            menuItems = orderedMenuItems,
            totalPrice = total,
            orderTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        )

        appViewModel.addHistoryOrder(historyOrder)
        appViewModel.updateTableInfo(
            TableInfo(
                tableId = cart.tableId,
                tableName = appViewModel.tableInfoList.value.find { it.tableId == cart.tableId }?.tableName
                    ?: "Bàn ${cart.tableId}",
                orderItems = emptyList(), // Clear current order items,
                maxPeople = 4
            )
        )
        cart.clear()
        findNavController().popBackStack()
    }

    override fun collectStateAndUpdateUi() {
        collectFlow(cart.items) {
            val cartItems = cart.snapshotItems()
            val orderRows = appViewModel.menuItems.value.mapNotNull { menuItem ->
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