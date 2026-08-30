package com.nanh.quanlybanhang.ui.suppliers.xml

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nanh.quanlybanhang.databinding.ItemSupplierRowBinding
import com.nanh.quanlybanhang.ui.suppliers.SupplierListUi

class SuppliersListAdapter : ListAdapter<SupplierListUi, SuppliersListAdapter.Vh>(
    object : DiffUtil.ItemCallback<SupplierListUi>() {
        override fun areItemsTheSame(a: SupplierListUi, b: SupplierListUi): Boolean = a.id == b.id
        override fun areContentsTheSame(a: SupplierListUi, b: SupplierListUi): Boolean = a == b
    },
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val binding = ItemSupplierRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Vh(binding)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.bind(getItem(position))
    }

    class Vh(private val binding: ItemSupplierRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SupplierListUi) {
            binding.textSupplierName.text = item.name
            bindOptionalLine(binding.textSupplierPhone, item.phone)
            bindOptionalLine(binding.textSupplierAddress, item.address)
            bindOptionalLine(binding.textSupplierNote, item.note)
        }

        private fun bindOptionalLine(view: TextView, value: String?) {
            if (value.isNullOrBlank()) {
                view.isVisible = false
            } else {
                view.isVisible = true
                view.text = value
            }
        }
    }
}
