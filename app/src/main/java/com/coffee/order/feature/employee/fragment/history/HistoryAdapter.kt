package com.coffee.order.feature.employee.fragment.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coffee.order.R
import com.coffee.order.databinding.LayoutOrderSummaryItemBinding
import com.coffee.order.domain.model.HistoryOrder
import java.text.NumberFormat
import java.util.Locale

class HistoryAdapter :
    ListAdapter<HistoryOrder, HistoryAdapter.ViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutOrderSummaryItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: LayoutOrderSummaryItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HistoryOrder) {
            val context = binding.root.context
            binding.tvOrderId.text = context.getString(R.string.history_order_id, item.orderId)
            binding.tvOrderTime.text = item.orderTime
            binding.tvOrderDetails.text = item.menuItems.joinToString { it.name }
            binding.tvOrderTable.text = context.getString(R.string.history_table, item.tableId)
            binding.tvOrderStaff.text = item.staffName
            binding.tvOrderTotalPrice.text = currencyFormatter.format(item.totalPrice ?: 0.0)
        }
    }

    private companion object {
        val currencyFormatter: NumberFormat =
            NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"))

        val DiffCallback = object : DiffUtil.ItemCallback<HistoryOrder>() {
            override fun areItemsTheSame(
                oldItem: HistoryOrder,
                newItem: HistoryOrder
            ): Boolean {
                return oldItem.orderId == newItem.orderId
            }

            override fun areContentsTheSame(
                oldItem: HistoryOrder,
                newItem: HistoryOrder
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
