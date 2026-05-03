package com.coffee.order.feature.employee.fragment.setting

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coffee.order.R
import com.coffee.order.databinding.LayoutMenuItemBinding
import com.coffee.order.domain.model.MenuItem
import java.text.NumberFormat
import java.util.Locale

class MenuAdapter : ListAdapter<MenuItem, MenuAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutMenuItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: LayoutMenuItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MenuItem) {
            binding.apply {
                tvProductName.text = item.name
                tvCategory.text = item.category
                tvProductPrice.text = currencyFormatter.format(item.price)

                // Load ảnh bằng Glide
                if (!item.imageUrl.isNullOrBlank()) {
                    Glide.with(ivProductImage.context)
                        .load(item.imageUrl)
                        .placeholder(R.drawable.ic_menu_book)
                        .error(R.drawable.ic_menu_book)
                        .centerCrop()
                        .into(ivProductImage)
                } else {
                    Glide.with(ivProductImage.context).clear(ivProductImage)
                    ivProductImage.setImageResource(R.drawable.ic_menu_book)
                }
            }
        }
    }

    private companion object {
        val currencyFormatter: NumberFormat =
            NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"))

        val DiffCallback = object : DiffUtil.ItemCallback<MenuItem>() {
            override fun areItemsTheSame(oldItem: MenuItem, newItem: MenuItem) =
                oldItem.menuItemId == newItem.menuItemId

            override fun areContentsTheSame(oldItem: MenuItem, newItem: MenuItem) =
                oldItem == newItem && oldItem.imageUrl == newItem.imageUrl
        }
    }
}
