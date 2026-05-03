package com.coffee.order.feature.employee.fragment.order

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.bumptech.glide.Glide
import com.coffee.order.databinding.ItemOrderActiveBinding
import java.text.DecimalFormat

class ActiveOrderListAdapter(
    private var items: List<OrderItemUi> = emptyList()
) : BaseAdapter() {
    override fun getCount(): Int = items.size

    override fun getItem(position: Int): OrderItemUi = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    fun submitItems(newItems: List<OrderItemUi>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = if (convertView == null) {
            ItemOrderActiveBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        } else {
            ItemOrderActiveBinding.bind(convertView)
        }

        val item = getItem(position)
        Glide.with(binding.imageViewMenuItem.context)
            .load(item.imageUrl)
            .into(binding.imageViewMenuItem)
        binding.textViewMenuName.text = item.name
        binding.textViewMenuCategory.text = item.category
        binding.textViewQuantity.text = "x${item.quantity}"
        binding.textViewLineTotal.text = formatPrice(item.lineTotal)

        return binding.root
    }

    fun formatPrice(value: Int): String {
        return DecimalFormat("#,###").format(value)
    }
}

data class OrderItemUi(
    val menuItemId: Long,
    val imageUrl: String,
    val name: String,
    val category: String,
    val quantity: Int,
    val lineTotal: Int, // Tổng tiền cho món này (price * quantity)
)
