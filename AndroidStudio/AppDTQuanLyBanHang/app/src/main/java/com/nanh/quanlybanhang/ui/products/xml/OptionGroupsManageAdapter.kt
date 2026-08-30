package com.nanh.quanlybanhang.ui.products.xml

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nanh.quanlybanhang.databinding.ItemOptionGroupRowBinding
import com.nanh.quanlybanhang.ui.products.OptionGroupRowUi

class OptionGroupsManageAdapter(
    private val listener: Listener,
) : ListAdapter<OptionGroupRowUi, OptionGroupsManageAdapter.Vh>(
    object : DiffUtil.ItemCallback<OptionGroupRowUi>() {
        override fun areItemsTheSame(a: OptionGroupRowUi, b: OptionGroupRowUi) = a.id == b.id
        override fun areContentsTheSame(a: OptionGroupRowUi, b: OptionGroupRowUi) = a == b
    },
) {

    interface Listener {
        fun onOpenGroup(item: OptionGroupRowUi)
        fun onEditGroup(item: OptionGroupRowUi)
        fun onDeleteGroup(item: OptionGroupRowUi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val binding = ItemOptionGroupRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Vh(binding, listener)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.bind(getItem(position))
    }

    class Vh(
        private val binding: ItemOptionGroupRowBinding,
        private val listener: Listener,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OptionGroupRowUi) {
            binding.textOptionGroupName.text = item.name
            binding.cardOptionGroup.setOnClickListener { listener.onOpenGroup(item) }
            binding.buttonEditOptionGroup.setOnClickListener { listener.onEditGroup(item) }
            binding.buttonDeleteOptionGroup.setOnClickListener { listener.onDeleteGroup(item) }
        }
    }
}
