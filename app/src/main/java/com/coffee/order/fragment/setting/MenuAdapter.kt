package com.coffee.order.fragment.setting

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coffee.order.databinding.LayoutMenuItemBinding
import com.coffee.order.viewmodel.model.MenuItem
import java.text.NumberFormat
import java.util.Locale

class MenuAdapter(
    private val onDeleteMenuItem: (menuItem: MenuItem) -> Unit,
    private val onEditMenuItem: (menuItem: MenuItem) -> Unit,
) : ListAdapter<MenuItem, MenuAdapter.ViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutMenuItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: LayoutMenuItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MenuItem) {
            binding.apply {
                tvProductId.text = item.menuItemId.toString()
                tvProductName.text = item.name
                tvCategory.text = item.category
                tvProductPrice.text = currencyFormatter.format(item.price)
                btnEditProduct.setOnClickListener {
                    onEditMenuItem(item)
                }
                btnDeleteProduct.setOnClickListener {
                    onDeleteMenuItem(item)
                }
            }
        }
    }

    private companion object {
        val currencyFormatter: NumberFormat =
            NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"))

        val DiffCallback = object : DiffUtil.ItemCallback<MenuItem>() {
            override fun areItemsTheSame(oldItem: MenuItem, newItem: MenuItem): Boolean {
                return oldItem.menuItemId == newItem.menuItemId
            }

            override fun areContentsTheSame(oldItem: MenuItem, newItem: MenuItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
