package com.nanh.quanlybanhang.ui.products.xml

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nanh.quanlybanhang.databinding.ItemOptionItemRowBinding
import com.nanh.quanlybanhang.ui.products.OptionItemRowUi

class OptionItemsListAdapter(
    private val listener: Listener,
) : ListAdapter<OptionItemRowUi, OptionItemsListAdapter.Vh>(
    object : DiffUtil.ItemCallback<OptionItemRowUi>() {
        override fun areItemsTheSame(a: OptionItemRowUi, b: OptionItemRowUi) = a.id == b.id
        override fun areContentsTheSame(a: OptionItemRowUi, b: OptionItemRowUi) = a == b
    },
) {

    interface Listener {
        fun onEditItem(item: OptionItemRowUi)
        fun onDeleteItem(item: OptionItemRowUi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val binding = ItemOptionItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Vh(binding, listener)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.bind(getItem(position))
    }

    class Vh(
        private val binding: ItemOptionItemRowBinding,
        private val listener: Listener,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OptionItemRowUi) {
            binding.textOptionItemName.text = item.name
            binding.textOptionItemExtraPrice.text = item.extraPriceLabel
            binding.buttonEditOptionItem.setOnClickListener { listener.onEditItem(item) }
            binding.buttonDeleteOptionItem.setOnClickListener { listener.onDeleteItem(item) }
        }
    }
}
