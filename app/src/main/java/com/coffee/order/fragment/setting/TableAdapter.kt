package com.coffee.order.fragment.setting

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coffee.order.databinding.LayoutTableManageItemBinding
import com.coffee.order.viewmodel.model.TableInfo

class TableAdapter : ListAdapter<TableInfo, TableAdapter.ViewHolder>(
    DiffCallback
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutTableManageItemBinding.inflate(
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
        private val binding: LayoutTableManageItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TableInfo) {
            binding.tvTableIdLabel.text = item.tableId.toString()
            binding.tvTableName.text = item.tableName
        }
    }

    private companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<TableInfo>() {
            override fun areItemsTheSame(oldItem: TableInfo, newItem: TableInfo): Boolean {
                return oldItem.tableId == newItem.tableId
            }

            override fun areContentsTheSame(oldItem: TableInfo, newItem: TableInfo): Boolean {
                return oldItem == newItem
            }
        }
    }
}
