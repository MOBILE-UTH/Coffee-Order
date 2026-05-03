package com.coffee.order.fragment.management

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coffee.order.databinding.LayoutEmptyTableBinding
import com.coffee.order.databinding.LayoutOccupiedTableBinding
import com.coffee.order.util.toDisplayTableNumber
import com.coffee.order.viewmodel.model.TableInfo

class TableGridAdapter(
    private val onTableClick: (TableInfo) -> Unit
) : ListAdapter<TableInfo, RecyclerView.ViewHolder>(DiffCallback) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).status) {
            TableInfo.Status.EMPTY -> VIEW_TYPE_EMPTY
            TableInfo.Status.OCCUPIED -> VIEW_TYPE_OCCUPIED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_EMPTY -> EmptyViewHolder(
                LayoutEmptyTableBinding.inflate(inflater, parent, false)
            )

            VIEW_TYPE_OCCUPIED -> OccupiedViewHolder(
                LayoutOccupiedTableBinding.inflate(inflater, parent, false)
            )

            else -> error("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is EmptyViewHolder -> holder.bind(item)
            is OccupiedViewHolder -> holder.bind(item)
        }
        holder.itemView.setOnClickListener {
            onTableClick(item)
        }
    }


    private class EmptyViewHolder(
        private val binding: LayoutEmptyTableBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TableInfo) {
            binding.tvNumber.text = item.tableId.toDisplayTableNumber()
            binding.tvCapacityValue.text = item.maxPeople.toString()
        }
    }

    private class OccupiedViewHolder(
        private val binding: LayoutOccupiedTableBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TableInfo) {
            binding.tvNumber.text = item.tableName
            binding.tvCapacityValue.text = item.maxPeople.toString()
        }
    }

    private companion object {
        const val VIEW_TYPE_EMPTY = 0
        const val VIEW_TYPE_OCCUPIED = 1

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

